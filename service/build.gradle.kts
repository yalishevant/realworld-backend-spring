import org.graalvm.buildtools.gradle.tasks.BuildNativeImageTask
import org.gradle.internal.os.OperatingSystem
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.graalvm)
    id("realworld.java-conventions")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

dependencies {
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    implementation(project(":service-bus"))
    implementation(project(":service-api"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation(libs.springdoc.starter.webmvc.ui)

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.liquibase:liquibase-core")

    testAnnotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    intTestAnnotationProcessor("org.projectlombok:lombok")
    intTestCompileOnly("org.projectlombok:lombok")
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set(rootProject.name)
            buildArgs.add("--verbose")
        }
    }
}

springBoot {
    buildInfo {
        properties {
            artifact = rootProject.name
        }
    }
}

tasks {
    named<BootJar>("bootJar") {
        archiveFileName.set("${rootProject.name}-${archiveVersion.get()}.${archiveExtension.get()}")
    }

    named<BootBuildImage>("bootBuildImage") {
        val registry = System.getenv("CR_REGISTRY")!!
        val namespace = System.getenv("CR_NAMESPACE")!!
        imageName = "${registry}/${namespace}/${rootProject.name}:${project.version}"
        publish = true
        tags = setOf("${registry}/${namespace}/${rootProject.name}:latest")
        docker {
            publishRegistry {
                url = System.getenv("CR_REGISTRY")
                username = System.getenv("CR_USERNAME")
                password = System.getenv("CR_PASSWORD")
            }
        }
    }

    val writeArtifactFile by registering {
        doLast {
            val outputDirectory = getByName<BuildNativeImageTask>("nativeCompile").outputDirectory
            outputDirectory.get().asFile.mkdirs()
            outputDirectory.file("gradle-artifact.txt")
                .get().asFile
                .writeText("${rootProject.name}-${project.version}-${platform()}")
        }
    }

    named("nativeCompile") {
        finalizedBy(writeArtifactFile)
    }

}

fun platform(): String {
    val os = OperatingSystem.current()
    val arc = System.getProperty("os.arch")
    return when {
        OperatingSystem.current().isWindows -> "windows-${arc}"
        OperatingSystem.current().isLinux -> "linux-${arc}"
        OperatingSystem.current().isMacOsX -> "darwin-${arc}"
        else -> os.nativePrefix
    }
}
