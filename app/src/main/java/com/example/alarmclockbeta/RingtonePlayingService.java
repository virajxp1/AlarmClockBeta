package com.example.alarmclockbeta;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;

public class RingtonePlayingService extends Service {

    MediaPlayer mediaSong;
    boolean musicPlaying;
    int startId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        String state = intent.getExtras().getString("extra");

        assert state != null;
        switch (state) {
            case "off":
                startId = 0;
                break;
            case "on":
                startId = 1;
                break;
            default:
                startId = 0;
                break;
        }

        if (!this.musicPlaying && startId == 1) {
            mediaSong = MediaPlayer.create(this, R.raw.alarm);
            mediaSong.start();

            this.musicPlaying = true;
            this.startId = 0;
        } else if (this.musicPlaying && startId == 0) {
            mediaSong.stop();
            mediaSong.reset();

            this.musicPlaying = false;
            this.startId = 0;
        } else if (!this.musicPlaying && startId == 0) {
            this.musicPlaying = false;
            this.startId = 0;
        } else if (this.musicPlaying && startId == 1) {
            this.musicPlaying = true;
            this.startId = 1;
        }


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        super.onDestroy();
        this.musicPlaying = false;
    }



}
