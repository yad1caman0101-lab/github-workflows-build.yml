package com.mycompany.jarvis;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class JarvisOverlayService extends Service {
    private WindowManager wm;
    private LinearLayout overlayLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayLayout = new LinearLayout(this);
        overlayLayout.setOrientation(LinearLayout.VERTICAL);
        overlayLayout.setGravity(Gravity.CENTER);
        overlayLayout.setPadding(30, 20, 30, 20);

        // Glowing Blue Circular Arc Design
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.parseColor("#E60F172A"));
        shape.setStroke(6, Color.parseColor("#00E5FF"));
        overlayLayout.setBackground(shape);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("⚡ HARBOUR AI ⚡");
        tvTitle.setTextColor(Color.parseColor("#00E5FF"));
        tvTitle.setTextSize(16);
        tvTitle.setGravity(Gravity.CENTER);

        TextView tvSub = new TextView(this);
        tvSub.setText("Listening...");
        tvSub.setTextColor(Color.WHITE);
        tvSub.setTextSize(12);
        tvSub.setGravity(Gravity.CENTER);

        overlayLayout.addView(tvTitle);
        overlayLayout.addView(tvSub);

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.y = 120;

        try {
            wm.addView(overlayLayout, params);
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayLayout != null && wm != null) {
            try {
                wm.removeView(overlayLayout);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
