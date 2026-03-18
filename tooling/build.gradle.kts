plugins {
    `kotlin-dsl`
}

dependencies {
    // Expose plugin classpaths so convention plugins can apply them by id.
    implementation(libs.agp.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.composeCompiler.gradlePlugin)
    implementation(libs.detekt.gradlePlugin)

    // Workaround for https://github.com/gradle/gradle/issues/15383 —
    // makes the version catalog type-safe accessors (LibrariesForLibs) available
    // inside precompiled script plugins in this included build.
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
