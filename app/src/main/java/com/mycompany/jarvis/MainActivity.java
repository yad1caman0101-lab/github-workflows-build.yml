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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#070D18"));
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("⚡ JARVIS CORE ⚡");
        title.setTextSize(26);
        title.setTextColor(Color.parseColor("#00E5FF"));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);

        tvStatus = new TextView(this);
        tvStatus.setText("Checking permissions...");
        tvStatus.setTextColor(Color.WHITE);
        tvStatus.setTextSize(16);
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(0, 0, 0, 30);

        Button btnPerm = new Button(this);
        btnPerm.setText("1. Grant Overlay Permission");
        btnPerm.setBackgroundColor(Color.parseColor("#0284C7"));
        btnPerm.setTextColor(Color.WHITE);
        btnPerm.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });

        Button btnSpeak = new Button(this);
        btnSpeak.setText("🎙️ Test Voice & Neon Smoke");
        btnSpeak.setBackgroundColor(Color.parseColor("#10B981"));
        btnSpeak.setTextColor(Color.WHITE);
        btnSpeak.setPadding(0, 20, 0, 20);
        btnSpeak.setOnClickListener(v -> startVoiceRecognition());

        layout.addView(title);
        layout.addView(tvStatus);
        layout.addView(btnPerm);
        layout.addView(btnSpeak);
        setContentView(layout);

        checkAndRequestPermissions();

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
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String query = matches.get(0);
                    tvStatus.setText("User: " + query);
                    boolean done = TaskController.handleCommand(MainActivity.this, query, () -> {
                        tts.speak("Aapki request process ki ja rahi hai.", TextToSpeech.QUEUE_FLUSH, null, null);
                    });
                }
            }
            @Override public void onReadyForSpeech(Bundle bundle) {
                tvStatus.setText("Listening...");
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(MainActivity.this)) {
                    startService(new Intent(MainActivity.this, JarvisOverlayService.class));
                }
            }
            @Override public void onError(int i) {
                stopService(new Intent(MainActivity.this, JarvisOverlayService.class));
                tvStatus.setText("Ready. Tap to speak.");
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float v) {}
            @Override public void onBufferReceived(byte[] bytes) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle bundle) {}
            @Override public void onEvent(int i, Bundle bundle) {}
        });
    }

    private void checkAndRequestPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA
            }, 100);
        }
    }

    private void startVoiceRecognition() {
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
