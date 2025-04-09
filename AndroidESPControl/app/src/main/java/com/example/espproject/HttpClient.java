package com.example.espproject;

import android.os.AsyncTask;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {
    public interface HttpResponseCallback {
        void onResponse(String response);
        void onError(Exception e);
    }

    public static void sendGetRequest(String urlString, HttpResponseCallback callback) {
        new AsyncTask<Void, Void, String>() {
            Exception exception = null;

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        return "Success";
                    } else {
                        return "Error: " + responseCode;
                    }
                } catch (Exception e) {
                    exception = e;
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (exception != null) {
                    callback.onError(exception);
                } else {
                    callback.onResponse(result);
                }
            }
        }.execute();
    }
}
