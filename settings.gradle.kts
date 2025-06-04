pluginManagement {
    repositories {
        // Android や Google 系のプラグインに対応
        google()
        // Kotlin やその他主要ライブラリ
        mavenCentral()
        // Gradle プラグイン用
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // build.gradle での repositories 使用を禁止（厳格運用）
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Android 関連ライブラリ
        google()
        // Java/Kotlin ライブラリ
        mavenCentral()
    }
}

rootProject.name = "demo0430"
include(":app")
