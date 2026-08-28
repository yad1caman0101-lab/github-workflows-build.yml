package com.jarvis.app;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GeminiClient {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public interface Callback {
        void onResponse(String text);
        void onError(String error);
    }

    public static void generateContent(String prompt, Callback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("model", "llama-3.3-70b-versatile");

                JSONArray messages = new JSONArray();
                JSONObject sys = new JSONObject();
                sys.put("role", "system");
                sys.put("content", "You are JARVIS. Respond strictly in 1-2 crisp spoken sentences in Hindi or Hinglish without markdown formatting.");
                messages.put(sys);

                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", prompt);
                messages.put(userMsg);

                payload.put("messages", messages);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject resJson = new JSONObject(response.toString());
                String reply = resJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                callback.onResponse(reply.trim());

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }
}
