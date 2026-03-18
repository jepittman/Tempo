import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    id("io.gitlab.arturbosch.detekt")
}

// detektPlugins dependency is added from the root build.gradle.kts where the version
// catalog type-safe accessor is natively available. This plugin owns only configuration.
extensions.configure<DetektExtension> {
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}
