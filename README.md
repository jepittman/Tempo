# Tempo

A Kotlin Multiplatform workout logger targeting Android, iOS, Wear OS, and Apple Watch. Workouts are tracked in real time from either wearable platform and synced through a Ktor backend.

Built as a portfolio project to demonstrate KMP architecture across four targets, offline-first data patterns, and wearable platform integration.

---

## Platforms

| Target | Technology |
|---|---|
| Android phone | Compose Multiplatform |
| iOS phone | Compose Multiplatform |
| Wear OS (Pixel Watch) | Wear Compose + Health Services API |
| Apple Watch | SwiftUI + WatchConnectivity + HealthKit |
| Backend | Ktor (Netty) |

---

## Architecture

Tempo uses a **feature module** structure with a strict unidirectional dependency graph. Feature modules depend on `:core:domain`. Platform entry points (`:composeApp`, `:wearApp`) depend on feature modules. Nothing in `:core:domain` knows about any framework.

```
:composeApp / :wearApp / iosApp / watchosApp
        │
        ▼
:feature:workout  :feature:history  :feature:profile
        │                │                  │
        └────────────────┼──────────────────┘
                         ▼
                   :core:domain          ← pure Kotlin, no framework deps
                   :core:data            ← repository implementations
                   :core:database        ← SQLDelight schemas
                   :core:network         ← Ktor client setup
                         │
                         ▼
                      :server            ← Ktor backend
```

UI follows **MVI** (Model-View-Intent). The watch platforms push events (heart rate samples, workout lifecycle) as intents into the same unidirectional flow as phone interactions.

---

## Module breakdown

### [`core:domain`](./core/domain)
Pure Kotlin. No Ktor, no SQLDelight, no Compose. Contains:

- **Models** — [`Workout`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/Workout.kt), [`ActiveWorkout`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/ActiveWorkout.kt), [`WorkoutSet`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/WorkoutSet.kt), [`HeartRateSample`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/HeartRateSample.kt), [`WorkoutSummary`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/WorkoutSummary.kt), [`WorkoutType`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/WorkoutType.kt), [`WorkoutStatus`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/ActiveWorkout.kt), [`DataSource`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/model/HeartRateSample.kt)
- **Repository interfaces** — [`WorkoutRepository`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/repository/WorkoutRepository.kt), [`HeartRateRepository`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/repository/HeartRateRepository.kt), [`WorkoutSetRepository`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/repository/WorkoutSetRepository.kt)
- **Use cases** — [`StartWorkoutUseCase`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/usecase/StartWorkoutUseCase.kt), [`FinishWorkoutUseCase`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/usecase/FinishWorkoutUseCase.kt), [`GetWorkoutHistoryUseCase`](./core/domain/src/commonMain/kotlin/com/jepittman/tempo/core/domain/usecase/GetWorkoutHistoryUseCase.kt)

Because this layer has zero framework dependencies it is trivially unit testable without a device or emulator.

### [`core:data`](./core/data)
Repository implementations. Reads from SQLDelight, writes through to Ktor on sync. Implements offline-first: writes always go to the local DB first, sync is a background concern.

### [`core:database`](./core/database)
SQLDelight schema definitions and generated type-safe query code. Shared across Android and iOS via the SQLDelight runtime and platform-specific drivers.

### [`core:network`](./core/network)
Ktor client configuration, API interface definitions, and serialization setup. Android uses the OkHttp engine, iOS uses the Darwin engine — both wired through `expect`/`actual`.

### [`feature:workout`](./feature/workout)
Active workout screen. Consumes a real-time `Flow` of `HeartRateSample` from the connected wearable. Drives `ActiveWorkout` state through MVI.

### [`feature:history`](./feature/history)
Past workout list and detail screens. Displays `WorkoutSummary` items from `GetWorkoutHistoryUseCase`.

### [`feature:profile`](./feature/profile)
User settings, connected device management, and personal goals.

### [`server`](./server)
Ktor backend. Handles user accounts and workout sync. Shares model serialization with the client via `:core:domain`.

### [`composeApp`](./composeApp)
Android and iOS entry points. Wires feature modules and DI. On Android this is also the Wearable Data Layer receiver for Pixel Watch events.

### [`wearApp`](./wearApp)
Wear OS entry point. Uses Health Services API for workout tracking and heart rate. Pushes data to the phone via `DataClient`.

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

ActiveWorkout                // runtime-only, not persisted directly
  workout: Workout
  status: WorkoutStatus      // Preparing | Active | Paused | Completed
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
| UI (phone) | Compose Multiplatform |
| UI (Wear OS) | Wear Compose |
| UI (Apple Watch) | SwiftUI |
| Async | Kotlin Coroutines + Flow |
| Local DB | SQLDelight |
| Networking | Ktor client |
| Backend | Ktor server (Netty) |
| Serialization | kotlinx.serialization |
| Date/Time | kotlinx.datetime |
| Watch → Phone (Android) | Wearable Data Layer API |
| Watch → Phone (iOS) | WatchConnectivity |
| Heart rate (Wear OS) | Health Services API |
| Heart rate (watchOS) | HealthKit |

---

## Build & run

**Android**
```shell
./gradlew :composeApp:assembleDebug
```

**iOS** — open `/iosApp` in Xcode and run, or use the IDE run configuration.

**Wear OS**
```shell
./gradlew :wearApp:assembleDebug
```

**Server**
```shell
./gradlew :server:run
```

---

## Status

| Layer | Status |
|---|---|
| [`core:domain`](./core/domain) — models, repositories, use cases | Done |
| [`core:database`](./core/database) — SQLDelight schema | Not started |
| [`core:data`](./core/data) — repository implementations | Not started |
| [`core:network`](./core/network) — Ktor client | Not started |
| [`feature:workout`](./feature/workout) | Not started |
| [`feature:history`](./feature/history) | Not started |
| [`feature:profile`](./feature/profile) | Not started |
| [`server`](./server) | Not started |
| Wear OS data layer integration | Not started |
| watchOS / WatchConnectivity | Not started |
