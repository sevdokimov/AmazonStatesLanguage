import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.intellij.platform.grammarkit")
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    testImplementation(libs.junit)

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2024.3.7.1")
        testFramework(TestFrameworkType.Platform)

        // Add plugin dependencies for compilation here, for example:
        bundledPlugins(listOf("com.intellij.modules.json", "com.intellij.jsonpath"))

        plugins("cz.tix.jsonata:0.1.0")

        grammarKit()
    }
}

tasks.generateLexer {
    sourceFile.set(file("src/main/kotlin/com/ess/asl/ilang/ilang.flex"))
}

tasks.generateParser {
    sourceFile.set(file("src/main/grammar/asl.ilang.bnf"))
}

sourceSets {
    main {
        java {
            srcDirs(
                layout.buildDirectory.dir("generated/sources/grammarkit-lexer/java/main"),
                layout.buildDirectory.dir("generated/sources/grammarkit-parser/java/main")
            )
        }
    }
}