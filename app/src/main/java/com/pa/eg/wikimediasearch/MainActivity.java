package com.pa.eg.wikimediasearch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends ActionBarActivity {

    Button searchButton;
    EditText searchString;
    ProgressDialog progressDialog;
    TextView webResponse;
    ArrayList<HashMap<String, String>> pageList;
    GridView lv;
    String baseUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=200&pilimit=100&generator=prefixsearch&gpssearch=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchButton = (Button)findViewById(R.id.search_button);
        searchString = (EditText) findViewById(R.id.search_string);
        webResponse = (TextView) findViewById(R.id.responseString);
        lv = (GridView)findViewById(R.id.gridView);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSearchButtonClicked (View view) {
        android.util.Log.d("MainActivity","Searching .. ");
        String searchWord = null;
        String wikiUrl = null;
        searchWord = searchString.getText().toString();
        pageList = new ArrayList<HashMap<String, String>>();

        if(searchWord.matches("")) {
            Toast msg = Toast.makeText(getApplicationContext(), "Please enter search string",
                    Toast.LENGTH_LONG);
            msg.show();
            return;
        } else {
            wikiUrl = baseUrl+searchWord;
            android.util.Log.d("MainActivity","Searching: "+wikiUrl);
        }

        hideKeyboard(this);
        if (wikiUrl !=null ) {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpage().execute(wikiUrl);
            } else {
                webResponse.setText("No network connection available.");
            }
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class DownloadWebpage extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... urls) {
            WebDownloader webDownloader = new WebDownloader();
            String response;
            try {
                response = webDownloader.downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
            if(response != null) {
                parseJson(response);
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            webResponse.setText(" Images downloaded ");

            ImageViewAdapter adapter = new ImageViewAdapter(getApplicationContext(),pageList);
            lv.setAdapter(adapter);
        }
    }

    public void parseJson(String jsonString) {
        try {
            JSONObject jsonObj = new JSONObject(jsonString);

            JSONObject query = jsonObj.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");

            Iterator<String> pageIt = pages.keys();
            while (pageIt.hasNext()) {
                String key = pageIt.next();
                android.util.Log.d("MainActivity", "PageID: " + key);

                HashMap<String, String> pageMap = new HashMap<String, String>();
                pageMap.put("pageid",key);

                JSONObject page = pages.getJSONObject(key);
                String pageTitle = page.getString("title");
                pageMap.put("title", pageTitle);
                android.util.Log.d("MainActivity","page title: " + pageTitle);

                JSONObject thumbnailObj = page.optJSONObject("thumbnail");
                if (thumbnailObj != null) {
                    String imageUrl = thumbnailObj.getString("source");
                    android.util.Log.d("MainActivity","Image Source: " + imageUrl);
                    pageMap.put("imageSource",imageUrl);

                } else {
                    android.util.Log.d("MainActivity","Image Source: NULL");
                    pageMap.put("imageSource","file:///android_asset/index.png");
                }
                pageList.add(pageMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //todo end the processing here
        }
    }
}
