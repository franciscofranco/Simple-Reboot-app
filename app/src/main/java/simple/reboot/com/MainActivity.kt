package simple.reboot.com

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.topjohnwu.superuser.Shell
import simple.reboot.com.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.areWeRooted().observe(this, ::renderRootAccess)

        binding.container.post {
            binding.container.translationY = (binding.coordinator.height shr 2.toFloat().toInt()).toFloat()
            binding.container.translationX = binding.container.width.toFloat()
            binding.container.animate().apply {
                cancel()
                translationX(0f).startDelay = 100
                setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        binding.container.isVisible = true
                    }
                })
                start()
            }
        }

        binding.root.setOnClickListener { onBackPressed() }
        binding.shutdown.setOnClickListener { runCmd(SHUTDOWN) }
        binding.reboot.setOnClickListener { runCmd(REBOOT_CMD) }
        binding.softReboot.setOnClickListener { onSoftRebootClick() }
        binding.rebootRecovery.setOnClickListener { onRebootRecoveryClick() }
        binding.rebootBootloader.setOnClickListener { onRebootBootloaderClick() }
        binding.rebootSafeMode.setOnClickListener { onRebootSafeModeClick() }
        binding.restartSystemui.setOnClickListener { onRebootSystemUi() }
        binding.softReboot.setOnClickListener { onSoftRebootClick() }
    }

    private fun renderRootAccess(areWeRooted: Boolean) {
        if (!areWeRooted) {
            Snackbar.make(binding.coordinator, R.string.root_status_no, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.close) { finishAnimation() }
                    .show()
        }
    }

    private fun finishAnimation() {
        binding.container.animate().apply {
            cancel()
            translationX(binding.container.width.toFloat())
            setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    finish()
                }
            })
            start()
        }
    }

    private fun runCmd(vararg cmd: String) {
        finishAnimation()
        Shell.su(*cmd).submit()
    }

    private fun runCmdWithCallback(callback: Shell.ResultCallback, vararg cmd: String) {
        finishAnimation()
        Shell.su(*cmd).submit(callback)
    }


    private fun onSoftRebootClick() {
        runCmdWithCallback({ out: Shell.Result ->
            if (out.isSuccess) runCmd(REBOOT_SOFT_REBOOT_CMD)
        }, *SHUTDOWN_BROADCAST)
    }

    private fun onRebootRecoveryClick() {
        runCmdWithCallback({ out: Shell.Result ->
            if (out.isSuccess) runCmd(REBOOT_RECOVERY_CMD)
        }, *SHUTDOWN_BROADCAST)
    }

    private fun onRebootBootloaderClick() {
        runCmdWithCallback({ out: Shell.Result ->
            if (out.isSuccess) runCmd(REBOOT_BOOTLOADER_CMD)
        }, *SHUTDOWN_BROADCAST)
    }

    private fun onRebootSafeModeClick() {
        runCmdWithCallback({ out: Shell.Result ->
            if (out.isSuccess) runCmd(*REBOOT_SAFE_MODE)
        }, *SHUTDOWN_BROADCAST)
    }

    private fun onRebootSystemUi() {
        runCmdWithCallback({ cmd: Shell.Result ->
            cmd.apply {
                if (isSuccess && out.size == 1) {
                    // pidof returns only one line anyway, guard just for safe measures
                    val stdout = cmd.out[0].trim { it <= ' ' }
                    val pid = stdout.toInt()
                    runCmd(String.format(Locale.getDefault(), REBOOT_SYSTEMUI_CMD, pid))
                }
            }
        }, PIDOF_SYSTEMUI)
    }

    override fun onBackPressed() {
        finishAnimation()
    }

    companion object {
        // just for safe measure, we don't want any data corruption, right?
        private val SHUTDOWN_BROADCAST = arrayOf( // we announce the device is going down so apps that listen for
                // this broadcast can do whatever
                "am broadcast android.intent.action.ACTION_SHUTDOWN",  // we tell the file system to write any data buffered in memory out to disk
                "sync",  // we also instruct the kernel to drop clean caches, as well as
                // reclaimable slab objects like dentries and inodes
                "echo 3 > /proc/sys/vm/drop_caches",  // and sync buffered data as before
                "sync")
        private const val SHUTDOWN = "svc power shutdown"
        private const val REBOOT_CMD = "svc power reboot"
        private const val REBOOT_SOFT_REBOOT_CMD = "setprop ctl.restart zygote"
        private const val REBOOT_RECOVERY_CMD = "reboot recovery"
        private const val REBOOT_BOOTLOADER_CMD = "reboot bootloader"
        private const val REBOOT_SYSTEMUI_CMD = "kill %d"
        private const val PKG_SYSTEMUI = "com.android.systemui"
        private const val PIDOF_SYSTEMUI = "pidof $PKG_SYSTEMUI"
        private val REBOOT_SAFE_MODE = arrayOf("setprop persist.sys.safemode 1", REBOOT_SOFT_REBOOT_CMD)
    }
}