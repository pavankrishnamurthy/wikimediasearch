package com.pa.eg.wikimediasearch;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pavan on 12/25/15.
 */
public class WebDownloader {

    public String downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("WebDownLoader", "The response is: " + response);
            is = conn.getInputStream();

            String contentAsString = getString(is);
            Log.d("WebDownloader",contentAsString);
            return contentAsString;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String getString (InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedInputStream bis = null;
        String response;
        try {
            bis = new BufferedInputStream(is);
            int buf;
            while ((buf = bis.read()) != -1) {
                baos.write(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
        response = new String(baos.toByteArray(),"UTF-8");
        return response;
    }

}
