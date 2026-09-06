pluginManagement {
    repositories {
        // mavenCentral 提前:aliyun 偶发不可达(2026-09-06 实测 000)时 KSP 等插件可从 central 兜底
        mavenCentral()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google()
    }
}
rootProject.name = "OnTime"
include(":app")
