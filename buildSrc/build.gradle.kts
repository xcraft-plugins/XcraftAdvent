plugins {
    `java-library`
}

// buildSrc classes run on the Gradle daemon JVM, so this toolchain must not
// target a newer JDK than the one Gradle itself runs on.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
