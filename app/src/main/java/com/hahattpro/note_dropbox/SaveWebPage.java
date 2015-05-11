package com.hahattpro.note_dropbox;

import android.content.Context;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//TODO: add edit text for name
public class SaveWebPage extends ActionBarActivity {

    DropboxAPI<AndroidAuthSession> mApi;
    private String APP_KEY="vbdeavygjun1yyz";
    private String APP_SECRET="q8yqmdihl0cuwtv";
    private String ACCESS_TOKEN=null;

    private EditText editSaveName;
    private EditText InputUrl;
    private TextView textStatus;
    private Button Submit;


    File htmlFile;
    String FILE_NAME;
    String WebAddress;

    String HTMLsource = null;
    //status boolean
    boolean getHTML_status = false; //no getHTML in process

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_web_page);

        //bind view
        editSaveName = (EditText) findViewById(R.id.edit_save_name);
        InputUrl = (EditText) findViewById(R.id.edit_WebAddress);
        textStatus = (TextView) findViewById(R.id.text_SaveHTMLStatus);
        Submit = (Button) findViewById(R.id.button_submit_web);

        // bind APP_KEY and APP_SECRET with session
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        //get the access token
        Intent intent = getIntent();
        ACCESS_TOKEN = intent.getExtras().getString(getResources().getString(R.string.Access_token));//get the access token

        if (!mApi.getSession().isLinked())
        new LoginDropbox().execute();//login




        WebAddress = "http://fruitbatfactory.com/humantanks/story.html";

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FILE_NAME = editSaveName.getText().toString()+".html";
                //WebAddress = InputUrl.getText().toString();
                new GetHTMLsource().execute(WebAddress);//get html and store to dropbox
            }
        });
    }

    //dropbox document ask for this, but still don't know what it is use for ?
    //It MUST have or it will throw com.dropbox.client2.exception.DropboxUnlinkedException
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
        getMenuInflater().inflate(R.menu.menu_save_web_page, menu);
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

    //build AndroidAuthSession
    private AndroidAuthSession buildSession()
    {
        // APP_KEY and APP_SECRET goes here
        AppKeyPair appKeyPair= new AppKeyPair(APP_KEY,APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        return session;
    }
    private class LoginDropbox extends  AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            mApi.getSession().startOAuth2Authentication(SaveWebPage.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class GetHTMLsource extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //A string cointain HTMLsource



            try {
                //the url
                URL url = new URL(params[0]);

                // Create the request to website, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                //write to buffer
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                //  Write to string
                HTMLsource = buffer.toString();

                // write to file
                FileOutputStream fileOutputStream = openFileOutput(FILE_NAME, 0);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
                writer.write(HTMLsource);
                writer.close();
                htmlFile = getFileStreamPath(FILE_NAME);

            }
            catch (MalformedURLException e)
            {

            }
            catch (IOException e)
            {

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            InputUrl.setText(HTMLsource);
             new UploadToDropbox().execute(htmlFile);
        }
    }

    //upload a file into dropbox
    private class UploadToDropbox extends AsyncTask<File,Void,Void>
    {
        Boolean error = false;
        String error_code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textStatus.setText("Uploading");
        }

        @Override
        protected Void doInBackground(File... params) {
            try {
                CreateFile createFile = new CreateFile(FILE_NAME, HTMLsource, SaveWebPage.this);
                File file = createFile.getFile();
                FileInputStream is = new FileInputStream(file);

                //upload to dropbox
                // need json_simple-1.1.jar libs
                Log.i("FILE_NAME",file.getName());
                mApi.putFile("Saved HTML"+"/"+file.getName()// path where file is upload in dropbox ( from root)
                        ,is //InputStream
                        ,file.length() //file length
                        ,null //parentRev
                        ,null);//ProgressListener
            Log.i("FILE_NAME",file.getName());
            }
            catch (FileNotFoundException e)
            {
                Log.e("FileInputStream", e.getMessage());
                error_code=e.getMessage();
                error=true;
            }
            catch (DropboxException e)
            {
                e.printStackTrace();

                error=true;
            }
            finally {
                htmlFile.delete();//clean up
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
