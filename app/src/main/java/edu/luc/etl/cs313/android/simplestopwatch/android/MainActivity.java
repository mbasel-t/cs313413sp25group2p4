package edu.luc.etl.cs313.android.simplestopwatch.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import edu.luc.etl.cs313.android.simplestopwatch.R;

public class MainActivity extends Activity {
    private TextView display;
    private int counter = 0;
    private Handler handler = new Handler();
    private Runnable countDownRunnable;
    private boolean isCountingDown = false;
    private long lastButtonPressTime = 0;
    private static final int INACTIVITY_TIME = 3000; // 3 seconds in milliseconds
    private static final int MAX_TIME = 99;

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
                if (System.currentTimeMillis() - lastButtonPressTime >= INACTIVITY_TIME) {
                    isCountingDown = true;
                    if (counter > 0) {
                        counter--;
                        updateDisplay();
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void handleButtonPress() {
        lastButtonPressTime = System.currentTimeMillis();
        boolean reset = false;
        if (isCountingDown) {
            counter = 0;
            handler.removeCallbacks(countDownRunnable);
            isCountingDown = false;
            reset = true;
        }

        if(counter < MAX_TIME && !reset) {
            counter++;
        }
        updateDisplay();

        // Start or restart the countdown check
        handler.removeCallbacks(countDownRunnable);
        handler.postDelayed(countDownRunnable, INACTIVITY_TIME);
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

        if (System.currentTimeMillis() - lastButtonPressTime >= INACTIVITY_TIME) {
            handler.post(countDownRunnable);
        }
    }
}
