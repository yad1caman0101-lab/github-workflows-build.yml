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
EOF

# 5. Push to GitHub
git add .
git commit -m "Add Flashlight control, LockScreen display and Floating Arc Overlay"
git push -u origin main --force
cd $HOME/jarvis_github
# 1. Check status aur force stage
git status
git add -A
# 2. Commit create karein
git commit -m "Update Jarvis with Torch, Overlay and Wake Features" --allow-empty
# 3. Token URL set karke force push karein
git remote set-url origin https://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@github.com/yad1caman0101-lab/github-workflows-build.yml.git
git push -u origin main --force
cd $HOME/jarvis_github
# 1. Manifest with All Permissions + Overlay + Foreground Service
cat << 'EOF' > app/src/main/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.mycompany.jarvis">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.FLASHLIGHT" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />

    <application
        android:label="Jarvis AI"
        android:allowBackup="true"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".JarvisOverlayService"
            android:foregroundServiceType="microphone"
            android:enabled="true"
            android:exported="false" />

    </application>
</manifest>
EOF

# 2. Glowing Neon Arc Reactor + Smoke Particle Overlay View
cat << 'EOF' > app/src/main/java/com/mycompany/jarvis/NeonSmokeView.java
package com.mycompany.jarvis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class NeonSmokeView extends View {
    private Paint ringPaint, glowPaint, smokePaint;
    private float pulseRadius = 100f;
    private boolean expanding = true;
    private final ArrayList<SmokeParticle> particles = new ArrayList<>();
    private final Random rand = new Random();

    public NeonSmokeView(Context context) {
        super(context);
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(8f);
        ringPaint.setColor(Color.parseColor("#00E5FF"));

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(22f);
        glowPaint.setColor(Color.parseColor("#4D00E5FF"));

        smokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private static class SmokeParticle {
        float x, y, vx, vy, radius;
        int alpha;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // 1. Emit Smoke Particles
        if (particles.size() < 40) {
            SmokeParticle p = new SmokeParticle();
            double angle = rand.nextDouble() * 2 * Math.PI;
            p.x = (float) (cx + Math.cos(angle) * pulseRadius);
            p.y = (float) (cy + Math.sin(angle) * pulseRadius);
            p.vx = (float) (Math.cos(angle) * (1 + rand.nextFloat() * 2));
            p.vy = (float) (Math.sin(angle) * (1 + rand.nextFloat() * 2) - 1.5f);
            p.radius = 25f + rand.nextFloat() * 30f;
            p.alpha = 140;
            particles.add(p);
        }

        // 2. Draw Smoke Particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            SmokeParticle p = particles.get(i);
            p.x += p.vx;
            p.y += p.vy;
            p.radius += 0.8f;
            p.alpha -= 4;

            if (p.alpha <= 0) {
                particles.remove(i);
                continue;
            }

            smokePaint.setColor(Color.parseColor("#00E5FF"));
            smokePaint.setAlpha(p.alpha / 3);
            smokePaint.setShader(new RadialGradient(p.x, p.y, p.radius, 
                    Color.argb(p.alpha, 0, 229, 255), 
                    Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawCircle(p.x, p.y, p.radius, smokePaint);
        }

        // 3. Draw Neon Rings
        canvas.drawCircle(cx, cy, pulseRadius, glowPaint);
        canvas.drawCircle(cx, cy, pulseRadius, ringPaint);

        // Pulsing Animation
        if (expanding) {
            pulseRadius += 1.2f;
            if (pulseRadius > 120f) expanding = false;
        } else {
            pulseRadius -= 1.2f;
            if (pulseRadius < 90f) expanding = true;
        }

        postInvalidateDelayed(16);
    }
}
EOF

# 3. Jarvis Overlay Service
cat << 'EOF' > app/src/main/java/com/mycompany/jarvis/JarvisOverlayService.java
package com.mycompany.jarvis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class JarvisOverlayService extends Service {
    private WindowManager wm;
    private FrameLayout container;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundServiceNotification();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        container = new FrameLayout(this);

        NeonSmokeView neonView = new NeonSmokeView(this);
        container.addView(neonView, new FrameLayout.LayoutParams(600, 600));

        TextView tv = new TextView(this);
        tv.setText("⚡ HARBOUR AI ⚡\nListening...");
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        tvParams.gravity = Gravity.CENTER;
        container.addView(tv, tvParams);

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                600, 600,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = 80;

        try {
            wm.addView(container, params);
        } catch (Exception ignored) {}
    }

    private void startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("jarvis_overlay", "Jarvis Active", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, "jarvis_overlay")
                    .setContentTitle("Jarvis Engine Active")
                    .setContentText("Listening for commands")
                    .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                    .build();
            startForeground(101, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (container != null && wm != null) {
            try {
                wm.removeView(container);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
EOF

# 4. MainActivity with Strict Permission Gateway
cat << 'EOF' > app/src/main/java/com/mycompany/jarvis/MainActivity.java
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
EOF

# 5. Push All Files to GitHub
git add -A
git commit -m "Add Neon Smoke Particle Canvas, Torch, Call & Overlay Gateway" --allow-empty
git remote set-url origin https://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@github.com/yad1caman0101-lab/github-workflows-build.yml.git
git push -u origin main --force
