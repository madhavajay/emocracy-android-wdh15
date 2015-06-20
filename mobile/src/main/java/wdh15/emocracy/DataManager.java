package wdh15.emocracy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
        setSettingKeyWithValue(CHANNELS_MODEL_KEY, channelsArrayJson);



        Log.v(TAG, "saving user model json: " + channelsArrayJson);
    }

    public ArrayList<ChannelModel> getChannels() {
        //Type listOfChannelsObject = new TypeToken<List<ChannelModel>>(){}.getType();
        String channelsJson = (String) getSetting(CHANNELS_MODEL_KEY, "String", "");

        Type collectionType = new TypeToken<Collection<ChannelModel>>(){}.getType();
        Collection<ChannelModel> channelModelsArray = gson.fromJson(channelsJson, collectionType);

        //ChannelModel[] channelModelsArray = gson.fromJson(channelsJson, ChannelModel[].class);
        //ArrayList<ChannelModel> channelModels = new ArrayList<ChannelModel>(Arrays.asList(channelModelsArray));
        ArrayList<ChannelModel> channelModels = new ArrayList<ChannelModel>(channelModelsArray);



        return channelModels;
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
