package wdh15.emocracy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by madhavajay on 20/06/15.
 */
public class ChannelVoteActivity extends Activity {

    private final static String TAG = "ChannelVoteActivity";

    private DataManager dataManager;
    private int channelId;
    private ChannelModel channelModel;

    private Button buttonVoteYes;
    private Button buttonVoteNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().hide();
        channelId = getIntent().getIntExtra("channel_id", 0);
        Log.v(TAG, "channel id is: " +  channelId);

        dataManager = new DataManager(this);
        channelModel = dataManager.getChannelById(channelId);
        if (channelModel == null) {
            Toast.makeText(ChannelVoteActivity.this, "Can't find Channel ID: " + channelId, Toast.LENGTH_SHORT).show();
            finish();
        }

        getActionBar().setTitle(channelModel.name);
        setContentView(R.layout.activity_channel_vote);

        setupView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register LocalBroadcastNotifcations
        LocalBroadcastManager.getInstance(this).registerReceiver(this.messageReceiver, new IntentFilter(NetworkManager.VOTE_RESPONSE));
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NetworkManager.VOTE_RESPONSE)) {
                int voteSuccess = (int)intent.getIntExtra("success", 0);
                if (voteSuccess == 1) {
                    Log.v(TAG, "Vote was successful");
                    Toast.makeText(getApplicationContext(), "Your vote has been submitted", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Your vote failed, please try again", Toast.LENGTH_LONG).show();
                    enableButtons();
                    setupView();
                }
            }
        }
    };

    private void enableButtons() {
        if (buttonVoteYes != null) {
            buttonVoteYes.setEnabled(true);
            buttonVoteYes.setBackgroundResource(R.drawable.icon_yes);
        }

        if (buttonVoteNo != null) {
            buttonVoteNo.setEnabled(true);
            buttonVoteNo.setBackgroundResource(R.drawable.icon_no);
        }
    }

    private void disableButtons() {
        if (buttonVoteYes != null) {
            buttonVoteYes.setEnabled(false);
            buttonVoteYes.setBackgroundResource(R.drawable.icon_yes_disabled);
        }

        if (buttonVoteNo != null) {
            buttonVoteNo.setEnabled(false);
            buttonVoteNo.setBackgroundResource(R.drawable.icon_no_disabled);
        }
    }

    private void setupView() {

        TextView labelVoteName = (TextView) findViewById(R.id.label_vote_name);
        labelVoteName.setText(channelModel.name);

        TextView labelVoteYes = (TextView) findViewById(R.id.label_vote_yes);
        labelVoteYes.setText("" + channelModel.yes);

        TextView labelVoteNo = (TextView) findViewById(R.id.label_vote_no);
        labelVoteNo.setText("" + channelModel.no);

        ImageView imageIcon = (ImageView) findViewById(R.id.channel_icon_large);
        String iconName = "icon_" + channelModel.id;

        int iconId = getResources().getIdentifier("wdh15.emocracy:drawable/" + iconName, null, null);
        imageIcon.setImageResource(iconId);

        buttonVoteYes = (Button) findViewById(R.id.button_vote_yes);
        buttonVoteNo = (Button) findViewById(R.id.button_vote_no);

        buttonVoteYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "clicked vote yes");
                NetworkManager.getInstance().voteYesForChannel(getApplication(), channelModel.id);
                disableButtons();
            }
        });


        buttonVoteNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "clicked vote NO");
                NetworkManager.getInstance().voteNoForChannel(getApplication(), channelModel.id);
                disableButtons();
            }
        });

        int userVoteForChannelId = dataManager.getUserVoteForChannelId(channelModel.id);
        Log.v(TAG, "the user has voted for this channel with the value: " + userVoteForChannelId);

        if (userVoteForChannelId > 0) {
            this.disableButtons();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.messageReceiver);
    }
}
