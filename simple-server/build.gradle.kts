import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  var kotlinVersion: String by extra
  kotlinVersion = "1.2.41"

  repositories {
    jcenter()
    mavenCentral()
  }
  dependencies {
    classpath(kotlin("gradle-plugin", kotlinVersion))
    // see: http://imperceptiblethoughts.com/shadow/
    classpath("com.github.jengelman.gradle.plugins:shadow:2.0.4")
  }
}

plugins {
  application
  java
}

group = "io.futz"
version = "1.0-SNAPSHOT"

apply {
  plugin("application")
  plugin("kotlin")
  plugin("com.github.johnrengelman.shadow")
}

application {
  // remember to append "Kt"
  mainClassName = "io.futz.server.ServerKt"
}

val kotlinVersion: String by extra

repositories {
  jcenter()
  mavenCentral()
}

dependencies {
  compile(kotlin("stdlib-jdk8", kotlinVersion))
  testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
  sourceCompatibility = JavaVersion.VERSION_1_9
}
tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

