package com.benbarker.posetime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 * Pose Timer main activity
 */

public class MainActivity extends AppCompatActivity {

    private CountDownTimerWithPause countDownTimer;
    private long initialSeconds = 30;
    private long posesLeft = 1;
    private long maxPoses = 1;
    private TextView mText;
    private TextView mPosesLeft;
    private boolean timerRunning = false;
    private boolean freshTimer = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        configureViews();
        refreshTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * react to the user tapping/selecting an options menu item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_thingy:
                Intent i = new Intent(this, MainPreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get widgets and set initial values
     */
    private void configureViews(){
        mText = (Button) this.findViewById(R.id.timerButton);
        mPosesLeft = (TextView) this.findViewById(R.id.posesLeftText);
        mPosesLeft.setText(String.valueOf(posesLeft));

        //Button begins red
        mText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);


        //Time values set with tag in seconds
        Button b30s = (Button) findViewById(R.id.button30s);
        assert b30s != null;
        b30s.setTag(30);
        Button b1m = (Button) findViewById(R.id.button1m);
        assert b1m != null;
        b1m.setTag(60);
        Button b2m = (Button) findViewById(R.id.button2m);
        assert b2m != null;
        b2m.setTag(120);
        Button b3m = (Button) findViewById(R.id.button3m);
        assert b3m != null;
        b3m.setTag(180);
        Button b5m = (Button) findViewById(R.id.button5m);
        assert b5m != null;
        b5m.setTag(300);
        Button b7m = (Button) findViewById(R.id.button7m);
        assert b7m != null;
        b7m.setTag(420);
        Button b10m = (Button) findViewById(R.id.button10m);
        assert b10m != null;
        b10m.setTag(600);
        Button b15m = (Button) findViewById(R.id.button15m);
        assert b15m != null;
        b15m.setTag(900);
        Button b20m = (Button) findViewById(R.id.button20m);
        assert b20m != null;
        b20m.setTag(1200);
        Button b25m = (Button) findViewById(R.id.button25m);
        assert b25m != null;
        b25m.setTag(1500);
        Button b30m = (Button) findViewById(R.id.button30m);
        assert b30m != null;
        b30m.setTag(1800);
        //set the tag on the custom button based on preferences
        setCustomButtonValue();

        //Button bCustom = (Button) findViewById(R.id.buttonCustom);
        //assert bCustom != null;
        //bCustom.setTag(10); //debugging value so I can shortcut timer
        //bCustom.setTag(2700);
    }

    public void setCustomButtonValue(){
        Button bCustom = (Button) findViewById(R.id.buttonCustom);
        assert bCustom != null;

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String value = SP.getString("customTimerPref", "10");
        int bCustomLength = Integer.valueOf(value);
        bCustom.setTag(bCustomLength*60);
    }

    /**
     * main button clicked, the main timer/start/stop button
     */
    public void mainButtonClick(View v) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean bScreenOn = SP.getBoolean("screenOnPref",false);

        if ((!timerRunning)&&(posesLeft > 0)) {
            Log.i("test","starting timer");
            if (freshTimer){
                this.countDownTimer.create();
                freshTimer = false;
            }
            else {
                this.countDownTimer.resume();
            }
            timerRunning = true;
            mText.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
            toggleTimerButtons(false);
            if (bScreenOn) {
                getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }


        } else {
            Log.i("test","pausing timer");
            this.countDownTimer.pause();
            timerRunning = false;
            mText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            toggleTimerButtons(true);
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }
    }

    /**
     * time preset buttons clicked, set seconds to that tag, refresh
     * and stop timer
     */
    public void timeButtonClick(View v) {
        setCustomButtonValue(); //make sure this is refreshed
        int seconds = (Integer)v.getTag();
        Log.i("test","time button = " + seconds);
        initialSeconds = seconds;
        this.countDownTimer.cancel();
        refreshTimer();
        this.countDownTimer.pause();
        timerRunning = false;
        mText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * increment poses left button
     */
    public void posesLeftIncrementClick(View v){
        if (posesLeft < 99) {
            posesLeft++;
            maxPoses = posesLeft;
            mPosesLeft.setText(posesToString(posesLeft,maxPoses));
        }
    }

    /**
     * decrement poses left button
     */
    public void posesLeftDecrementClick(View v){
        if (posesLeft > 1) {
            posesLeft--;
            maxPoses = posesLeft;
            mPosesLeft.setText(posesToString(posesLeft,maxPoses));
        }
    }

    /**
     * Creates a fresh timer
     */
    private void refreshTimer()
    {
        Log.i("test", "refresh counter");
        this.countDownTimer = new CountDownTimerWithPause(MainActivity.this.initialSeconds * 1000, 100, true) {
            public void onTick(long ms) {
                long seconds = Math.round((float) ms / 1000.0f);
                Log.i("test","seconds value = " + seconds);
                mText.setText(secondsToString(seconds));
            }
            public void onFinish() {
                Log.i("test", "finished countdown");
                if (posesLeft > 0) {
                    posesLeft--;
                }
                endTimerAlert();
                refreshTimer();
            }
        };

        mText.setText(secondsToString(this.initialSeconds));
        mPosesLeft.setText(posesToString(posesLeft,maxPoses));
        timerRunning = false;
        freshTimer = true;
        toggleTimerButtons(true);

        //If more poses are left, immediately start the timer
        if (posesLeft > 0 && posesLeft != maxPoses) {
            timerRunning=true;
            freshTimer=false;
            this.countDownTimer.create();
            toggleTimerButtons(false);
        }
    }

    /**
     * Given a long int of seconds return "MM:SS" string
     */
    private static String secondsToString(long pTime) {
        return String.format(Locale.US,"%02d:%02d", pTime / 60, pTime % 60);
    }

    /**
     * Given a posesLeft and maxPoses return formatted string
     */
    private static String posesToString(long posesLeft, long maxPoses){
        if (posesLeft > 1){
            return String.format(Locale.US,"%01d poses left",posesLeft);
        }
        else if (posesLeft == 1){
            return String.format(Locale.US,"%01d pose left",posesLeft);
        }
        else{
            return String.format(Locale.US,"%01d poses done",maxPoses);
        }
    }

    /**
     * Enable/disable all the time preset buttons
     */
    private void toggleTimerButtons(boolean status){
        Button b30s = (Button) findViewById(R.id.button30s);
        Button b1m = (Button) findViewById(R.id.button1m);
        Button b2m = (Button) findViewById(R.id.button2m);
        Button b3m = (Button) findViewById(R.id.button3m);
        Button b5m = (Button) findViewById(R.id.button5m);
        Button b7m = (Button) findViewById(R.id.button7m);
        Button b10m = (Button) findViewById(R.id.button10m);
        Button b15m = (Button) findViewById(R.id.button15m);
        Button b20m = (Button) findViewById(R.id.button20m);
        Button b25m = (Button) findViewById(R.id.button25m);
        Button b30m = (Button) findViewById(R.id.button30m);
        Button bCustom = (Button) findViewById(R.id.buttonCustom);
        assert b30s != null;
        b30s.setEnabled(status);
        assert b1m != null;
        b1m.setEnabled(status);
        assert b2m != null;
        b2m.setEnabled(status);
        assert b3m != null;
        b3m.setEnabled(status);
        assert b5m != null;
        b5m.setEnabled(status);
        assert b7m != null;
        b7m.setEnabled(status);
        assert b10m != null;
        b10m.setEnabled(status);
        assert b15m != null;
        b15m.setEnabled(status);
        assert b20m != null;
        b20m.setEnabled(status);
        assert b25m != null;
        b25m.setEnabled(status);
        assert b30m != null;
        b30m.setEnabled(status);
        assert bCustom != null;
        bCustom.setEnabled(status);
    }

    /**
     * Alert the user time has ended
     */
    private void endTimerAlert(){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean bVibrate = SP.getBoolean("vibratePref",false);

        //Vibration
        if (bVibrate) {
            Vibrator vibrator;
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }

        //Sound
        //Create an AudioManager with a special listener that only plays the sound once
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        Uri notificationSoundUri = Uri.parse(SP.getString("alarmsoundPref", "DEFAULT_SOUND"));

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), notificationSoundUri);
        } catch (Exception e1) {
            e1.printStackTrace();
            mediaPlayer.release();
            return;
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        try {
            mediaPlayer.prepare();
        } catch (Exception e1) {
            e1.printStackTrace();
            mediaPlayer.release();
            return;
        }
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.start();
            }
        });
        mediaPlayer.setVolume(volume, volume);
        mediaPlayer.start();
    }
}