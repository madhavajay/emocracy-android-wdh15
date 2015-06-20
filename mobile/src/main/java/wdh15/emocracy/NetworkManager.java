package wdh15.emocracy;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by madhavajay on 20/06/15.
 */
public class NetworkManager {

    private final String TAG = "NetworkManager";
    private final String SERVER_URL = "http://192.168.170.47:8080/emocracy/api/";
    public static final String LOGIN_RESPONSE = "login_response";
    public static final String CHANNELS_RESPONSE = "channels_response";

    private DataManager dataManager;

    private Context context;
    private Application application;

    private UserModel userModel;

    private final Gson gson = new Gson();

    private static NetworkManager ourInstance = new NetworkManager();

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    public NetworkManager() {

    }

    private String getEndpointUrl(String endpoint) {
        return SERVER_URL + endpoint + "/";
    }

    public void loginUser(Application application, final String username) {
        this.application = application;
        this.context = this.application.getApplicationContext();
        new Thread(new Runnable() {

            @Override
            public void run() {
                makeLoginUser(username);
            }
        }).start();
    }

    public void makeLoginUser(String username) {
        String url = getEndpointUrl("register");

        try {
            url += URLEncoder.encode(username, "UTF-8").toString();
            Log.v(TAG, "login url constructed: " + url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Login response: " + response);
        Log.v(TAG, "Login res: " + response.toString());

        if (response != null && response.code() >= 200 && response.code() <= 299) {
            String responseJson = null;
            try {
                responseJson = response.body().string();
                dataManager = new DataManager(this.context);
                dataManager.setUserJson(responseJson);
                this.userModel = dataManager.getUser();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v(TAG, "login responseJson: " + responseJson);
            this.postNotification(LOGIN_RESPONSE, "success", 1);
        } else {
            this.postNotification(LOGIN_RESPONSE, "success", 0);
        }
    }

    public void getAllChannels(Application application) {
        this.application = application;
        this.context = this.application.getApplicationContext();
        new Thread(new Runnable() {

            @Override
            public void run() {
                makeGetAllChannels();
            }
        }).start();
    }

    public void makeGetAllChannels() {
        String url = getEndpointUrl("channels");
        // http://192.168.170.47:8080/emocracy/api/channels
        //     java.lang.NullPointerException: Attempt to read from field 'int wdh15.emocracy.UserModel.id' on a null object reference
        dataManager = new DataManager(this.context);
        this.userModel = dataManager.getUser();

        url += this.userModel.id;
        Log.v(TAG, "get all channels url constructed: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Get all channels response: " + response);

        if (response != null && response.code() >= 200 && response.code() <= 299) {
            String responseJson = null;
            try {
                responseJson = response.body().string();
                dataManager.setChannels(responseJson);

                //dataManager.setChannelsJson(responseJson);
                //this.userModel = dataManager.getUser();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v(TAG, "get all channels responseJson: " + responseJson);
            this.postNotification(CHANNELS_RESPONSE, "success", 1);
        } else {
            this.postNotification(CHANNELS_RESPONSE, "success", 0);
        }
    }

    // LocalBroadcastManager Helper Methods
    private void postNotification(String intentName, String name, int value) {
        Intent intent = new Intent(intentName);
        intent.putExtra(name, value);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    private void postNotification(String intentName, String name, HashMap<String, String> value) {
        Intent intent = new Intent(intentName);

        JSONObject jsonObject = new JSONObject(value);
        String jsonValue = jsonObject.toString();

        intent.putExtra(name, jsonValue);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }
}
