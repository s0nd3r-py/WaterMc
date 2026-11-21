plugins { java }
repositories { mavenCentral() }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

val locationFile = layout.buildDirectory.file("runtime-location.txt").get().asFile
val runtimeDirectory = if (providers.gradleProperty("runtimeDir").isPresent) {
    file(providers.gradleProperty("runtimeDir").get())
} else {
    require(locationFile.isFile) { "Сначала выполните benchmarks/micro/prepare-runtime.py" }
    file(locationFile.readText().trim())
}
require(runtimeDirectory.isDirectory) { "Не найден подготовленный runtime: " + runtimeDirectory }

dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    implementation(fileTree(runtimeDirectory) { include("**/*.jar") })
}
sourceSets {
    main { java.srcDir("../../watermc-server/src/test/java/org/watermc/ticks/reference") }
}
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
val benchmarkClasspath = sourceSets.main.get().runtimeClasspath
val classpathFile = layout.buildDirectory.file("runtime-classpath.txt")
tasks.register("writeRuntimeClasspath") {
    dependsOn(tasks.classes)
    inputs.files(benchmarkClasspath)
    outputs.file(classpathFile)
    doLast { classpathFile.get().asFile.writeText(benchmarkClasspath.asPath) }
}
