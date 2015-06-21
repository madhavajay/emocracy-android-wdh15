package wdh15.emocracy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by madhavajay on 20/06/15.
 */
public class LoginActivity extends Activity {

    private final static String TAG = "LoginActivity";
    private DataManager dataManager;
    private EditText usernameInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().hide();
        setContentView(R.layout.activity_login);

        dataManager = new DataManager(this);

        usernameInput = (EditText) findViewById(R.id.input_username);
        usernameInput.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);


        // Register LocalBroadcastNotifcations
        LocalBroadcastManager.getInstance(this).registerReceiver(this.messageReceiver, new IntentFilter(NetworkManager.LOGIN_RESPONSE));

        loginButton = (Button) findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "Login button pressed");
                Log.v(TAG, "username is: " + usernameInput.getText());

                String username = usernameInput.getText().toString();
                username = username.trim();

                if (username.equals("") || username.length() == 0) {
                    Log.v(TAG, "no username");
                    Toast.makeText(LoginActivity.this, "Username can't be empty", Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG, "yes we have a username disable button and attempt login");
                    loginButton.setEnabled(false);
                    NetworkManager.getInstance().loginUser(getApplication(), username);
                }
            }
        });
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(NetworkManager.LOGIN_RESPONSE)) {
                int loginSuccess = (int)intent.getIntExtra("success", 0);
                if (loginSuccess == 1) {
                    dataManager.setHasUser(true);
                    finish();
                } else {
                    dataManager.setHasUser(false);
                    loginButton.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Failed to login, please try again", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.messageReceiver);
        super.onDestroy();
    }

}
