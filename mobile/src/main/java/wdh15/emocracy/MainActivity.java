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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by madhavajay on 20/06/15.
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private DataManager dataManager;

    private final Handler handler = new Handler();

    private ChannelListAdapter adapter;
    private ListView listView;
    public ArrayList<ChannelModel> channels;

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
                handler.postDelayed(this, 3000);
            }
        };

        handler.postDelayed(runnable, 3000);
    }

    private void setupView() {
        Log.v(TAG, "calling setup view");

        listView = (ListView) findViewById(R.id.list_view);

        channels = new ArrayList<ChannelModel>();
        Log.v(TAG, "we have a channels list to apply to the adapter " + channels.toString());

        adapter = new ChannelListAdapter(this, R.layout.channel, channels);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "clicked on a listview item");

                ChannelModel channelModel = (ChannelModel) listView.getItemAtPosition(position);
                Intent i;
                i = new Intent(MainActivity.this, ChannelVoteActivity.class);
                Bundle extras = new Bundle();
                extras.putInt("channel_id", channelModel.id);
                i.putExtras(extras);
                startActivity(i);

            }
        });
    }

    private void updateView() {
        channels.clear();
        channels.addAll(dataManager.getChannels());
        adapter.notifyDataSetChanged();

        Log.v(TAG, "updateview with these channels: " + channels.toString());

        Log.v(TAG, "calling update view");
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NetworkManager.CHANNELS_RESPONSE)) {
                int newChannelData = (int)intent.getIntExtra("success", 0);
                if (newChannelData == 1) {
                    Log.v(TAG, "new channel data");
                    updateView();
                } else {
                    Toast.makeText(MainActivity.this, "Server not responding, trying again.", Toast.LENGTH_SHORT).show();
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
