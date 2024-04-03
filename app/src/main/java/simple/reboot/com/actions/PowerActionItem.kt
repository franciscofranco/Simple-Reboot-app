package simple.reboot.com.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable

@Stable
data class PowerActionItem(
    val type: PowerActionType,
    @StringRes val title: Int,
    @DrawableRes val icon: Int? = null,
    val command: String,
)