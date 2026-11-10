import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestLogEvent
import fr.il_totore.manadrop.Permission

plugins {
    java
    alias(libs.plugins.spigot.manadrop)

    alias(libs.plugins.versions.updater)
    idea
}

group = "de.groovybyte.spigot.xcraftadvent"
version = "1.5.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    mavenLocal()
}

dependencies {
    compileOnly(libs.spigot)

    compileOnly(libs.google.guava)
    compileOnly(libs.apache.commons.lang3)

    // spigot is compileOnly for the plugin jar, but the tests need the API
    testImplementation(libs.spigot)

    testImplementation(platform(libs.junit.platform))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

tasks {
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    spigot {
        desc {
            authors("Salami555")
            description("Adventskalender-Plugin")

            main("de.groovybyte.spigot.xcraftadvent.XcraftAdvent")
            apiVersion("1.21")

            command {
                named("adventskalender")
                aliases("xmas", "advent")
                description("Öffnet den Adventskalender")
                usage("/advent")
                // opening the calendar additionally requires calendar.own;
                // both are checked at runtime in CalendarCommandExecutor
                permission("calendar.show")
            }

            permission {
                named("calendar.own")
                description("Benötigt, um einen Adventskalender zu besitzen")
                defaultType(Permission.DefaultType.GRANT_PLAYER)
            }

            permission {
                named("calendar.show")
                description("Benötigt, um den Adventskalender anzuzeigen")
                defaultType(Permission.DefaultType.GRANT_PLAYER)
            }

            permission {
                named("calendar.doors.open")
                description("Ermöglicht das Öffnen von Türchen")
                defaultType(Permission.DefaultType.GRANT_OP)
            }
        }
    }

    buildTools {
        versions("1.21")
        workDir = projectDir.resolve("server/")
    }

    withType<DependencyUpdatesTask> {
        gradleReleaseChannel = "current"
    }

    test {
        useJUnitPlatform()

        testLogging {
            showStandardStreams = true
            events(
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.FAILED
            )
        }
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
