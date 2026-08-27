package com.mycompany.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private TextView tvStatus, tvResponse;
    private Button btnSpeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow to open over Lock Screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#050B14"));
        layout.setPadding(60, 60, 60, 60);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("⚡ HARBOUR / JARVIS ⚡");
        tvTitle.setTextSize(24);
        tvTitle.setTextColor(Color.parseColor("#00E5FF"));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, 40);

        tvStatus = new TextView(this);
        tvStatus.setText("Tap button or say 'Harbour'...");
        tvStatus.setTextSize(16);
        tvStatus.setTextColor(Color.WHITE);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(0, 0, 0, 20);

        tvResponse = new TextView(this);
        tvResponse.setText("");
        tvResponse.setTextSize(14);
        tvResponse.setTextColor(Color.parseColor("#38BDF8"));
        tvResponse.setGravity(Gravity.CENTER);
        tvResponse.setPadding(0, 0, 0, 40);

        btnSpeak = new Button(this);
        btnSpeak.setText("🎙️ ACTIVATE ASSISTANT");
        btnSpeak.setTextSize(18);
        btnSpeak.setBackgroundColor(Color.parseColor("#0284C7"));
        btnSpeak.setTextColor(Color.WHITE);
        btnSpeak.setPadding(30, 30, 30, 30);

        layout.addView(tvTitle);
        layout.addView(tvStatus);
        layout.addView(tvResponse);
        layout.addView(btnSpeak);
        setContentView(layout);

        // Overlay Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA
            }, 101);
        }

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("hi", "IN"));
            }
        });

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle bundle) {
                stopService(new Intent(MainActivity.this, JarvisOverlayService.class));
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    String query = data.get(0);
                    tvStatus.setText("Command: " + query);
                    
                    TaskController.handleCommand(MainActivity.this, query, () -> {
                        tvResponse.setText("Jarvis Thinking...");
                        GeminiClient.askGemini(query, new GeminiClient.Callback() {
                            @Override
                            public void onResponse(String reply) {
                                runOnUiThread(() -> {
                                    tvResponse.setText("AI: " + reply);
                                    tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, null);
                                });
                            }
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    tts.speak("Aapki request process ho gayi.", TextToSpeech.QUEUE_FLUSH, null, null);
                                });
                            }
                        });
                    });
                }
            }
            @Override public void onReadyForSpeech(Bundle bundle) {
                tvStatus.setText("Listening...");
                startService(new Intent(MainActivity.this, JarvisOverlayService.class));
            }
            @Override public void onError(int i) {
                stopService(new Intent(MainActivity.this, JarvisOverlayService.class));
                tvStatus.setText("Ready");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float v) {}
            @Override public void onBufferReceived(byte[] bytes) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle bundle) {}
            @Override public void onEvent(int i, Bundle bundle) {}
        });

        btnSpeak.setOnClickListener(v -> startListening());
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        recognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (recognizer != null) { recognizer.destroy(); }
        stopService(new Intent(this, JarvisOverlayService.class));
        super.onDestroy();
    }
}
