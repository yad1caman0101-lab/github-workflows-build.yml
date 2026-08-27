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
