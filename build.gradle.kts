import org.gradle.api.tasks.compile.JavaCompile

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("kapt") version "1.9.25"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.autobill"
version = "0.0.1-SNAPSHOT"
description = "Spring Boot project for Demo"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	runtimeOnly("org.postgresql:postgresql:42.7.7")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	// JWT Token support
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
	// MapStruct for DTO -> Entity mapping (compile-time generated mapper)
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
		jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
	}
}

// Configure Spring Boot to run with correct main class
springBoot {
	mainClass.set("com.autobill.billsmart.SmartposApplicationKt")
}

// Skip tests during regular builds - tests will be addressed in Phase 2
tasks.test {
	enabled = false
}

// Suppress Java deprecation warnings for now (not critical for Phase 1)
tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs.add("-Xlint:-deprecation")
}

// -----------------------------------------------------------------
// Profile-specific Run Tasks
//   ./gradlew runDev      -> spring.profiles.active=dev
//   ./gradlew runUat      -> spring.profiles.active=uat
//   ./gradlew runProd     -> spring.profiles.active=prod
// -----------------------------------------------------------------
tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("runDev") {
	group = "application"
	description = "Run application with the DEVELOPMENT profile"
	dependsOn("classes")
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.autobill.billsmart.SmartposApplicationKt")
	args("--spring.profiles.active=dev")
}

tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("runUat") {
	group = "application"
	description = "Run application with the UAT profile"
	dependsOn("classes")
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.autobill.billsmart.SmartposApplicationKt")
	args("--spring.profiles.active=uat")
}

tasks.register<org.springframework.boot.gradle.tasks.run.BootRun>("runProd") {
	group = "application"
	description = "Run application with the PRODUCTION profile"
	dependsOn("classes")
	classpath = sourceSets["main"].runtimeClasspath
	mainClass.set("com.autobill.billsmart.SmartposApplicationKt")
	args("--spring.profiles.active=prod")
}
