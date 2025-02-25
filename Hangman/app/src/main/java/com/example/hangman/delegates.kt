import androidx.compose.runtime.*
import kotlin.reflect.KProperty

// Property delegation for State
operator fun <T> State<T>.getValue(thisObj: Any?, property: KProperty<*>): T = value

// Property delegation for MutableState
operator fun <T> MutableState<T>.setValue(thisObj: Any?, property: KProperty<*>, value: T) {
    this.value = value
}