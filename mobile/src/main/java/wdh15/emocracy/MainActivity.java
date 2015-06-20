package wdh15.emocracy;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

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

        setContentView(R.layout.activity_main);
        setupView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!dataManager.hasUser()) {
            Log.v(TAG, "No user so show login");
            showLogin();
        } else {
            Log.v(TAG, "Already got user so show main screen");

            NetworkManager.getInstance().getAllChannels(getApplication());
            setupRunLoop();
            // Register LocalBroadcastNotifcations
            LocalBroadcastManager.getInstance(this).registerReceiver(this.messageReceiver, new IntentFilter(NetworkManager.CHANNELS_RESPONSE));
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
        Log.v(TAG, "calling setup view");
        FragmentManager fragmentManager = getFragmentManager();
        ChannelFragment firstChannel = (ChannelFragment) fragmentManager.findFragmentById(R.id.first_channel);
        ChannelFragment secondChannel = (ChannelFragment) fragmentManager.findFragmentById(R.id.second_channel);
        ChannelFragment thirdChannel = (ChannelFragment) fragmentManager.findFragmentById(R.id.third_channel);

        Button buttonAllChannels = (Button) findViewById(R.id.button_all_channels);
        buttonAllChannels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "clicked on all channels button");
            }
        });

        Button buttonMoodHappy = (Button) findViewById(R.id.button_mood_happy);
        buttonMoodHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Clicked on happy mood button");
            }
        });

        Button buttonMoodSad = (Button) findViewById(R.id.button_mood_sad);
        buttonMoodSad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Clicked on sad mood button");
            }
        });

    }

    private void updateView() {
        ArrayList<ChannelModel> channels = dataManager.getChannels();

        Log.v(TAG, "updateview with these channels: " + channels.toString());

        Log.v(TAG, "calling update view");
        FragmentManager fragmentManager = getFragmentManager();
        ChannelFragment firstChannel = (ChannelFragment) fragmentManager.findFragmentById(R.id.first_channel);
        ChannelFragment secondChannel = (ChannelFragment) fragmentManager.findFragmentById(R.id.second_channel);
        ChannelFragment thirdChannel = (ChannelFragment) fragmentManager.findFragmentById(R.id.third_channel);

        if (channels.size() > 0) {
            final ChannelModel firstChannelModel = channels.remove(0);
            if (firstChannel != null) {
                firstChannel.updateView(firstChannelModel);

                firstChannel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v(TAG, "HELL YES IM CLICKING THE FRAGMENT");
                        Intent i;
                        i = new Intent(MainActivity.this, ChannelVoteActivity.class);
                        Bundle extras = new Bundle();
                        extras.putInt("channel_id", firstChannelModel.id);
                        i.putExtras(extras);
                        startActivity(i);
                    }
                });
            }
        }

        if (channels.size() > 0) {
            final ChannelModel secondChannelModel = channels.remove(0);
            if (secondChannelModel != null) {
                secondChannel.updateView(secondChannelModel);

                secondChannel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i;
                        i = new Intent(MainActivity.this, ChannelVoteActivity.class);
                        Bundle extras = new Bundle();
                        extras.putInt("channel_id", secondChannelModel.id);
                        i.putExtras(extras);
                        startActivity(i);
                    }
                });
            }
        }

        if (channels.size() > 0) {
            final ChannelModel thirdChannelModel = channels.remove(0);
            if (thirdChannelModel != null) {
                thirdChannel.updateView(thirdChannelModel);

                thirdChannel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v(TAG, "HELL YES IM CLICKING THE FRAGMENT");
                        Intent i;
                        i = new Intent(MainActivity.this, ChannelVoteActivity.class);
                        Bundle extras = new Bundle();
                        extras.putInt("channel_id", thirdChannelModel.id);
                        i.putExtras(extras);
                        startActivity(i);
                    }
                });
            }
        }

        TextView labelHappyNumber = (TextView) findViewById(R.id.label_number_happy);
        labelHappyNumber.setText("" + 1);
        TextView labelSadNumber = (TextView) findViewById(R.id.label_number_sad);
        labelSadNumber.setText("" + 0);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NetworkManager.CHANNELS_RESPONSE)) {
                int newChannelData = (int)intent.getIntExtra("success", 0);
                if (newChannelData == 1) {
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
