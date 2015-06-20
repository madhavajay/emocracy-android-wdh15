package wdh15.emocracy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.GregorianCalendar;

/**
 * Created by madhavajay on 20/06/15.
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private DataManager dataManager;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        dataManager = new DataManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!dataManager.hasUser()) {
            Log.v(TAG, "No user so show login");
            showLogin();
        } else {
            Log.v(TAG, "Already got user so show main screen");
            //setupView();
            setupRunLoop();
            // Register LocalBroadcastNotifcations
            LocalBroadcastManager.getInstance(this).registerReceiver(this.messageReceiver, new IntentFilter(NetworkManager.LOGIN_RESPONSE));
        }
    }

    private void showLogin() {
        Intent i;
        i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void setupRunLoop() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "entered run loop fetch channel data and notifcations");

                NetworkManager.getInstance().getAllChannels(getApplication());
                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(runnable, 5000);
    }

    private void setupView() {

    }

    private void updateView() {

    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NetworkManager.CHANNELS_RESPONSE)) {
                int loginSuccess = (int)intent.getIntExtra("success", 0);
                if (loginSuccess == 1) {
                    Log.v(TAG, "new channel data");
                    updateView();
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.messageReceiver);

        handler.removeCallbacksAndMessages(null);
    }
}
