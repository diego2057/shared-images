import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("jacoco")
	id("org.springframework.boot") version "2.7.6"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	id("org.sonarqube") version "3.5.0.2730"
	id("org.jmailen.kotlinter") version "3.10.0"
	id("com.gorylenko.gradle-git-properties") version "2.4.1"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("kapt") version "1.6.21"
}

group = "com.tul.shared"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()

	maven {
		val host = project.property("nexusHost")
		url = uri("$host/repository/maven-public/")
		isAllowInsecureProtocol = true
		credentials {
			username = System.getenv("NEXUS_USER")
			password = System.getenv("NEXUS_PASS")
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.projectreactor.kafka:reactor-kafka:1.3.12")
	implementation("org.springframework.kafka:spring-kafka")

	developmentOnly("org.springframework.boot:spring-boot-devtools")

	api("org.mapstruct:mapstruct:1.4.1.Final")
	kapt("org.mapstruct:mapstruct-processor:1.4.1.Final")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("de.bwaldvogel:mongo-java-server:1.36.0")
	testImplementation("com.github.tomakehurst:wiremock-jre8:2.25.1")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("com.github.javafaker:javafaker:0.14") {
		exclude(group = "org.yaml", module = "snakeyaml")
	}

	// ApiDocumentation
	implementation("com.tul:common-api-doc:1.1.0")
}

extra["springCloudVersion"] = "2021.0.5"

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
		allWarningsAsErrors = true
	}
}

tasks.getByName<Jar>("jar") {
	enabled = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
	}
}

sonarqube {
	properties {
		property("sonar.sources", "src/main/kotlin")
		property("sonar.tests", "src/test/kotlin")
		property("sonar.verbose", "true")
		property("sonar.qualitygate.wait", "true")
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
		property("sonar.coverage.exclusions", "**/GlobalRestToKafkaApplication*.*,**/*Mapper*.*,**/dto/*,**/model/*")
	}
}

tasks.withType<JacocoCoverageVerification> {
	afterEvaluate {
		classDirectories.setFrom(files(classDirectories.files.map {
			fileTree(it).apply {
				exclude("**/SharedImagesApplication*.*", "**/*Mapper*.*", "**/dto/**", "**/model/**")
			}
		}))
	}
}

tasks.withType<JacocoReport> {
	afterEvaluate {
		classDirectories.setFrom(files(classDirectories.files.map {
			fileTree(it).apply {
				exclude("**/SharedImagesApplication*.*", "**/*Mapper*.*", "**/dto/**", "**/model/**")
			}
		}))
	}
}
