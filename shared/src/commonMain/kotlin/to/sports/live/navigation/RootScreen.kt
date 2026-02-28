package to.sports.live.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import to.sports.live.presentation.dashboard.DashboardScreen
import to.sports.live.presentation.sync_splash.SplashScreen

@Composable
fun RootScreen(component: RootComponent) {
    Children(
        stack = component.stack,
        animation = stackAnimation()
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.SyncSplashChild -> SplashScreen(child.component)
            is RootComponent.Child.DashboardChild -> DashboardScreen(child.component)
        }
    }
}
