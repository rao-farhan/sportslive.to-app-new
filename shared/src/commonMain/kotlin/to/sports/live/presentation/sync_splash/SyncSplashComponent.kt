package to.sports.live.presentation.sync_splash

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import to.sports.live.util.componentScope

class SyncSplashComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit
) : ComponentContext by componentContext {

    private val store = instanceKeeper.getStore {
        SyncSplashStoreFactory(DefaultStoreFactory()).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SyncSplashStore.State> = store.stateFlow

    init {
        store.labels
            .onEach { label ->
                when (label) {
                    SyncSplashStore.Label.SyncCompleted -> onFinished()
                }
            }
            .launchIn(componentScope())

        store.accept(SyncSplashStore.Intent.StartSync)
    }
}
