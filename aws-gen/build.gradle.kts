import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    id("org.jetbrains.dokka") version "0.9.18"
}

group = "io.futz.aws.generator"
version = "1.0.0"

repositories {
    jcenter()
    mavenCentral()
}

allprojects {
    apply {
        plugin("org.jetbrains.dokka")
        plugin("kotlin")
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation("junit:junit:4.12")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }

    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }

}

subprojects {

    repositories {
        jcenter()
        mavenCentral()
    }

    val dokka by tasks.getting(DokkaTask::class) {
        outputDirectory = "$buildDir/dokka"
        jdkVersion = 8
        reportUndocumented = true
    }

    defaultTasks("dokka")
}

project(":client") {
    dependencies {
        compile(project(":synthesizer"))
        compile("software.amazon.awssdk:cloudformation:2.5.59")
    }
}

project(":core") {
    dependencies {
        compile("com.fasterxml.jackson.core:jackson-annotations:2.9.8")
    }
}

project(":generated") {
    dependencies {
        compile(project(":core"))
    }
}

project(":generator") {
    dependencies {
        compile(project(":generated"))
        compile(project(":parser"))
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
        compile("com.squareup:kotlinpoet:1.3.0")
    }
}

project(":parser") {
    dependencies {
        compile(project(":generated"))
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
        compile("org.jsoup:jsoup:1.12.1")
    }
}

project(":synthesizer") {
    dependencies {
        compile(project(":generated"))
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    }
}


