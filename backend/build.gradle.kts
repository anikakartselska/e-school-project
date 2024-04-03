import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.springframework.boot") version "2.7.3"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.spring") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "com.nevexis"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation(project(":frontend"))
    implementation(project(":jooq-db"))
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    developmentOnly("org.springframework.boot:spring-boot-devtools")


    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jooq:jooq:3.15.5")
    implementation("org.jooq:jooq-kotlin:3.15.5")

    implementation("org.liquibase:liquibase-core")
    runtimeOnly("com.oracle.database.jdbc:ojdbc8")

    implementation("org.apache.poi:poi:5.2.0")
    implementation("org.apache.poi:poi-ooxml:5.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.testcontainers:testcontainers:1.17.3")
    testImplementation("org.testcontainers:oracle-xe:1.17.3")
    testImplementation("org.testcontainers:junit-jupiter:1.17.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val processResources by tasks.existing {
    dependsOn(copyFrontend)
}

val bootJar by tasks.existing(Jar::class) {
    this.archiveBaseName.set("new-demo-rc")
    this.archiveVersion.set("")
}

val copyFrontend by tasks.registering(Copy::class) {
    dependsOn(":frontend:buildFrontend")
    val inputDir = File("${rootDir}/frontend/dist")
    val outputDir = File("${rootDir}/backend/src/main/resources/static")
    outputDir.apply {
        if (this.exists()) {
            deleteRecursively()
        }
        mkdir()
    }
    from(inputDir)
    into(outputDir)
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
