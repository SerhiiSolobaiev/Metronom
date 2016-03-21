package com.myandroid.metronom;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MetronomService extends Service {

    private static final String LOG_TAG = MetronomService.class.getSimpleName();

    public static final String METRONOM_SERVICE = "com.myandroid.metronom.service.METRONOM_SERVICE";
    private static Camera cam;
    private static Vibrator v;
    private static MediaPlayer mediaPlayer;
    private Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG,"MetronomService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "in onStartCommand");

        final int frequency = intent.getIntExtra("frequency", 100);
        final boolean[] parameters = intent.getBooleanArrayExtra("parameters");
        Log.v(LOG_TAG, "frequency = " + frequency);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Vibrator has it's own pattern
        //vibrate for pattern[1] milliseconds
        //sleep for pattern[2] milliseconds
        final long[] pattern = {0, frequency*10, frequency*10};//for better displaying
        if (parameters[0]) {
            v.vibrate(pattern,0);
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.sounds_cooked);
        mediaPlayer.setLooping(true);//if sound ends

        timer = new Timer();
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (parameters[1]) {
                            if (getPackageManager().hasSystemFeature(
                                    PackageManager.FEATURE_CAMERA_FLASH)) {
                                Camera.Parameters p = cam.getParameters();
                                if (p.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                    cam.setParameters(p);
                                    cam.startPreview();
                                } else {
                                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                    cam.setParameters(p);
                                    cam.startPreview();
                                }
                            }
                            else{
                                Log.v(LOG_TAG,"device hasn't FEATURE_CAMERA_FLASH");
                            }
                        }
                        if (parameters[2]) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                                mediaPlayer.seekTo(0);
                            } else {
                                mediaPlayer.start();
                            }
                        }
                        updateIndicator();
                    }
                }, 0, frequency*10); //for better displaying
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //massage to activity to update indicator on UI thread
    private void updateIndicator(){
        Intent intent = new Intent("updateIndicatorOnUI");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (cam != null) {
            cam.stopPreview();
            cam.release();
        }
        if (v != null) {
            v.cancel();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Log.v(LOG_TAG, "service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
