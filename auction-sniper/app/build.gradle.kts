/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.7/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    implementation("org.igniterealtime.smack:smack-core:4.4.8")
    // Over tcp connection
    implementation("org.igniterealtime.smack:smack-tcp:4.4.8")
     // Optional for XMPP-IM (RFC 6121) support (Roster, Threaded Chats, …)
    implementation("org.igniterealtime.smack:smack-im:4.4.8")
    // Optional for XMPP extensions support
    implementation("org.igniterealtime.smack:smack-extensions:4.4.8")
    implementation("org.igniterealtime.smack:smack-xmlparser-xpp3:4.4.8") // or the latest version
    implementation("org.igniterealtime.smack:smack-resolver-minidns:4.4.8")
    implementation("org.igniterealtime.smack:smack-java8:4.4.8")

    testImplementation("com.googlecode.windowlicker:windowlicker-swing:r268")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.auction.Main"
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.auction.Main"
    }
}
