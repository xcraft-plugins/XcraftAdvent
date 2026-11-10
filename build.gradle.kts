import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestLogEvent
import fr.il_totore.manadrop.Permission
import de.groovybyte.spigot.xcraftadvent.PluginConstants

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

// The shared command/permission constants live in buildSrc so this build
// script can reference them; adding the same source directory to the main
// source set compiles the identical class into the plugin jar as well.
sourceSets {
    main {
        java {
            srcDir("buildSrc/src/main/java")
        }
    }
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
                named(PluginConstants.COMMAND_LABEL)
                aliases(*PluginConstants.COMMAND_ALIASES)
                description(PluginConstants.COMMAND_DESCRIPTION)
                usage(PluginConstants.COMMAND_USAGE)
                // opening the calendar additionally requires calendar.own;
                // both are checked at runtime in CalendarCommandExecutor
                permission(PluginConstants.PERMISSION_SHOW_CALENDAR)
            }

            permission {
                named(PluginConstants.PERMISSION_OWN_CALENDAR)
                description(PluginConstants.PERMISSION_OWN_CALENDAR_DESCRIPTION)
                defaultType(Permission.DefaultType.GRANT_PLAYER)
            }

            permission {
                named(PluginConstants.PERMISSION_SHOW_CALENDAR)
                description(PluginConstants.PERMISSION_SHOW_CALENDAR_DESCRIPTION)
                defaultType(Permission.DefaultType.GRANT_PLAYER)
            }

            permission {
                named(PluginConstants.PERMISSION_OPEN_CALENDAR_DOORS)
                description(PluginConstants.PERMISSION_OPEN_CALENDAR_DOORS_DESCRIPTION)
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
