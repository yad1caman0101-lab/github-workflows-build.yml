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
