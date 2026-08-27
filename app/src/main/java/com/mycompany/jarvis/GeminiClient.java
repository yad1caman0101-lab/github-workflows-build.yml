package com.mycompany.jarvis;

import android.os.AsyncTask;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiClient {
    private static final String API_KEY = "AQ.Ab8RN6IX2L2PodtGzlstcB-9SKuPwUrHiwW30t5-InezdlYjpw";
    private static final String API_URL = "https://googleapis.com" + API_KEY;

    public interface GeminiResponseListener {
        void onResponseReceived(String responseText);
    }

    public static void askJarvis(String userQuery, final GeminiResponseListener listener) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    JSONObject jsonBody = new JSONObject();
                    JSONArray contentsArray = new JSONArray();
                    JSONObject contentObj = new JSONObject();
                    JSONArray partsArray = new JSONArray();
                    JSONObject partObj = new JSONObject();

                    partObj.put("text", "You are Jarvis, an advanced AI assistant. Answer briefly in Hinglish (mixed Hindi and English) like Tony Stark's AI. User command: " + params[0]);
                    partsArray.put(partObj);
                    contentObj.put("parts", partsArray);
                    contentsArray.put(contentObj);
                    jsonBody.put("contents", contentsArray);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                    os.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    return jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Sorry sir, सर्वर से कनेक्शन नहीं हो पा रहा है।";
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (listener != null) {
                    listener.onResponseReceived(result);
                }
            }
        }.execute(userQuery);
    }
}
