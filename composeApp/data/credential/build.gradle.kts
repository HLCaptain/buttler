plugins {
    alias(libs.plugins.illyan.butler.kotlinMultiplatformLibrary)
    alias(libs.plugins.illyan.butler.koinForKotlinMultiplatform)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.shared.model)

        implementation(projects.composeApp.core.local)
        implementation(projects.composeApp.core.network)
        implementation(projects.composeApp.domain)
        implementation(projects.composeApp.core.utils)

        implementation(libs.kotlinx.coroutines)
        implementation(libs.napier)
    }
}