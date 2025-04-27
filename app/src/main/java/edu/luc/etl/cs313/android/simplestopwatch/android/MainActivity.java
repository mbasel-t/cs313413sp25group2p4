package edu.luc.etl.cs313.android.simplestopwatch.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.luc.etl.cs313.android.simplestopwatch.R;
import android.media.ToneGenerator;
import android.media.AudioManager;

public class MainActivity extends Activity {
    private TextView display;
    private int counter = 0;
    private Handler handler = new Handler();
    private Runnable countDownRunnable;
    private Runnable beepIndefinitelyRunnable;
    private boolean isCountingDown = false;
    private boolean hasBeeped = false;
    private boolean hasUserInteraction = false;
    private boolean isBeepingIndefinitely = false;
    private long lastButtonPressTime = 0;
    private static final int INACTIVITY_TIME = 3000; // 3 seconds in milliseconds
    private static final int MAX_TIME = 99;
    private boolean alarmJustStarted = false;
    private boolean bypassInactivityTime = false;

    private ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);
        Button controlButton = findViewById(R.id.controlButton);

        controlButton.setOnClickListener(v -> handleButtonPress());


        // Initialize the countdown runnable
        countDownRunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println(isCountingDown);
                isCountingDown = false;
                if (bypassInactivityTime || System.currentTimeMillis() - lastButtonPressTime >= INACTIVITY_TIME) {
                    //Beep after 3sec of inactivity, as timer starts to count down, or when timer hits max time
                    if(!isCountingDown && !hasBeeped) {
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                        hasBeeped = true;
                    }
                    isCountingDown = true;

                    if (isCountingDown && counter > 0) {
                        if (alarmJustStarted) {
                            alarmJustStarted = false;
                        } else {
                            counter--;
                        }

                        updateDisplay();
                        handler.postDelayed(this, 1000);
                    }
                    else{
                        // Beeps start sounding indefinitely when countdown reaches 0
                        if(!isBeepingIndefinitely) {
                            isBeepingIndefinitely = true;
                            handler.post(beepIndefinitelyRunnable); // <- loops infinitely until button press
                        }
                    }
                }
            }
        };

        //Runnable for the infinite beeping at end of countdown
        beepIndefinitelyRunnable = new Runnable() {
            public void run() {
                if (isBeepingIndefinitely) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    handler.postDelayed(this, 400);
                }
            }
        };
    }

    private void handleButtonPress() {
        lastButtonPressTime = System.currentTimeMillis();
        hasBeeped = false;
        hasUserInteraction = true;
        boolean reset = false;

        //stop the beeping at end of countdown
        if (isBeepingIndefinitely) {
            isBeepingIndefinitely = false;
            handler.removeCallbacks(beepIndefinitelyRunnable);
        }

        if (isCountingDown) {
            counter = 0;
            handler.removeCallbacks(countDownRunnable);
            isCountingDown = false;
            reset = true;
        }

        if (counter < MAX_TIME && !reset) {
            counter++;
        }
        updateDisplay();

        // Start or restart the countdown check
        if (counter == MAX_TIME) {
            alarmJustStarted = true;
            bypassInactivityTime = true;
            handler.removeCallbacks(countDownRunnable);
            handler.post(countDownRunnable);
        } else if (counter > 0) {
            alarmJustStarted = true;
            handler.removeCallbacks(countDownRunnable);
            handler.postDelayed(countDownRunnable, INACTIVITY_TIME);
        } else {
            handler.removeCallbacks(countDownRunnable);
        }
    }


    private void updateDisplay() {
        display.setText(String.format("%02d", counter));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasUserInteraction && System.currentTimeMillis() - lastButtonPressTime >= INACTIVITY_TIME) {
            handler.post(countDownRunnable);
        }
    }


}
