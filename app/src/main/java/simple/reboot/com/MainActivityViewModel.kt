package simple.reboot.com

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel() {
    var uiState by mutableStateOf(UiState())
        private set

    fun areWeRooted() {
        viewModelScope.launch {
            val isRoot = withContext(Dispatchers.IO) { Shell.getShell().isRoot }
            uiState = uiState.copy(areWeRooted = isRoot)
        }
    }

    fun runCommand(command: String) {
        Shell.cmd(command).submit()
    }
}

data class UiState(
    val areWeRooted: Boolean? = null
)