package wdh15.emocracy;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by madhavajay on 3/05/15.
 */
public class DataManager {

    private static final String EMOCRACY_KEY = "emocracy";
    private static final String USER_KEY = "user";
    private static final String USER_MODEL_KEY = "user_model";
    private static final String CHANNELS_MODEL_KEY = "channels_model";
    private static final String VOTES_FOR_CHANNELS_MODEL_KEY = "votes_channels_model";

    private static final String TAG = "DataManager";
    private Context context;

    private final Gson gson = new Gson();

    public DataManager(Context context) {
        this.context = context;
    }

    public boolean hasUser() {
        Integer hasUser = (Integer)this.getSetting(USER_KEY, "Integer", 0);
        if (hasUser == 1) {
            return true;
        }
        return false;
    }

    public void setHasUser(boolean hasUser) {
        int user = (hasUser) ? 1 : 0;
        setSettingKeyWithValue(USER_KEY, user);
    }

    public void setUserJson(String userModelJson) {
        setSettingKeyWithValue(USER_MODEL_KEY, userModelJson);
        Log.v(TAG, "saving user model json: " + userModelJson);
    }

    public void setChannels(String channelsJson) {

        GsonBuilder builder = new GsonBuilder();
        Map o = (Map) builder.create().fromJson(channelsJson, Object.class);

        Log.v(TAG, "the object of channels: " + o.toString());
        List channels = (List) o.get("channels");


        Log.v(TAG, "my channels list " + channels);
/*
my channels list [{name=Hungry, id=1.0, yes=0.0, no=0.0, alive=0.0, democracy=null}, {name=Beertime, id=2.0, yes=0.0, no=0.0, alive=0.0, democracy=null}]

 */
/*
the object of channels: {channels=[{name=Hungry, id=1.0, yes=0.0, no=0.0, alive=0.0, democracy=null}, {name=Beertime, id=2.0, yes=0.0, no=0.0, alive=0.0, democracy=null}]}

 */

        /*
        //JSONObject channelsObject = gson.fromJson(channelsJson, JSONObject.class);
        //JSONArray channelsArray = new JSONArray();
        try {
            channelsArray = (JSONArray) channelsObject.getJSONArray("channels");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
        String channelsArrayJson = gson.toJson(channels);

        // we need these for comparing
        ArrayList<ChannelModel> oldChannels = this.getChannels();

        setSettingKeyWithValue(CHANNELS_MODEL_KEY, channelsArrayJson);

        Log.v(TAG, "saving user model json: " + channelsArrayJson);
        updateBasedOnChannelState(oldChannels);
    }

    private void updateBasedOnChannelState(ArrayList<ChannelModel> oldChannels) {
        ArrayList<ChannelModel> channels = this.getChannels();
        Log.v(TAG, "updating based on channel state");
        Log.v(TAG, "old channels " + oldChannels);
        Log.v(TAG, "new channels " + channels);

        for (ChannelModel channelModel : channels) {
            if (channelModel.alive < 1) {
                Log.v(TAG, "found a channel which is dead so wiping the local votes ID: " + channelModel.id);
                this.setUserVoteForChannelId(channelModel.id, 0);
            }

            ChannelModel oldChannelModelMatch = null;
            for (ChannelModel oldChannelModel : oldChannels) {
                if (oldChannelModel.id == channelModel.id) {
                    Log.v(TAG, "found old channel to compare to");
                    oldChannelModelMatch = oldChannelModel;
                }
            }

            //Log.v(TAG, "old timestamp " + oldChannelModelMatch.timestamp + " new timestamp " + channelModel.timestamp);
            //Log.v(TAG, "values of " + oldChannelModelMatch + " " +channelModel.timestamp + " " + oldChannelModelMatch.timestamp);


            int channelVoteForUser = this.getUserVoteForChannelId(channelModel.id);
            if (oldChannelModelMatch == null || channelModel.timestamp > oldChannelModelMatch.timestamp) {
                // we can check for notifications and send them
                int democracy = -1;
                try {
                     democracy = channelModel.democracy;
                    if (democracy == 0 || democracy == 1) {
                        Log.v(TAG, "we have a result for the vote " + democracy);
                        this.sendNotification(1, channelModel);
                    }
                } catch (Exception e) {
                    Log.v(TAG, e.toString());
                }

                Log.v(TAG, "what is democracy now? " + democracy);
                if (democracy == -1 && channelModel.alive > 0 && channelVoteForUser == 0) {
                    Log.v(TAG, "we have a new thing to vote on: " + channelModel.name);
                    this.sendNotification(0, channelModel);
                }
            }
        }
    }

    private void sendNotification(int notficationType, ChannelModel channelModel) {

        String iconName = "icon_" + channelModel.id + "_white";

        int iconId = this.context.getResources().getIdentifier("wdh15.emocracy:drawable/" + iconName, null, null);

        int notificationId = (int) (System.currentTimeMillis() / 1000L);


        // Build intent for notification content
        Intent viewIntent = new Intent(this.context, ChannelVoteActivity.class);
        viewIntent.putExtra("channel_id", channelModel.id);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this.context, notificationId, viewIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setHintHideIcon(false)
                        .setBackground(BitmapFactory.decodeResource(
                                this.context.getResources(), R.drawable.notification_icon))
                        .clearActions();


        String message = channelModel.name;
        String title = channelModel.name;
        Log.v(TAG, "so were sending a notifcation for channel: " + channelModel.name + " with " + channelModel.toString());
        if (notficationType == 1) {
            if (channelModel.democracy == 1) {
                message += " has WON!";
                title += " has WON!";
            } else if (channelModel.democracy == 0) {
                message += " has LOST!";
                title += " has LOST!";
            }
        } else if (notficationType == 0) {

        }

        /*
        Intent notificationIntent = new Intent(context, Main.class);
	notificationIntent.putExtra("item_id", "1001"); // <-- HERE I PUT THE EXTRA VALUE
	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
	notif.contentIntent = contentIntent;
         */





        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.context)
                // ..- .--. = UP in morse code
                // wait, on, wait, on, wait etc...
                .setVibrate(new long[]{0, 200, 200, 200, 200, 400, 200, 200, 200, 400, 200, 400, 200, 200})
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setLocalOnly(false)
                .extend(wearableExtender)
                .setContentIntent(viewPendingIntent);
        notificationBuilder.setAutoCancel(true);

        if (notficationType == 0) {
            Intent voteYesIntent = new Intent(this.context, ChannelVoteActivity.class);
            voteYesIntent.putExtra("channel_id", channelModel.id);
            voteYesIntent.putExtra("yes_no", 1);

            PendingIntent voteYesPendingIntent = PendingIntent.getActivity(this.context, notificationId + 1, voteYesIntent, PendingIntent.FLAG_ONE_SHOT);

            Intent voteNoIntent = new Intent(this.context, ChannelVoteActivity.class);
            voteNoIntent.putExtra("channel_id", channelModel.id);
            voteNoIntent.putExtra("yes_no", 0);
            PendingIntent voteNoPendingIntent = PendingIntent.getActivity(this.context, notificationId + 2, voteNoIntent, PendingIntent.FLAG_ONE_SHOT);

            notificationBuilder.addAction(R.drawable.icon_yes_white_wear, "Yes", voteYesPendingIntent);
            notificationBuilder.addAction(R.drawable.icon_no_white_wear, "No", voteNoPendingIntent);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.context);

        try {
            if (notficationType == 0) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(this.context, notification);
                r.play();
            } else if (notficationType == 1) {
                Log.v(TAG, "are we here for sound?");
                Uri jingle = null;
                if (channelModel.democracy == 1) {
                    jingle = Uri.parse("android.resource://wdh15.emocracy/raw/good");
                } else if (channelModel.democracy == 0) {
                    jingle = Uri.parse("android.resource://wdh15.emocracy/raw/bad");
                }

                Log.v(TAG, "heres our audio link: " + jingle);
                notificationBuilder.setSound(jingle);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        notificationManager.notify(notificationId, notificationBuilder.build());

    }


    public ArrayList<ChannelModel> getChannels() {
        //Type listOfChannelsObject = new TypeToken<List<ChannelModel>>(){}.getType();
        String channelsJson = (String) getSetting(CHANNELS_MODEL_KEY, "String", "[]");

        Log.v(TAG, "loading to inflate channels json is: " + channelsJson);

        Type collectionType = new TypeToken<Collection<ChannelModel>>(){}.getType();
        Collection<ChannelModel> channelModelsArray = gson.fromJson(channelsJson, collectionType);

        //ChannelModel[] channelModelsArray = gson.fromJson(channelsJson, ChannelModel[].class);
        //ArrayList<ChannelModel> channelModels = new ArrayList<ChannelModel>(Arrays.asList(channelModelsArray));
        ArrayList<ChannelModel> channelModels = new ArrayList<ChannelModel>(channelModelsArray);



        return channelModels;
    }

    public int getUserVoteForChannelId(int channelId) {
        HashMap<Integer, Integer> votesForChannels = this.getVotesForChannels();
        // key is channelId
        Integer channelIdInteger = new Integer(channelId);
        int vote = 0; // not vote state
        if (votesForChannels.containsKey(channelIdInteger)) {
            vote = votesForChannels.get(channelIdInteger);
        }

        Log.v(TAG, "the vote for the channel id: " + channelId + " is: " + vote);
        return vote;
    }

    public void setUserVoteForChannelId(int channelId, int noValueYesNo) {
        // noValueYesNo is 0, 1, 2
        HashMap<Integer, Integer> votesForChannels = this.getVotesForChannels();
        votesForChannels.put(channelId, noValueYesNo);

        Type type = new TypeToken<Map<String, String>>(){}.getType();
        String votesForChannelsJson = gson.toJson(votesForChannels, type);
        Log.v(TAG, "saving this vote for channel JSON");
        setSettingKeyWithValue(VOTES_FOR_CHANNELS_MODEL_KEY, votesForChannelsJson);
    }

    private HashMap<Integer, Integer> getVotesForChannels() {
        // key is channelId
        HashMap<Integer, Integer> votesForChannels = new HashMap<Integer, Integer>();
        String votesForChannelsJson = (String) this.getSetting(VOTES_FOR_CHANNELS_MODEL_KEY, "String", "{}");
        //String votesForChannelsJson = (String) this.getSetting(VOTES_FOR_CHANNELS_MODEL_KEY, "String", "{\"0\":\"0\"}");
        Log.v(TAG, "before fail we get: " + votesForChannelsJson);
        if (votesForChannelsJson.equals("\"\"") || votesForChannelsJson.equals("") ) {
            //votesForChannelsJson = "{\"0\":\"0\"}";
            votesForChannelsJson = "{}";
            //votesForChannelsJson = gson.toJson(votesForChannelsJson);
            //setSettingKeyWithValue(VOTES_FOR_CHANNELS_MODEL_KEY, votesForChannelsJson);
        }

        Type type = new TypeToken<Map<Integer, Integer>>(){}.getType();
        Log.v(TAG, "wtf is the json looking like: " + votesForChannelsJson);
        votesForChannels = gson.fromJson(votesForChannelsJson, type);

        return votesForChannels;
    }


    public ChannelModel getChannelById(int channelId) {
        ArrayList<ChannelModel> channels = this.getChannels();
        for (ChannelModel channelModel : channels) {
            if (channelModel.id == channelId) {
                return channelModel;
            }
        }
        return null;
    }

    /*

    Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
Collection<Integer> ints2 = gson.fromJson(json, collectionType);

    {
  "channels": [
    {
      "name": "Hungry?",
      "id": 2,
      "yes": 10,
      "no": 1,
      "alive": 1,
      "democracy": 1
    }
  ]
}
     */

    public UserModel getUser() {
        String userModelJson = (String) getSetting(USER_MODEL_KEY, "String", "");
        UserModel userModel = gson.fromJson(userModelJson, UserModel.class);

        Log.v(TAG, "our user model " + userModel.toString());
        return userModel;
    }

    private SharedPreferences getSharedPreferences() {
        SharedPreferences settings = this.context.getSharedPreferences(EMOCRACY_KEY, Context.MODE_PRIVATE);
        return settings;
    }

    private Object getSetting(String settingKey, String type, Object defaultVal) {
        SharedPreferences settings = this.getSharedPreferences();
        if (type.equals("String")) {
            return settings.getString(settingKey, defaultVal.toString());
        } else if (type.equals("Integer")) {
            int settingsInt = settings.getInt(settingKey, (int)defaultVal);
            return new Integer(settingsInt);
        }

        return null;
    }

    private void setSettingKeyWithValue(String key, Integer value) {
        SharedPreferences settings = this.getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value.intValue());
        editor.commit();
    }

    private void setSettingKeyWithValue(String key, String value) {
        SharedPreferences settings = this.getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
