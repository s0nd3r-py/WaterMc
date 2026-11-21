plugins {
    java
}

group = "one.water"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Built against the public Paper API only, so the same jar runs on Leaf and on Water
    if (rootProject.findProject(":watermc-api") != null) {
        compileOnly(project(":watermc-api"))
    } else {
        compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}
