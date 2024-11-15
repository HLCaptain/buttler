import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.local.room)
        implementation(projects.composeApp.domain)
        implementation(projects.shared)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.napier)
    }
}
