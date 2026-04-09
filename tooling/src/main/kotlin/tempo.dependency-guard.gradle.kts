import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension

// In Gradle 8.14.3, tasks.register() eagerly realizes tasks inside the plugin-apply build
// operation, running the configuration block (setParams) before the script body can execute.
// Work-around: register a configureEach hook *before* applying DG so it fires ahead of
// DG's own setParams validation and pre-populates extension.configurations.
tasks.configureEach {
    if (name == "dependencyGuard" || name == "dependencyGuardBaseline") {
        project.extensions.findByType(DependencyGuardPluginExtension::class.java)
            ?.configuration("androidRuntimeClasspath")
    }
}

pluginManager.apply("com.dropbox.dependency-guard")
