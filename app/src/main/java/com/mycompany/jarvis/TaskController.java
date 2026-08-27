package com.mycompany.jarvis;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.provider.ContactsContract;

public class TaskController {
    private static boolean isTorchOn = false;

    public static boolean handleCommand(Context context, String query, Runnable onNoIntentMatch) {
        if (query == null || query.trim().isEmpty()) return false;
        String cmd = query.toLowerCase().trim();

        // 1. Torch / Flashlight Control
        if (cmd.contains("torch") || cmd.contains("flashlight") || cmd.contains("light")) {
            if (cmd.contains("on") || cmd.contains("chalao") || cmd.contains("jalao")) {
                setTorch(context, true);
                return true;
            } else if (cmd.contains("off") || cmd.contains("band")) {
                setTorch(context, false);
                return true;
            }
        }

        // 2. YouTube Automation
        if (cmd.contains("youtube") || cmd.contains("gana") || cmd.contains("song") || cmd.contains("play")) {
            String q = cmd.replace("youtube", "").replace("par", "").replace("play", "").replace("karo", "").replace("chalao", "").trim();
            if (q.isEmpty()) q = "bhojpuri song";
            try {
                Intent ytIntent = new Intent(Intent.ACTION_SEARCH);
                ytIntent.setPackage("com.google.android.youtube");
                ytIntent.putExtra("query", q);
                ytIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ytIntent);
                return true;
            } catch (Exception e) {
                Intent webYt = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(q)));
                webYt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webYt);
                return true;
            }
        }

        // 3. Contact Call
        if (cmd.contains("call") || cmd.contains("lagao") || cmd.contains("milao")) {
            String name = cmd.replace("call", "").replace("ko", "").replace("karo", "").replace("lagao", "").trim();
            String number = findContactNumber(context, name);
            if (number != null) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(callIntent);
                return true;
            }
        }

        // 4. Launch Any App
        if (cmd.contains("open") || cmd.contains("kholo")) {
            String pkg = null;
            if (cmd.contains("whatsapp")) pkg = "com.whatsapp";
            else if (cmd.contains("instagram")) pkg = "com.instagram.android";
            else if (cmd.contains("free fire")) pkg = "com.dts.freefireth";
            else if (cmd.contains("chrome")) pkg = "com.android.chrome";
            else if (cmd.contains("camera")) pkg = "com.android.camera";
            else if (cmd.contains("capcut")) pkg = "com.lemon.lvoverseas";

            if (pkg != null) {
                Intent appIntent = context.getPackageManager().getLaunchIntentForPackage(pkg);
                if (appIntent != null) {
                    appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(appIntent);
                    return true;
                }
            }
        }

        if (onNoIntentMatch != null) {
            onNoIntentMatch.run();
        }
        return false;
    }

    public static void setTorch(Context context, boolean state) {
        try {
            CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = cm.getCameraIdList()[0];
            cm.setTorchMode(cameraId, state);
            isTorchOn = state;
        } catch (Exception ignored) {}
    }

    private static String findContactNumber(Context context, String name) {
        String phone = null;
        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = context.getContentResolver().query(
                uri, projection,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                new String[]{"%" + name + "%"},
                null
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) phone = cursor.getString(0);
                cursor.close();
            }
        } catch (Exception ignored) {}
        return phone;
    }
}
