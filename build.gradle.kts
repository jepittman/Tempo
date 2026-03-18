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
    // Convention plugins from the tooling included build.
    // Declared here so they are on the classpath and can be applied to subprojects below.
    id("tempo.detekt") apply false
}

val detektFormattingDep = libs.detekt.formatting

subprojects {
    apply(plugin = "tempo.detekt")
    dependencies {
        "detektPlugins"(detektFormattingDep)
    }
}

// Aggregate task — safe to run before every commit.
// Automatically picks up detekt + test tasks from all subprojects as they are registered.
tasks.register("audit") {
    group = "verification"
    description = "Runs detekt and all unit tests across every module."
}

subprojects {
    tasks.whenTaskAdded {
        if (name == "detekt" || name == "allTests" || name == "testDebugUnitTest") {
            rootProject.tasks.named("audit").configure { dependsOn(this@whenTaskAdded) }
        }
    }
}