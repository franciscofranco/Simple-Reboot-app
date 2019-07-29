package simple.reboot.com;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.Shell;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.coordinator)
    protected CoordinatorLayout coordinatorLayout;
    @BindView(R.id.container)
    protected ViewGroup container;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MainActivity_ViewBinding(this);

        Shell.getShell(shell -> {
            if (!shell.isRoot()) {
                if (coordinatorLayout != null) {
                    Snackbar.make(coordinatorLayout, R.string.root_status_no,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.close, view -> finishAnimation()).show();
                }
            }
        });

        container.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (container != null) {
                            container.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            container.setTranslationY(coordinatorLayout.getHeight() >> 2);
                            container.setTranslationX(container.getWidth());
                            container.animate().cancel();
                            container.animate().translationX(0f).setStartDelay(100)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            container.setVisibility(View.VISIBLE);
                                        }
                                    })
                                    .start();
                        }
                    }
                });
    }

    private void finishAnimation() {
        if (container != null) {
            container.animate().cancel();
            container.animate().translationX(container.getWidth())
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

    @OnClick(R.id.coordinator)
    protected void onParentClick() {
        onBackPressed();
    }

    @OnClick(R.id.shutdown)
    public void onShutdownClick(View view) {
        runCmd(SHUTDOWN);
    }

    @OnClick(R.id.reboot)
    public void onRebootClick(View view) {
        runCmd(REBOOT_CMD);
    }

    @OnClick(R.id.soft_reboot)
    public void onSoftRebootClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_SOFT_REBOOT_CMD);
            }
        }, SHUTDOWN_BROADCAST());
    }

    @OnClick(R.id.reboot_recovery)
    public void onRebootRecoveryClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_RECOVERY_CMD);
            }
        }, SHUTDOWN_BROADCAST());
    }

    @OnClick(R.id.reboot_bootloader)
    public void onRebootBootloaderClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_BOOTLOADER_CMD);
            }
        }, SHUTDOWN_BROADCAST());
    }

    @OnClick(R.id.reboot_safe_mode)
    public void onRebootSafeModeClick(View view) {
        runCmdWithCallback(out -> {
            if (out.isSuccess()) {
                runCmd(REBOOT_SAFE_MODE);
            }
        }, SHUTDOWN_BROADCAST());
    }

    @Override
    public void onBackPressed() {
        finishAnimation();
    }
}
