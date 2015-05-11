package com.hahattpro.note_dropbox;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class MainActivity extends ActionBarActivity {

    DropboxAPI<AndroidAuthSession> mApi;
    private String APP_KEY="vbdeavygjun1yyz";
    private String APP_SECRET="q8yqmdihl0cuwtv";
    private String ACCESS_TOKEN;

    Button mLogin;//login dropbox
    Button mSubmit;//upload file
    EditText textTitle;//file name (without extension)
    EditText textBody;//file body
    TextView textStatus;//show upload progress
    Button mSaveWebPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

           //bind view to layout
        mLogin = (Button) findViewById(R.id.button_login);
        mSubmit = (Button) findViewById(R.id.button_submit);
        textTitle = (EditText) findViewById(R.id.text_title);
        textBody = (EditText) findViewById(R.id.text_body);
        textStatus = (TextView) findViewById(R.id.text_Status);
        mSaveWebPage = (Button) findViewById(R.id.button_ToWebPage);

        // bind APP_KEY and APP_SECRET with session
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click here will lead to dropbox login page
                new LoginDropbox().execute();
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            //Click to upload note to dropbox
            public void onClick(View v) {
                new UploadToDropbox().execute();
            }
        });

        mSaveWebPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SaveWebPage.class);
                intent.putExtra(getResources().getString(R.string.Access_token),ACCESS_TOKEN);
                startActivity(intent);
            }
        });
    }

    //dropbox document ask for this, but still don't know what it is use for ?
    protected void onResume() {
        super.onResume();
        if (mApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mApi.getSession().finishAuthentication();
                ACCESS_TOKEN = mApi.getSession().getOAuth2AccessToken();
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


    //open browser, login, ask for permission
    private class LoginDropbox extends  AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            mApi.getSession().startOAuth2Authentication(MainActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    //upload a file into dropbox
    private class UploadToDropbox extends AsyncTask<Void,Void,Void>
    {
        Boolean error = false;
        String error_code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textStatus.setText("start upload");
        }

        @Override



        protected Void doInBackground(Void... params) {

            String FILE_NAME= textTitle.getText().toString();
            String body = textBody.getText().toString();
            CreateFile createFile=null;
            try {
                //create a file with name Title.txt and body
                 createFile = new CreateFile(FILE_NAME, body, MainActivity.this);
                File file = createFile.getFile();
                FileInputStream is = new FileInputStream(file);

                //upload to dropbox
                // need json_simple-1.1.jar libs
                mApi.putFile("My Spam Note"+"/"+file.getName()// path where file is upload in dropbox ( from root)
                        ,is //InputStream
                        ,file.length() //file length
                        ,null //parentRev
                        ,null);//ProgressListener

            }
            catch (FileNotFoundException e)
            {
                Log.e("FileInputStream",e.getMessage());
                error_code=e.getMessage();
                error=true;
            }
            catch (DropboxException e)
            {
                Log.e("Dropbox error",e.getMessage());
                error_code=e.getMessage();
                error=true;
            }
            finally {
                createFile.CleanUp();//delete temp file
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (error)
                textStatus.setText("error "+error_code);
                else
            textStatus.setText("done upload");

        }
    }

}
