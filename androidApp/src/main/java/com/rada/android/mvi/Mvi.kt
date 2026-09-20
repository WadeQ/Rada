package com.rada.android.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Marker for a user action or system event a screen can handle. One sealed hierarchy per feature. */
interface MviIntent

/** Marker for a screen's renderable state. Should be an immutable data class, one per feature. */
interface MviState

/** Marker for a one-shot side effect (navigation, a toast, a snackbar) — never re-read from state. */
interface MviEffect

/**
 * Base ViewModel every feature extends. Mirrors the shape of this pattern generally (state
 * as a single StateFlow, intents in, effects out as one-shot events) without depending on any
 * specific proprietary base class — this is a from-scratch, generic version for this project.
 *
 * Route screens collect [state] and [effects]; Content screens are pure functions of
 * [State] -> Unit that emit [Intent]s upward. Route screens own navigation and Scaffold;
 * Content screens own layout only and take no ViewModel reference directly.
 */
abstract class MviViewModel<Intent : MviIntent, State : MviState, Effect : MviEffect>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects: Flow<Effect> = _effects.receiveAsFlow()

    protected val currentState: State get() = _state.value

    protected fun setState(reducer: State.() -> State) {
        _state.value = _state.value.reducer()
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    /** Every feature implements this as the single entry point for user/system intents. */
    abstract fun onIntent(intent: Intent)
}
