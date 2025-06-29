plugins {
    kotlin("jvm") version "1.7.10"
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:4.9.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.code.gson:gson:2.10.1")
}

repositories {
    mavenCentral()
}