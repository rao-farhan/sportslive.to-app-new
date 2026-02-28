package to.sports.live.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import to.sports.live.presentation.dashboard.DashboardComponent
import to.sports.live.presentation.sync_splash.SyncSplashComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        class SyncSplashChild(val component: SyncSplashComponent) : Child()
        class DashboardChild(val component: DashboardComponent) : Child()
    }
}

class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.SyncSplash,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            Config.SyncSplash -> RootComponent.Child.SyncSplashChild(
                SyncSplashComponent(
                    componentContext = componentContext,
                    onFinished = {
                        navigation.replaceAll(Config.Dashboard)
                    }
                )
            )
            Config.Dashboard -> RootComponent.Child.DashboardChild(
                DashboardComponent(
                    componentContext = componentContext
                )
            )
        }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object SyncSplash : Config
        @Serializable
        data object Dashboard : Config
    }
}
