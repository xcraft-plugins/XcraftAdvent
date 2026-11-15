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
            // Reverted to match the released 2.0.0-alpha2 JAR metadata
            authors("ShirleyNekoDev")
            description("Advent calendar plugin.")

            main("de.groovybyte.spigot.xcraftadvent.XcraftAdvent")
            // valid api-versions are 1.13 up to the running server version
            // (see the PaperMC plugin.yml docs); 26.3 matches the spigot-api
            apiVersion("26.3")

            command {
                named(PluginConstants.COMMAND_LABEL)
                aliases(*PluginConstants.COMMAND_ALIASES)
                description(PluginConstants.COMMAND_DESCRIPTION)
                usage(PluginConstants.COMMAND_USAGE)
                // Backward compatibility: the 2.0.0-alpha2 JAR used "calendar.ownAndShow"
                // as the command permission. Existing servers may have granted this exact
                // node via LuckPerms or similar. At runtime, the plugin checks both
                // calendar.own AND calendar.show individually (see CalendarCommandExecutor).
                permission(PluginConstants.PERMISSION_OWN_AND_SHOW)
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
        versions("26.3")
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
