package to.sports.live.presentation.sync_splash

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import to.sports.live.presentation.sync_splash.SyncSplashStore.Intent
import to.sports.live.presentation.sync_splash.SyncSplashStore.Label
import to.sports.live.presentation.sync_splash.SyncSplashStore.State

internal class SyncSplashStoreFactory(private val storeFactory: StoreFactory) {

    fun create(): SyncSplashStore =
        object : SyncSplashStore, Store<Intent, State, Label> by storeFactory.create(
            name = "SyncSplashStore",
            initialState = State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed class Msg {
        data class SyncStarted(val isSyncing: Boolean) : Msg()
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Nothing, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.StartSync -> startSync()
            }
        }

        private fun startSync() {
            scope.launch {
                dispatch(Msg.SyncStarted(true))
                // Simulate server sync delay of 5 seconds
                delay(5000)
                dispatch(Msg.SyncStarted(false))
                publish(Label.SyncCompleted)
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.SyncStarted -> copy(isSyncing = msg.isSyncing)
            }
    }
}
