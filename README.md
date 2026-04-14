# Tempo

A Kotlin Multiplatform workout logger targeting Android, iOS, and Wear OS. Workouts are tracked in real time, persisted locally via SQLDelight, and optionally synced through a Ktor backend.

Built as a portfolio project to demonstrate production KMP architecture across multiple targets: offline-first data patterns, MVI state management, wearable platform integration, and a multi-module Gradle build with automated quality checks.

---

## Platforms

| Target | Technology |
|---|---|
| Android phone | Compose Multiplatform |
| iOS phone | Compose Multiplatform |
| Wear OS | Wear Compose + Health Services API |
| Backend | Ktor (Netty) |

---

## Architecture

Tempo uses a **feature module** structure with a strict unidirectional dependency graph. Feature modules depend only on `:core:domain`. Nothing in `:core:domain` knows about any framework.

```
:androidApp          iosApp (Xcode)       :wearApp
      │                    │                   │
      └──────────┬──────────┘                  │
                 ▼                             │
           :composeApp                         │
        (shared Compose UI)                    │
                 │                             │
                 ▼                             ▼
   :feature:workout  :feature:history  :feature:profile
                 │
                 ▼
           :core:domain          ← pure Kotlin, no framework deps
           :core:data            ← TempoDataModule, repository wiring
           :core:database        ← SQLDelight schemas + drivers
           :core:network         ← Ktor client (in progress)
                 │
                 ▼
              :server            ← Ktor backend (in progress)
```

UI follows **MVI** (Model-View-Intent). The watch platform pushes heart rate samples and workout lifecycle events as intents into the same unidirectional flow as phone interactions.

---

## Module breakdown

### [`core:domain`](./core/domain)
Pure Kotlin. No Ktor, no SQLDelight, no Compose. Contains:

- **Models** — `Workout`, `ActiveWorkout`, `WorkoutSet`, `HeartRateSample`, `WorkoutSummary`, `WorkoutType`, `WorkoutStatus`, `DataSource`
- **Repository interfaces** — `WorkoutRepository`, `HeartRateRepository`, `WorkoutSetRepository`
- **Use cases** — `StartWorkoutUseCase`, `PauseWorkoutUseCase`, `ResumeWorkoutUseCase`, `FinishWorkoutUseCase`, `DiscardWorkoutUseCase`, `LogHeartRateSampleUseCase`, `GetWorkoutHistoryUseCase`

Because this layer has zero framework dependencies it is trivially unit testable without a device or emulator. Use case coverage is at 100% line / 97% branch.

### [`core:data`](./core/data)
`TempoDataModule` — wires the database layer to the domain repository interfaces. Accepts a platform-specific `DriverFactory` and exposes `WorkoutRepository`, `HeartRateRepository`, and `WorkoutSetRepository`. Network sync is a future concern.

### [`core:database`](./core/database)
SQLDelight schema definitions and generated type-safe query code. Three repositories backed by four tables (`WorkoutEntity`, `WorkoutSetEntity`, `HeartRateSampleEntity`, `WorkoutSummaryEntity`). Platform drivers for Android (`AndroidSqliteDriver`), iOS (`NativeSqliteDriver`), and JVM (`JdbcSqliteDriver`). JVM driver used in integration tests without a device.

### [`core:network`](./core/network)
Ktor client configuration and API interface definitions. Wires Android (OkHttp) and iOS (Darwin) engines through `expect`/`actual`. In progress.

### [`feature:workout`](./feature/workout)
Active workout tracking screen. Full MVI: `WorkoutViewModel` drives `WorkoutUiState` through `WorkoutIntent`s. Supports start, pause, resume, finish, and discard. Real-time heart rate display from wearable `Flow`. Timer updates every second via `viewModelScope`.

### [`feature:history`](./feature/history)
Workout history list. `HistoryViewModel` collects `GetWorkoutHistoryUseCase` as a `StateFlow`. `HistoryScreen` renders a `LazyColumn` of summary cards (duration, avg/max HR, set count, volume). Shows empty state for new users.

### [`feature:profile`](./feature/profile)
User settings and device management. In progress.

### [`composeApp`](./composeApp)
Shared Compose UI library for Android and iOS. Hosts `AppContainer` (manual DI — constructs all use cases from `TempoDataModule`) and `AppNavigation` (typed `NavHost` + `NavigationBar` with three top-level tabs). Produces a static iOS framework (`ComposeApp.framework`) consumed by the Xcode project.

### [`androidApp`](./androidApp)
Android application entry point. `MainActivity` creates `AppContainer` with `AndroidDriverFactory` and calls `setContent { App(...) }`. Depends only on `:composeApp`.

### [`wearApp`](./wearApp)
Wear OS entry point. Uses Health Services API for workout session management and heart rate. Pushes data to the phone via `DataClient`.

### [`server`](./server)
Ktor backend. Handles workout sync. Shares serialization with the client via `:core:domain` models. In progress.

---

## Domain model

```
Workout
  id: String
  type: WorkoutType          // Running | Cycling | Walking | Strength | Hiit | Yoga | Swimming | Other
  title: String?
  startedAt: Instant
  endedAt: Instant?          // null while active
  notes: String?

ActiveWorkout                // runtime-only aggregate, not persisted directly
  workout: Workout
  status: WorkoutStatus      // Preparing | Active | Paused | Completed | Discarded
  elapsedSeconds: Long
  currentHeartRateBpm: Int?
  sets: List<WorkoutSet>

WorkoutSet
  id: String
  workoutId: String
  exerciseName: String
  setNumber: Int
  reps: Int?
  weightKg: Double?
  durationSeconds: Long?
  completedAt: Instant

HeartRateSample
  workoutId: String
  bpm: Int
  recordedAt: Instant
  source: DataSource         // WearOs | WatchOs | Manual

WorkoutSummary               // read model for history list
  workoutId: String
  type: WorkoutType
  title: String?
  startedAt: Instant
  durationSeconds: Long
  averageHeartRateBpm: Int?
  maxHeartRateBpm: Int?
  totalSets: Int?
  totalVolumeKg: Double?
```

---

## Tech stack

| Concern | Library |
|---|---|
| UI (phone) | Compose Multiplatform 1.10.0 |
| UI (Wear OS) | Wear Compose |
| Navigation | AndroidX Navigation Compose 2.9.1 (KMP) |
| Async | Kotlin Coroutines + Flow |
| Local DB | SQLDelight 2.0.2 |
| Networking | Ktor 3.3.3 |
| Backend | Ktor server (Netty) |
| Serialization | kotlinx.serialization |
| Date/Time | kotlinx.datetime |
| Watch → Phone | Wearable Data Layer API |
| Heart rate (Wear OS) | Health Services API |

---

## Build & run

**Android**
```shell
./gradlew :androidApp:assembleDebug
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run, or use the KMP run configuration in Android Studio.

**Wear OS**
```shell
./gradlew :wearApp:assembleDebug
```

**Server**
```shell
./gradlew :server:run
```

---

## Quality

All checks are aggregated under a single task:

```shell
./gradlew audit
```

This runs across every module:

| Check | Tool |
|---|---|
| Lint + formatting | Detekt + ktlint |
| Unit tests | Kotlin test (JVM + iOS simulator) |
| Test coverage | Kover (JVM targets) |
| Dependency locking | Dependency Guard |
| Binary API compatibility | Binary Compatibility Validator |

CI runs `./gradlew audit` on every push and pull request via GitHub Actions on `macos-14` (Apple Silicon).

---

## Status

| Layer | Status |
|---|---|
| `core:domain` — models, repositories, use cases | Done — 100% line coverage |
| `core:database` — SQLDelight schema + all three repositories | Done — 94% line coverage |
| `core:data` — `TempoDataModule` wiring | Done |
| `core:network` — Ktor client | In progress |
| `feature:workout` — full MVI screen | Done |
| `feature:history` — history list screen | Done |
| `feature:profile` — settings screen | In progress |
| `androidApp` / `composeApp` / `iosApp` — navigation + DI wiring | Done |
| `wearApp` — Wear OS session + heart rate | In progress |
| `server` — Ktor backend | In progress |
