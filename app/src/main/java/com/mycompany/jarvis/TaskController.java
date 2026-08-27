package com.mycompany.jarvis;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class TaskController {

    public static boolean handleCommand(Context context, String query) {
        if (query == null) return false;
        String command = query.toLowerCase().trim();

        // 1. YouTube Song Search & Play
        if (command.contains("youtube") || command.contains("gana") || command.contains("song")) {
            String searchQuery = command.replace("youtube", "")
                                        .replace("par", "")
                                        .replace("play", "")
                                        .replace("karo", "")
                                        .replace("chalao", "").trim();
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.setPackage("com.google.android.youtube");
            intent.putExtra("query", searchQuery);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(searchQuery)));
                webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webIntent);
                return true;
            }
        }

        // 2. Direct Contact Call
        if (command.contains("call") || command.contains("lagao")) {
            String name = command.replace("call", "")
                                 .replace("ko", "")
                                 .replace("karo", "")
                                 .replace("lagao", "")
                                 .replace("karein", "").trim();
            String number = getPhoneNumber(context, name);
            if (number != null) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(callIntent);
                return true;
            }
        }

        // 3. Any App Open
        if (command.contains("open") || command.contains("kholo")) {
            String appName = command.replace("open", "").replace("kholo", "").replace("app", "").trim();
            String pkg = getAppPackage(appName);
            if (pkg != null) {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return true;
                }
            }
        }

        return false;
    }

    private static String getPhoneNumber(Context context, String name) {
        String number = null;
        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(uri, projection, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?", new String[]{"%" + name + "%"}, null);
            if (cursor != null && cursor.moveToFirst()) {
                number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                cursor.close();
            }
        } catch (Exception ignored) {}
        return number;
    }

    private static String getAppPackage(String appName) {
        if (appName.contains("whatsapp")) return "com.whatsapp";
        if (appName.contains("instagram")) return "com.instagram.android";
        if (appName.contains("free fire")) return "com.dts.freefireth";
        if (appName.contains("chrome")) return "com.android.chrome";
        if (appName.contains("capcut")) return "com.lemon.lvoverseas";
        return "com.google.android.youtube";
    }
}
