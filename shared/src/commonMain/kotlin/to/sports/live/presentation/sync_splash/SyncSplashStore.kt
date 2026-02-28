package to.sports.live.presentation.sync_splash

import com.arkivanov.mvikotlin.core.store.Store
import to.sports.live.presentation.sync_splash.SyncSplashStore.Intent
import to.sports.live.presentation.sync_splash.SyncSplashStore.State
import to.sports.live.presentation.sync_splash.SyncSplashStore.Label

interface SyncSplashStore : Store<Intent, State, Label> {
    sealed class Intent {
        data object StartSync : Intent()
    }

    data class State(
        val isSyncing: Boolean = false
    )

    sealed class Label {
        data object SyncCompleted : Label()
    }
}
