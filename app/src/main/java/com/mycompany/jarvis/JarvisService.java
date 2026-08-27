package com.mycompany.jarvis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import java.util.ArrayList;
import java.util.Locale;

public class JarvisService extends Service {
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private PowerManager.WakeLock wakeLock;
    private TextToSpeech textToSpeech;
    private boolean isJarvisSpeaking = false;

    @Override
    public void onCreate() {
        super.onCreate();
        
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Jarvis::WakeLockTag");
        wakeLock.acquire();

        createNotificationChannel();
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "jarvis_channel")
                    .setContentTitle("Jarvis Active")
                    .setContentText("Listening for 'Jarvis'...")
                    .build();
        }
        startForeground(1, notification);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(new Locale("hi", "IN"));
                }
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(android.os.Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0).toLowerCase();
                    if (command.contains("jarvis")) {
                        triggerJarvisResponse(command);
                    }
                }
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onError(int error) {
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override public void onReadyForSpeech(android.os.Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(android.os.Bundle partialResults) {}
            @Override public void onEvent(int eventType, android.os.Bundle params) {}
        });

        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void triggerJarvisResponse(String command) {
        if (isJarvisSpeaking) return; 
        textToSpeech.speak("Yes sir?", TextToSpeech.QUEUE_FLUSH, null, null);
        
        String cleanQuery = command.replace("jarvis", "").trim();
        if (cleanQuery.isEmpty()) cleanQuery = "hello";

        GeminiClient.askJarvis(cleanQuery, new GeminiClient.GeminiResponseListener() {
            @Override
            public void onResponseReceived(String responseText) {
                isJarvisSpeaking = true;
                textToSpeech.speak(responseText, TextToSpeech.QUEUE_FLUSH, null, "JarvisReply");
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                        @Override public void onStart(String utteranceId) {}
                        @Override public void onDone(String utteranceId) { isJarvisSpeaking = false; }
                        @Override public void onError(String utteranceId) { isJarvisSpeaking = false; }
                    });
                }
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "jarvis_channel", "Jarvis Service", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        if (speechRecognizer != null) speechRecognizer.destroy();
        if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); }
    }
}
