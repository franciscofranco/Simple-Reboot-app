package simple.reboot.com

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers

class MainActivityViewModel : ViewModel() {
    fun areWeRooted() = liveData(Dispatchers.IO) {
        emit(Shell.rootAccess())
    }
}