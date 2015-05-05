package com.hahattpro.note_dropbox;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;


public class MainActivity extends ActionBarActivity {

    DropboxAPI<AndroidAuthSession> mApi;
    private String APP_KEY="vbdeavygjun1yyz";
    private String APP_SECRET="q8yqmdihl0cuwtv";

    Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidAuthSession session = buildSession(); // bind APP_KEY and APP_SECRET with session

        mApi = new DropboxAPI<AndroidAuthSession>(session);

        mLogin = (Button) findViewById(R.id.button_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click here will lead to dropbox login page
                mApi.getSession().startOAuth2Authentication(MainActivity.this);
            }
        });

    }

    protected void onResume() {
        super.onResume();

        if (mApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mApi.getSession().finishAuthentication();
                String accessToken = mApi.getSession().getOAuth2AccessToken();
                //accessToken should be save somewhere
                //TODO: accessToken ?



                Log.i("DbAuthLog","Login successful");
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ///////>>>>>>HELPER FUNCTION<<<<<<///////////////////

    //build AndroidAuthSession
    private AndroidAuthSession buildSession()
    {
        // APP_KEY and APP_SECRET goes here
        AppKeyPair appKeyPair= new AppKeyPair(APP_KEY,APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        return session;
    }

}
