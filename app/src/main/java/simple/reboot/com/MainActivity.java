package simple.reboot.com;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import eu.chainfire.libsuperuser.Shell;

/**
 * The Activity extends ActionBarActivity because that's a requirement per the new API for it to
 * bound with the Toolbar. I suggest you to read Chris Banes (Google programmer) blog about this
 */
public class MainActivity extends ActionBarActivity {
    /**
     * It's good practice to declare immutable variables in upper case
     */
    private static final String REBOOT_CMD = "reboot";
    private static final String REBOOT_RECOVERY_CMD = "reboot recovery";
    private static final String REBOOT_BOOTLOADER_CMD = "reboot bootloader";
    private static final String PLAY_STORE_MY_APPS
            = "https://play.google.com/store/apps/developer?id=Francisco+Franco";

    /**
     * This annotation comes from ButterKnife lib. Please it's documentation to understand what
     * it does, but it's pretty self explanatory since I'm not doing findViewById rogrammatically
     * during activity create
     */
    @InjectView(R.id.root_status)
    protected TextView mRootStatusSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        /**
         * As per new Support API we can use a Toolbar instead of a ActionBar. The big difference
         * functionality wise is that the Toolbar is part of the View hierarchy of your layout
         * and because of that you have full control over it like a normal View,
         * which is pretty useful. You can also use a normal Navigation Drawer with it,
         * but for that you should consult API details as it's not the purpose of this app.
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * Starting a new generic Thread like this is not the best practice because it's not
         * really bound to the activity lifecycle and you can't really stop it if this gets
         * destroyed. A typical case of a crash would be the Thread starts,
         * then the activity is destroyed (imagine a rotation of you click back) and then on the
         * Handler (which runs on the Main Thread (Looper.getMainLooper())) will try to set the
         * Text into the TextView and it *can* be null since you cannot guarantee the Thread will
         * finish at any precise period in time and the view reference could be lost in between.
         *
         * This is a lot of 'coulda' or 'shoulda' but it's not good to assume that things will
         * work just because.
         *
         * In this specific case, and due to the nature of the app which does nothing really
         * important we can get away with simply checking if the TextView(mRootStatusSummary)
         * isn't null at the time of the action to prevent a FC. Just for FYI this version has been
         * deployed to a couple thousands of users and I've yet to receive a single crash.
         *
         * If you're going to use Threading on your own app make sure to read about all the
         * different Threading APIs available which are generally more acceptable than this
         * solution. But as always be sure to evaluate your needs and decide which fits you best.
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Shell.SU.available()) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (mRootStatusSummary != null) {
                                mRootStatusSummary.setText(R.string.root_status_yes);
                            }
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * The following @OnClick annotations are part of ButterKnife's lib. If you wish to learn
     * about it please read its documentation
     */

    @OnClick(R.id.about)
    public void onAboutClick(View view) {
        String url = PLAY_STORE_MY_APPS;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    /**
     * I'm using generic Threads below because in this case we don't care about the activity
     * lifecycle, we just want to run these root commands on the background thread so that we
     * don't block the main thread, so for this case it's acceptable to just do new Thread() and
     * get along with it
     *
     * What each command does it's pretty self explanatory.
     */

    @OnClick(R.id.reboot)
    public void onRebootClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run(REBOOT_CMD);
            }
        }).start();
    }

    @OnClick(R.id.reboot_recovery)
    public void onRebootRecoveryClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run(REBOOT_RECOVERY_CMD);
            }
        }).start();
    }

    @OnClick(R.id.reboot_bootloader)
    public void onRebootBootloaderClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run(REBOOT_BOOTLOADER_CMD);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
