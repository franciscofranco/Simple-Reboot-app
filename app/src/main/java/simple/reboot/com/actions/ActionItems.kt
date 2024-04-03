package simple.reboot.com.actions

import simple.reboot.com.R

private const val SHUTDOWN = "svc power shutdown"
private const val REBOOT_CMD = "svc power reboot"
private const val REBOOT_SOFT_REBOOT_CMD = "stop ; start"
private const val REBOOT_RECOVERY_CMD = "reboot recovery"
private const val REBOOT_BOOTLOADER_CMD = "reboot bootloader"
private const val REBOOT_SYSTEMUI_CMD =
    "pidof com.android.systemui | awk '{print \$1}' | xargs kill"
private val REBOOT_SAFE_MODE = StringBuilder()
    .append("setprop persist.sys.safemode 1;\n")
    .append(REBOOT_SOFT_REBOOT_CMD)
    .toString()

object ActionItems {
    val commands: List<PowerActionItem> = listOf(
        PowerActionItem(
            type = PowerActionType.POWER_OFF,
            title = R.string.shutdown,
            icon = R.drawable.ic_power_off,
            command = SHUTDOWN
        ),
        PowerActionItem(
            type = PowerActionType.RESTART,
            title = R.string.reboot,
            icon = R.drawable.ic_restart,
            command = REBOOT_CMD
        ),
        PowerActionItem(
            type = PowerActionType.RESTART_SYSTEMUI,
            title = R.string.restart_systemui,
            command = REBOOT_SYSTEMUI_CMD
        ),
        PowerActionItem(
            type = PowerActionType.SOFT_RESTART,
            title = R.string.soft_reboot,
            command = REBOOT_SOFT_REBOOT_CMD
        ),
        PowerActionItem(
            type = PowerActionType.RESTART_RECOVERY,
            title = R.string.reboot_recovery,
            command = REBOOT_RECOVERY_CMD
        ),
        PowerActionItem(
            type = PowerActionType.RESTART_BOOTLOADER,
            title = R.string.reboot_bootloader,
            command = REBOOT_BOOTLOADER_CMD
        ),
        PowerActionItem(
            type = PowerActionType.RESTART_SAFE_MODE,
            title = R.string.reboot_safe_mode,
            command = REBOOT_SAFE_MODE
        )
    )
}