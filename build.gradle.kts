plugins {
    kotlin("jvm") version("1.9.0")
    `maven-publish`
}

group = "net.maew"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api(project(":console"))
}

tasks.test {
    useJUnitPlatform()
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
            groupId = "net.maew"
            artifactId = "quick-libs"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}

kotlin {
    jvmToolchain(8)
}
