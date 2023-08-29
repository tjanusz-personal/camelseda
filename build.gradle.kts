import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.3"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.apache.camel.springboot:camel-spring-boot-starter:${project.ext["camel.version"]}")
	
	implementation("org.springframework.boot:spring-boot-actuator:${project.ext["spring.boot.version"]}")
	implementation("org.springframework.boot:spring-boot-starter:${project.ext["spring.boot.version"]}")
	implementation("org.springframework.boot:spring-boot-starter-web:${project.ext["spring.boot.version"]}")
	implementation("org.springframework.boot:spring-boot-starter-tomcat:${project.ext["spring.boot.version"]}")
	implementation("org.springframework.boot:spring-boot-starter-logging:${project.ext["spring.boot.version"]}")
	implementation("org.springframework.boot:spring-boot-actuator-autoconfigure:${project.ext["spring.boot.version"]}")
	
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("org.apache.camel:camel-management:${project.ext["camel.version"]}")
	implementation("org.apache.camel:camel-servlet:${project.ext["camel.version"]}")

	implementation("org.apache.tomcat.embed:tomcat-embed-core:${project.ext["tomcat-embed.version"]}")
	implementation("org.apache.tomcat.embed:tomcat-embed-websocket:${project.ext["tomcat-embed.version"]}")

	implementation("com.google.guava:guava:${project.ext["guava.version"]}")

	// https://mvnrepository.com/artifact/jakarta.inject/jakarta.inject-api
	implementation("jakarta.inject:jakarta.inject-api:2.0.1")
	
	implementation("com.fasterxml.jackson.core:jackson-core:${project.ext["jackson.version"]}")
	implementation("com.fasterxml.jackson.core:jackson-databind:${project.ext["jackson.databind.version"]}")
	
}

springBoot {
	// https://discuss.gradle.org/t/gradle-kotlin-build-gradle-kts-with-cpp-application-plugin/31030
	mainClass.value( "com.example.camelseda.CamelsedaApplication" )
}


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
