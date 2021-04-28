import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("jacoco")
	id("org.springframework.boot") version "2.4.1"
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
	id("org.sonarqube") version "3.0"
	id("org.jmailen.kotlinter") version "3.3.0"
	kotlin("jvm") version "1.4.21"
	kotlin("plugin.spring") version "1.4.21"
	kotlin("kapt") version "1.4.20"
}

group = "com.tul.shared"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.projectreactor.kafka:reactor-kafka:1.3.1")
	implementation("org.springframework.kafka:spring-kafka")
	implementation ("io.springfox:springfox-boot-starter:3.0.0")
	implementation("io.sentry:sentry-spring-boot-starter:4.3.0")
	implementation("io.sentry:sentry-logback:4.3.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	api("org.mapstruct:mapstruct:1.4.1.Final")
	kapt("org.mapstruct:mapstruct-processor:1.4.1.Final")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("de.bwaldvogel:mongo-java-server:1.36.0")
	testImplementation("com.github.tomakehurst:wiremock-jre8:2.25.1")
	testImplementation("io.projectreactor:reactor-test")
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

tasks.jacocoTestReport{
	reports {
		xml.isEnabled = true
	}
}

sonarqube{
	properties {
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
	}
}
