package wdh15.emocracy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by madhavajay on 3/05/15.
 */
public class DataManager {

    private static final String EMOCRACY_KEY = "emocracy";
    private static final String USER_KEY = "user";
    private static final String USER_MODEL_KEY = "user_model";

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