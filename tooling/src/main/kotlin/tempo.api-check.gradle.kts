plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

// BCV tracks the public API surface of each KMP library module.
// On first use, run:  ./gradlew apiDump
// This generates a <module>/api/<module>.api file per-module that is committed
// to source control. Subsequent ./gradlew apiCheck (or audit) will fail if the
// public API changes without a corresponding apiDump + commit.
//
// To intentionally change the API, make your changes then run apiDump again.

// BCV's androidApi* tasks fail with variant-ambiguity errors when resolving transitive
// local KMP Android library dependencies in Gradle 8.14.3: those libraries publish
// androidApiElements with platformType='jvm' rather than 'androidJvm', producing
// multiple unranked candidates. Remove the Android tasks from the aggregate task
// dependency chains so they are never realised. JVM + KLib checks still cover the API.
afterEvaluate {
    val androidTaskNames = setOf("androidApiDump", "androidApiCheck", "androidApiBuild")
    listOf("apiDump", "apiCheck").forEach { aggregate ->
        tasks.findByName(aggregate)?.let { aggregateTask ->
            val filtered = aggregateTask.dependsOn.filter { dep ->
                val name: String? = when (dep) {
                    is Task -> (dep as Task).name
                    is TaskProvider<*> -> (dep as TaskProvider<*>).name
                    is String -> dep
                    else -> null
                }
                name !in androidTaskNames
            }
            aggregateTask.setDependsOn(filtered)
        }
    }
    // Also disable them so they are skipped if someone invokes them directly.
    tasks.matching { it.name in androidTaskNames }.configureEach { enabled = false }
}
