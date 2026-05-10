// === Демонстраційний проект до лекції з API та інтеграцій ===
// Кожна група залежностей коментована темою зі слайдів.

import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    // Слайди 4, 5 — Protobuf для gRPC. Генерує Kotlin/Java класи з .proto файлу.
    id("com.google.protobuf") version "0.9.4"
}

group = "this4you"
version = "0.0.1-SNAPSHOT"
description = "api-example"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // === Слайди 4, 6 — REST API через Spring MVC ===
    implementation("org.springframework.boot:spring-boot-starter-web")

    // === Слайд 2 — Валідація DTO (Bean Validation: @NotBlank, @Min, @Email) ===
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // === Слайд 7 — Security: filter chain, JWT/ApiKey фільтри ===
    implementation("org.springframework.boot:spring-boot-starter-security")

    // === Слайд 5 — WebSocket (live чат) ===
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // === Слайди 4, 9 — GraphQL + GraphiQL Playground ===
    implementation("org.springframework.boot:spring-boot-starter-graphql")

    // === Слайд 13 — Actuator: /actuator/health, /actuator/metrics (моніторинг) ===
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // === Слайд 11 — Message broker ===
    implementation("org.springframework.boot:spring-boot-starter-kafka")   // Kafka (+ autoconfiguration)

    // === Слайд 4 — SOAP (Spring Web Services, contract-first) ===
    implementation("org.springframework.boot:spring-boot-starter-web-services")
    implementation("wsdl4j:wsdl4j:1.6.3")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")
    implementation("org.glassfish.jaxb:jaxb-runtime")

    // === Слайд 9 — OpenAPI / Swagger UI ===
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // === Слайд 7 — JWT (JSON Web Token) ===
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // === Слайди 7, 12 — Rate limiting (token bucket) ===
    implementation("com.bucket4j:bucket4j_jdk17-core:8.14.0")

    // === Слайд 4 — gRPC (бінарний RPC через Protobuf) ===
    implementation("io.grpc:grpc-netty-shaded:1.68.1")
    implementation("io.grpc:grpc-protobuf:1.68.1")
    implementation("io.grpc:grpc-stub:1.68.1")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.5")
    // anotaation processor для grpc-java
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // === Слайд 13 — Тести ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // У Spring Boot 4 test slices розділені; для @AutoConfigureMockMvc потрібен webmvc-test
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// === Конфігурація protoc для gRPC ===
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.68.1"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/grpc",
                "build/generated/source/proto/main/grpckt",
                "build/generated/source/proto/main/kotlin"
            )
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
