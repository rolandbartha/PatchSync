plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
    id("io.freefair.lombok") version "8.11"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

group = "net.rolandbrt.patchsync"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")

    implementation("org.java-websocket:Java-WebSocket:1.5.7")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes("Main-Class" to "net.rolandbrt.patchsync.App")
    }
    finalizedBy("setup")
}

tasks.register<Zip>("setup") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations.runtimeClasspath.get().files.iterator().forEach {
        println(it.path)
        from(zipTree(it))
    }
    finalizedBy("deployToTest")
}

tasks.register<Copy>("deployToTest") {
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.get().archiveFile)
    into("C:\\Users\\Roli\\Desktop\\New folder (7)\\PatchSync")
    rename { "custom.jar" }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}