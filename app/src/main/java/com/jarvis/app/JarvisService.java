package com.jarvis.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.Locale;

public class JarvisService extends Service {
    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private TextToSpeech tts;
    private boolean isListening = false;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundServiceNotification();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("hi", "IN"));
                speak("JARVIS is online, Sir.");
                initSpeechRecognizer();
            }
        });
    }

    private void startForegroundServiceNotification() {
        String channelId = "JARVIS_SERVICE_CHANNEL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "JARVIS Engine", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("JARVIS Active")
                .setContentText("Listening for commands...")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .build();

        startForeground(1, notification);
    }

    private void initSpeechRecognizer() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());

                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onResults(Bundle results) {
                        isListening = false;
                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            processCommand(matches.get(0).toLowerCase());
                        } else {
                            restartListening(1000);
                        }
                    }

                    @Override
                    public void onError(int error) {
                        isListening = false;
                        restartListening(1500);
                    }

                    @Override public void onReadyForSpeech(Bundle params) { isListening = true; }
                    @Override public void onBeginningOfSpeech() {}
                    @Override public void onRmsChanged(float rmsdB) {}
                    @Override public void onBufferReceived(byte[] buffer) {}
                    @Override public void onEndOfSpeech() { isListening = false; }
                    @Override public void onPartialResults(Bundle partialResults) {}
                    @Override public void onEvent(int eventType, Bundle params) {}
                });

                startListening();
            }
        });
    }

    private void startListening() {
        if (speechRecognizer != null && !isListening) {
            try {
                speechRecognizer.startListening(speechIntent);
            } catch (Exception ignored) {}
        }
    }

    private void restartListening(long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(this::startListening, delay);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JARVIS_VOICE");
        }
    }

    private void processCommand(String cmd) {
        if (cmd.contains("youtube") && (cmd.contains("play") || cmd.contains("chala"))) {
            speak("Opening YouTube, Sir.");
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
            }
            restartListening(3000);
        } else {
            GeminiClient.generateContent(cmd, new GeminiClient.Callback() {
                @Override
                public void onResponse(String text) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        speak(text);
                        restartListening(4000);
                    });
                }

                @Override
                public void onError(String error) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        speak("Main samajh gaya Sir.");
                        restartListening(2000);
                    });
                }
            });
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
