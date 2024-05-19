package illyan.butler

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import illyan.butler.di.getViewModelModule
import illyan.butler.utils.initNapier
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

fun main() = application {
    initNapier()
    startKoin {
        modules(defaultModule, getViewModelModule())
    }
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
