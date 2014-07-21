package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 25/06/2014
 * Time: 10:17
 * Version: V2.0
 * SPRINT 4 - SPLASH SCREEN ON STARTUP
 * ************************
 */
public class Splash extends Activity

{
    private final int SPLASH_DISPLAY_LENGTH = 4000;     // int to determine the length of time the splash screen appears

    // called when the activity is first created
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        setContentView(R.layout.splash);                // opens up the splash XML file

        // handler to start the MainScreenActivity and close this Splash-Screen after specified time (1000 sec)

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run()
            {

                Intent mainIntent = new Intent(Splash.this,MainScreenActivity.class);   // creates an Intent
                Splash.this.startActivity(mainIntent);                                  // runs new activity
                Splash.this.finish();                                                   // closes splash activity
            }
        }, SPLASH_DISPLAY_LENGTH);
    }// onCreate

} // Splash
