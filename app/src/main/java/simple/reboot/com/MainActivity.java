package simple.reboot.com;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Locale;

import simple.reboot.com.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding b;

    // just for safe measure, we don't want any data corruption, right?
    private static String[] SHUTDOWN_BROADCAST() {
        return new String[]{
                // we announce the device is going down so apps that listen for
                // this broadcast can do whatever
                "am broadcast android.intent.action.ACTION_SHUTDOWN",
                // we tell the file system to write any data buffered in memory out to disk
                "sync",
                // we also instruct the kernel to drop clean caches, as well as
                // reclaimable slab objects like dentries and inodes
                "echo 3 > /proc/sys/vm/drop_caches",
                // and sync buffered data as before
                "sync",
        };
    }

    private static final String SHUTDOWN = "svc power shutdown";
    private static final String REBOOT_CMD = "svc power reboot";
    private static final String REBOOT_SOFT_REBOOT_CMD = "setprop ctl.restart zygote";
    private static final String REBOOT_RECOVERY_CMD = "reboot recovery";
    private static final String REBOOT_BOOTLOADER_CMD = "reboot bootloader";
    private static final String[] REBOOT_SAFE_MODE
            = new String[]{"setprop persist.sys.safemode 1", REBOOT_SOFT_REBOOT_CMD};
    private static final String REBOOT_SYSTEMUI_CMD = "kill %d";
    private static final String PKG_SYSTEMUI = "com.android.systemui";
    private static final String PIDOF_SYSTEMUI = "pidof " + PKG_SYSTEMUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        Shell.getShell(shell -> {
            if (!shell.isRoot()) {
                if (b != null) {
                    Snackbar.make(b.coordinator, R.string.root_status_no,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.close, view -> finishAnimation()).show();
                }
            }
        });

        b.container.post(() -> {
            b.container.setTranslationY(b.coordinator.getHeight() >> 2);
            b.container.setTranslationX(b.container.getWidth());
            b.container.animate().cancel();
            b.container.animate().translationX(0f).setStartDelay(100)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            b.container.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        });
    }

    private void finishAnimation() {
        if (b != null) {
            b.container.animate().cancel();
            b.container.animate().translationX(b.container.getWidth())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            finish();
                        }
                    })
                    .start();
        }
    }

    private void runCmd(@NonNull final String... cmd) {
        finishAnimation();
        Shell.su(cmd).submit();
    }

    private void runCmdWithCallback(Shell.ResultCallback callback, @NonNull final String... cmd) {
        finishAnimation();
        Shell.su(cmd).submit(callback);
    }

    public void onParentClick(View view) {
        onBackPressed();
    }

    public void onShutdownClick(View view) {
        runCmd(SHUTDOWN);
    }

    public void onRebootClick(View view) {
        runCmd(REBOOT_CMD);
    }

    public void onSoftRebootClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_SOFT_REBOOT_CMD);
            }
        }, SHUTDOWN_BROADCAST());
    }

    public void onRebootRecoveryClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_RECOVERY_CMD);
            }
        }, SHUTDOWN_BROADCAST());
    }

    public void onRebootBootloaderClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_BOOTLOADER_CMD);
            }
        }, SHUTDOWN_BROADCAST());
    }

    public void onRebootSafeModeClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_SAFE_MODE);
            }
        }, SHUTDOWN_BROADCAST());
    }

    public void onRebootSystemUi(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                List<String> result = out.getOut();

                if (result.size() == 1) {
                    // pidof returns only one line anyway, guard just for safe measures
                    String stdout = result.get(0).trim();
                    int pid = Integer.parseInt(stdout);

                    runCmd(String.format(Locale.getDefault(), REBOOT_SYSTEMUI_CMD, pid));
                }
            }
        }, PIDOF_SYSTEMUI);
    }

    @Override
    public void onBackPressed() {
        finishAnimation();
    }
}
