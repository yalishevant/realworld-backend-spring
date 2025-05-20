pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        gradlePluginPortal(); mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "realworld-backend-spring"
include("service-bus")
include("service-api")
include("service")
