package com.jarvis.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextToSpeech tts;
    private final String GROQ_API_KEY = "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";
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Text To Speech Initialization
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("hi", "IN"));
                checkAudioPermissionAndStart();
            }
        });
    }

    private void checkAudioPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        } else {
            setupVoiceEngine();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupVoiceEngine();
        } else {
            Toast.makeText(this, "Microphone permission is required for JARVIS", Toast.LENGTH_LONG).show();
        }
    }

    private void setupVoiceEngine() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition not available on this device", Toast.LENGTH_LONG).show();
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String query = matches.get(0).toLowerCase();
                    Toast.makeText(MainActivity.this, "Heard: " + query, Toast.LENGTH_SHORT).show();
                    handleQuery(query);
                } else {
                    restartListening(1000);
                }
            }

            @Override
            public void onError(int error) {
                isListening = false;
                restartListening(1200);
            }

            @Override public void onReadyForSpeech(Bundle params) { isListening = true; }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() { isListening = false; }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        speak("JARVIS is fully online and listening, Sir.");
        restartListening(1500);
    }

    private void startListening() {
        if (speechRecognizer != null && !isListening) {
            try {
                speechRecognizer.startListening(speechRecognizerIntent);
            } catch (Exception ignored) {}
        }
    }

    private void restartListening(long delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(this::startListening, delayMillis);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_TTS");
        }
    }

    private void handleQuery(String cmd) {
        if (cmd.contains("youtube") && (cmd.contains("chala") || cmd.contains("play") || cmd.contains("open"))) {
            speak("Opening YouTube, Sir.");
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
            if (launchIntent != null) startActivity(launchIntent);
            restartListening(3000);
        } else if (cmd.contains("whatsapp")) {
            speak("Opening WhatsApp, Sir.");
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");
            if (launchIntent != null) startActivity(launchIntent);
            restartListening(3000);
        } else {
            askGroqAI(cmd);
        }
    }

    private void askGroqAI(String prompt) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + GROQ_API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("model", "llama-3.3-70b-versatile");
                JSONArray messages = new JSONArray();

                JSONObject sys = new JSONObject();
                sys.put("role", "system");
                sys.put("content", "You are JARVIS. Answer naturally in 1-2 crisp spoken sentences in Hindi or Hinglish without markdown.");
                messages.put(sys);

                JSONObject usr = new JSONObject();
                usr.put("role", "user");
                usr.put("content", prompt);
                messages.put(usr);

                payload.put("messages", messages);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder res = new StringBuilder();
                while (scanner.hasNext()) res.append(scanner.nextLine());
                scanner.close();

                JSONObject resObj = new JSONObject(res.toString());
                String reply = resObj.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                runOnUiThread(() -> {
                    speak(reply);
                    restartListening(4000);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    speak("Main samajh gaya Sir, batayein aage kya karna hai.");
                    restartListening(2000);
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}

