package com.coderwurst.student_attendance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * ***********************
 * Created by IntelliJ IDEA
 * User: andrew
 * Date: 19/07/2014
 * Time: 11:34
 * Version: V1.0
 * ADD CLASS INFORMATION HERE
 * ************************
 */
public class ChooseQR extends Activity

{

    private String pid, lectureUrl, tutorialUrl;


    private static final String TAG_ID = "id";
    // private static final String TAG_NAME = "name";
    private static final String TAG_LECTURE = "lectureUrl";
    private static final String TAG_TUTORIAL = "tutorialUrl";


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_qr);

        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        pid = i.getStringExtra(TAG_ID);
        lectureUrl = i.getStringExtra(TAG_LECTURE);
        tutorialUrl = i.getStringExtra(TAG_TUTORIAL);

        // change the page title to that of the module number selected
        TextView title = (TextView) findViewById(R.id.module_number);
        title.setText(pid);

        Log.d("qr address: ", lectureUrl + "," + tutorialUrl);

        // load the default QR-Code data into the webview
        WebView webview = (WebView)this.findViewById(R.id.webView);
        WebSettings settings = webview.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        webview.loadUrl(lectureUrl);



    } // onCreate

} // ChooseQR
