package to.sports.live

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import to.sports.live.navigation.RootComponent
import to.sports.live.navigation.RootScreen

@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme {
        RootScreen(rootComponent)
    }
}
