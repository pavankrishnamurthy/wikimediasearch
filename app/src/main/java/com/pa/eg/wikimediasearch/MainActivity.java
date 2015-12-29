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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    Button searchButton;
    EditText searchString;
    ProgressDialog progressDialog;
    TextView webResponse;
    ArrayList<HashMap<String, String>> pageList;
    GridView lv;
    String baseUrl = "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=thumbnail&pithumbsize=50&pilimit=100&generator=prefixsearch&gpssearch=";
    int activeSearch = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchButton = (Button)findViewById(R.id.search_button);
        searchString = (EditText) findViewById(R.id.search_string);
        webResponse = (TextView) findViewById(R.id.responseString);
        lv = (GridView)findViewById(R.id.gridView);

        searchString.addTextChangedListener(new TextWatcher() {
            Timer timer;
            long DELAY = 2000;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("MainAcitivity","beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("MainAcitivity","onTextChanged");
                if (timer != null) {
                    Log.d("MainAcitivity","onTextChanged timer cancelled");
                    timer.cancel();
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {
                final String searchString = s.toString();
                Log.d("MainAcitivity","afterTextChanged searchString" + searchString);

                if (searchString.length() >= 3) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable () {
                                @Override
                                public void run() {
                                    Log.d("MainActivity", "runnable activesearch : " +
                                            activeSearch + " string: " + searchString);
                                    if (activeSearch == 0 ) {
                                        Toast msg = Toast.makeText(getApplicationContext(),
                                                "searching string: " + searchString,
                                                Toast.LENGTH_LONG);
                                        msg.show();
                                        searchWikiImages(searchString);
                                    }
                                }
                            });
                        }

                    }, DELAY);
                }
            }
        });

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
        String searchWord = searchString.getText().toString();
        searchWikiImages(searchWord);
    }

    public void searchWikiImages(String searchWord) {
        android.util.Log.d("MainActivity","Searching:  " + searchWord);
        String wikiUrl = null;
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
        if (wikiUrl != null ) {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpage().execute(wikiUrl);
                activeSearch++;
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
            //searchActive = true;
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Downloading ...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... urls) {
            WebDownloader webDownloader = new WebDownloader();
            String httpResponse;
            String result = "Images downloaded";
            try {
                httpResponse = webDownloader.downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
            if(httpResponse != null) {
                result = parseJson(httpResponse);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            webResponse.setText(result);

            ImageViewAdapter adapter = new ImageViewAdapter(getApplicationContext(),pageList);
            lv.setAdapter(adapter);
            activeSearch--;
        }
    }

    public String parseJson(String jsonString) {
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
                    pageMap.put("imageSource","file:///android_asset/noimage.png");
                }
                pageList.add(pageMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "Unable to retrieve web page. Search String may be invalid.";
        }

        return "Wiki pages downloaded";
    }
}
