package to.sports.live

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import to.sports.live.navigation.DefaultRootComponent

fun MainViewController() = ComposeUIViewController {
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(LifecycleRegistry())
    )
    App(rootComponent = root)
}
