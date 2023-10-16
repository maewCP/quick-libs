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
    api(project(":console"))
    api(project(":file"))
    implementation("org.apache.commons:commons-lang3:3.12.0") // use in net.bramp.ffmpeg.progress.Progress.fps (ffmpeg-0.7.1-SNAPSHOT.jar)
    // FFMpeg Wrapper
    // implementation("net.bramp.ffmpeg:ffmpeg:0.7.0")
    api(files("libs/ffmpeg-0.7.1-SNAPSHOT.jar")) // implement side_data_list for rotation
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
            artifactId = "ffmpeg"
            version = "1.0-SNAPSHOT"

            from(components["java"])
            artifact("libs/ffmpeg-0.7.1-SNAPSHOT.jar")
        }
    }
}


kotlin {
    jvmToolchain(8)
}
