package com.senic.nuimo.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.senic.nuimo.NuimoController;
import com.senic.nuimo.NuimoControllerListener;
import com.senic.nuimo.NuimoDiscoveryListener;
import com.senic.nuimo.NuimoDiscoveryManager;
import com.senic.nuimo.NuimoGesture;
import com.senic.nuimo.NuimoGestureEvent;
import com.senic.nuimo.NuimoLedMatrix;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Revoking/granting permissions:
 *
 * ~/Library/Android/sdk/platform-tools/adb shell pm grant com.senic.nuimo.demo android.permission.ACCESS_COARSE_LOCATION
 * ~/Library/Android/sdk/platform-tools/adb shell pm revoke com.senic.nuimo.demo android.permission.ACCESS_COARSE_LOCATION
 *
 */
public class MainActivity extends AppCompatActivity implements NuimoDiscoveryListener, NuimoControllerListener {

    NuimoDiscoveryManager discovery = new NuimoDiscoveryManager(this);
    NuimoController controller;

    @Bind(R.id.log)
    ListView logListView;
    LogArrayAdapter logAdapter;

    @Bind(R.id.led_animation)
    FloatingActionButton toggleAnimationButton;
    boolean animatingLed = false;
    int animationIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        logAdapter = new LogArrayAdapter(this);
        logListView.setAdapter(logAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.discover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverAndConnect();
            }
        });
        findViewById(R.id.led_all_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayAllLedsOn();
            }
        });
        toggleAnimationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animatingLed = !animatingLed;
                toggleAnimationButton.setImageResource(animatingLed ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                if (animatingLed) {
                    animationIndex = 0;
                    displayMatrix(LED_ANIMATION_FRAMES[0]);
                }
            }
        });

        discovery.addDiscoveryListener(this);
    }

    @Override
    protected void onDestroy() {
        discovery.stopDiscovery();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        discovery.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("SimpleDateFormat")
    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    public void log(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logAdapter.add(new String[] { sdf.format(new Date()), text });
            }
        });
    }

    void discoverAndConnect() {
        if (controller != null) {
            controller.disconnect();
        }
        log("Start discovery");
        discovery.startDiscovery();
    }

    void displayAllLedsOn() {
        displayMatrix(
                "*********" +
                "*********" +
                "*********" +
                "*********" +
                "*********" +
                "*********" +
                "*********" +
                "*********" +
                "*********");
    }

    void displayMatrix(String string) {
        if (controller == null) return;
        controller.displayLedMatrix(new NuimoLedMatrix(string), 2.0);
        log("Send matrix");
    }

    /**
     * NuimoDiscoveryListener implementation
     */

    @Override
    public void onDiscoverNuimoController(@NotNull NuimoController nuimoController) {
        //if (!nuimoController.getAddress().equals(""))
        log("Discovered " + nuimoController.getAddress() + ". Trying to connect...");
        log("Stop discovery");
        discovery.stopDiscovery();
        controller = nuimoController;
        controller.addControllerListener(this);
        controller.connect();
    }

    /**
     * NuimoControllerListener implementation
     */

    @Override
    public void onConnect() {
        log("Connected to " + (controller != null ? controller.getAddress() : "null"));
        displayAllLedsOn();
    }

    @Override
    public void onFailToConnect() {
        log("Failed to connect to " + (controller != null ? controller.getAddress() : "null"));
    }

    @Override
    public void onDisconnect() {
        log("Disconnected");
    }

    @Override
    public void onLedMatrixWrite() {
        log("Matrix written");
        if (animatingLed) {
            displayMatrix(LED_ANIMATION_FRAMES[(++animationIndex) % LED_ANIMATION_FRAMES.length]);
        }
    }

    @Override
    public void onGestureEvent(@NotNull NuimoGestureEvent event) {
        System.out.println(event.getGesture().toString() + ": " + event.getValue());
        String logText;
        switch (event.getGesture()) {
            case BUTTON_PRESS:   logText = "Button pressed"; break;
            case BUTTON_RELEASE: logText = "Button released"; break;
            case SWIPE_LEFT:     logText = "Swiped left"; break;
            case SWIPE_RIGHT:    logText = "Swiped right"; break;
            case SWIPE_UP:       logText = "Swiped up"; break;
            case SWIPE_DOWN:     logText = "Swiped down"; break;
            case ROTATE:         logText = getLogTextForRotationEvent(event); break;
            case FLY_LEFT:       logText = "Fly left, speed = " + event.getValue(); break;
            case FLY_RIGHT:      logText = "Fly right, speed = " + event.getValue(); break;
            case FLY_BACKWARDS:  logText = "Fly backwards, speed = " + event.getValue(); break;
            case FLY_TOWARDS:    logText = "Fly towards, speed = " + event.getValue(); break;
            case FLY_UP_DOWN:    logText = "Fly, distance = " + event.getValue(); break;
            default:             logText = event.getGesture().name();
        }
        log(logText);
    }

    @Override
    public void onBatteryPercentageChange(int i) {
        log("Battery percentage updated: " + i + "%");
    }

    Date lastRotationDate;
    int lastRotationDirection = 0;
    int accumulatedRotationValue = 0;
    private String getLogTextForRotationEvent(NuimoGestureEvent event) {
        Date now = new Date();
        double speed = 0;
        int rotationValue = (event.getValue() == null ? 0 : event.getValue());
        int rotationDirection = rotationValue > 0 ? 1 : -1;
        if (rotationDirection == lastRotationDirection && (now.getTime() - lastRotationDate.getTime() < 2000)) {
            speed = rotationValue / (double)(now.getTime() - lastRotationDate.getTime());
            accumulatedRotationValue += rotationValue;
        }
        else {
            accumulatedRotationValue = rotationValue;
        }
        lastRotationDirection = rotationDirection;
        lastRotationDate = now;
        return String.format("Rotated %d\n  Speed: %.3f, Accumulated: %d", rotationValue, speed, accumulatedRotationValue);
    }

    private static String[] LED_ANIMATION_FRAMES = new String[] {
            "    **   " +
            "   ***   " +
            "  ****   " +
            "    **   " +
            "    **   " +
            "    **   " +
            "    **   " +
            "    **   " +
            "   ****  ",

            "   ***   " +
            "  *****  " +
            " **   ** " +
            "      ** " +
            "     **  " +
            "    **   " +
            "   **    " +
            "  ****** " +
            " ******* ",

            " ******* " +
            " ******* " +
            "     **  " +
            "    **   " +
            "     **  " +
            "      ** " +
            " **   ** " +
            " ******* " +
            "  *****  ",
    };
}
