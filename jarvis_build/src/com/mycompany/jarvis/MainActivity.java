package com.mycompany.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Toast.makeText(this, "Initializing Jarvis...", Toast.LENGTH_SHORT).show();

        // माइक्रोफोन की परमिशन चेक करना
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                startJarvisService();
            }
        } else {
            startJarvisService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startJarvisService();
        } else {
            Toast.makeText(this, "Sir, जार्विस को काम करने के लिए माइक्रोफोन की अनुमति चाहिए।", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startJarvisService() {
        Intent serviceIntent = new Intent(this, JarvisService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "Jarvis is now active, sir!", Toast.LENGTH_SHORT).show();
        finish(); // परमिशन मिलते ही ऐप बंद हो जाएगा पर बैकग्राउंड में चालू रहेगा
    }
}
