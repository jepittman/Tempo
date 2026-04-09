plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dependencyGuard) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.binaryCompatibilityValidator) apply false
    // Convention plugins from the tooling included build.
    // Declared here so they are on the classpath and can be applied to subprojects below.
    id("tempo.detekt") apply false
    id("tempo.dependency-guard") apply false
    id("tempo.kover") apply false
    id("tempo.api-check") apply false
}

val detektFormattingDep = libs.detekt.formatting

subprojects {
    apply(plugin = "tempo.detekt")
    dependencies {
        "detektPlugins"(detektFormattingDep)
    }
}

// Aggregate task — safe to run before every commit.
// Automatically picks up quality tasks from all subprojects as they are registered.
// Tasks included:
//   detekt              — lint + ktlint formatting (all modules)
//   allTests            — KMP unit tests (all modules)
//   testDebugUnitTest   — Android unit tests (app modules)
//   dependencyGuard     — locked dependency baseline check (library modules)
//   koverXmlReportJvm   — coverage report from JVM test execution (core modules)
//   apiCheck            — binary API compatibility check (library modules)
tasks.register("audit") {
    group = "verification"
    description = "Runs detekt, all unit tests, dependency guard, coverage, and API checks across every module."
}

subprojects {
    tasks.whenTaskAdded {
        if (name in setOf(
                "detekt",
                "allTests",
                "testDebugUnitTest",
                "dependencyGuard",
                "koverXmlReportJvm",
                "apiCheck",
            )
        ) {
            rootProject.tasks.named("audit").configure { dependsOn(this@whenTaskAdded) }
        }
    }
}