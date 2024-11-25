import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import fr.il_totore.manadrop.Permission

plugins {
    java
    id("fr.il_totore.manadrop") version "0.4.3"

    id("com.github.ben-manes.versions") version "0.51.0"
    id("se.ascp.gradle.gradle-versions-filter") version "0.1.16"
    idea
}

group = "de.groovybyte.spigot.xcraftadvent"
version = "1.5.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    /*
    As Spigot-API depends on the BungeeCord ChatComponent-API,
    we need to add the Sonatype OSS repository, as Gradle,
    in comparison to maven, doesn't want to understand the ~/.m2
    directory unless added using mavenLocal(). Maven usually just gets
    it from there, as most people have run the BuildTools at least once.
    This is therefore not needed if you're using the full Spigot/CraftBukkit,
    or if you're using the Bukkit API.
    */
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")

    implementation("com.google.guava:guava:33.3.1-jre")

    val junitVersion = "5.11.3"
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
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
                permission("calender.ownAndShow")
            }

            permission {
                named("calendar.admin.showAny")
                description("Benötigt, um fremde Adventskalenders anzuzeigen")
                defaultType(Permission.DefaultType.GRANT_OP)
            }

            permission {
                named("calendar.ownAndShow")
                description("Benötigt, um Adventskalenders zu besitzen und anzuzeigen")
                defaultType(Permission.DefaultType.GRANT_PLAYER)
            }

            permission {
                named("calendar.openDoors")
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
        versionsFilter {
            exclusiveQualifiers.addAll("ea", "alpha", "beta", "b", "Final", "RELEASE", "RC", "RC\\d+")
        }
    }

    test {
        enableAssertions = true
        testLogging {
            showStandardStreams = true
        }
        useJUnitPlatform()
    }
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}
