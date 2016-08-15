package simple.reboot.com;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

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
    private static final String REBOOT_BOOTLOADER_CMD = "svc power reboot bootloader";
    private static final String[] REBOOT_SAFE_MODE
            = new String[]{"setprop persist.sys.safemode 1", REBOOT_SOFT_REBOOT_CMD};
    private static final String PLAY_STORE_MY_APPS
            = "https://play.google.com/store/apps/developer?id=Francisco+Franco";

    private static final int RUNNABLE_DELAY_MS = 1000;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        HandlerThread mHandlerThread = new HandlerThread("BackgroundThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return Shell.SU.available();
            }

            @Override
            protected void onPostExecute(Boolean rootNotAvailable) {

                // We don't really know if the activity is still alive at this point. The information we want to
                // show is not really critical so we can just wrap it around a try & catch and let it fail
                // gracefully
                if (!rootNotAvailable) {
                    try {
                        Snackbar.make(getWindow().getDecorView(), R.string.root_status_no,
                                Snackbar.LENGTH_INDEFINITE)
                                .setAction(R.string.close, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        finish();
                                    }
                                })
                                .show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @WorkerThread
    private void runCmd(long timeout, @NonNull final String... cmd) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run(cmd);
                mHandler.removeCallbacks(this);
            }
        }, timeout);
    }

    /**
     * The following @OnClick annotations are part of ButterKnife's lib. If you wish to learn
     * about it please read its documentation
     *
     * @see <a href="http://jakewharton.github.io/butterknife/">ButterKnife</a>
     */
    @OnClick(R.id.about)
    public void onAboutClick(View view) {
        String url = PLAY_STORE_MY_APPS;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @OnClick(R.id.shutdown)
    public void onShutdownClick(View view) {
        runCmd(0, SHUTDOWN);
    }

    @OnClick(R.id.reboot)
    public void onRebootClick(View view) {
        runCmd(0, REBOOT_CMD);
    }

    @OnClick(R.id.soft_reboot)
    public void onSoftRebootClick(View view) {
        runCmd(0, SHUTDOWN_BROADCAST());
        runCmd(RUNNABLE_DELAY_MS, REBOOT_SOFT_REBOOT_CMD);
    }

    @OnClick(R.id.reboot_recovery)
    public void onRebootRecoveryClick(View view) {
        runCmd(0, SHUTDOWN_BROADCAST());
        runCmd(RUNNABLE_DELAY_MS, REBOOT_RECOVERY_CMD);
    }

    @OnClick(R.id.reboot_bootloader)
    public void onRebootBootloaderClick(View view) {
        runCmd(0, REBOOT_BOOTLOADER_CMD);
    }

    @OnClick(R.id.reboot_safe_mode)
    public void onRebootSafeModeClick(View view) {
        runCmd(0, SHUTDOWN_BROADCAST());
        runCmd(RUNNABLE_DELAY_MS, REBOOT_SAFE_MODE);
    }
}
