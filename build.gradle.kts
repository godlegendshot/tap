plugins {
    idea
    `maven-publish`
    kotlin("jvm") version Dependency.Kotlin.Version
    kotlin("plugin.serialization") version Dependency.Kotlin.Version apply false
    id("org.jetbrains.dokka") version Dependency.Dokka.Version apply false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:${Dependency.Paper.Version}-R0.1-SNAPSHOT")

        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.6.2")

        testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}

listOf("api", "core").forEach { projectName ->
    project(":${rootProject.name}-$projectName") {
        apply(plugin = "org.jetbrains.dokka")

        tasks {
            create<Jar>("sourcesJar") {
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }

            create<Jar>("dokkaJar") {
                archiveClassifier.set("javadoc")
                dependsOn("dokkaHtml")

                from("$buildDir/dokka/html/") {
                    include("**")
                }
            }
        }
    }
}

idea {
    module {
        excludeDirs.add(file(".server"))
        excludeDirs.addAll(allprojects.map { it.buildDir })
        excludeDirs.addAll(allprojects.map { it.file(".gradle") })
    }
}

listOf("api", "core").forEach { projectName ->
    project(":${rootProject.name}-$projectName") {
        apply(plugin = "maven-publish")

        afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        groupId = project.group.toString()
                        artifactId = "${rootProject.name}-$projectName"
                        version = project.version.toString()
                        from(components["java"])

                        artifact(tasks["sourcesJar"])
                        artifact(tasks["dokkaJar"])
                    }
                }
            }
        }
    }
}
