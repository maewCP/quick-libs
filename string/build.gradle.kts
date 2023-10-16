plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "net.maew.quick-libs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.maew.quick-libs"
            artifactId = "string"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}

kotlin {
    jvmToolchain(8)
}
