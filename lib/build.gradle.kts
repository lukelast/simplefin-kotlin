plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `java-library`
    `maven-publish`
}

group = "com.github.lukelast"
version = "1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlin.serialization)
    api(libs.ktor.client)

    implementation(libs.ktor.client.cio)
    
    // Test dependencies
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform)
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "simplefin-kotlin"
        }
    }
}
