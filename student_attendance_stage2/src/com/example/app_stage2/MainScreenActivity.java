package com.example.app_stage2;  // found at http://www.androidhive.info/2012/05/how-to-connect-android-with-php-mysql/

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

// import Zxing Files
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;




public class MainScreenActivity extends Activity implements OnClickListener

{
	
	private Button btnTest;
	private Button btnScan;
    private TextView formatTxt, contentTxt;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);
		
		// Buttons
		btnTest = (Button) findViewById(R.id.test_button);
		btnScan = (Button) findViewById(R.id.scan_button);

        // TextViews
        formatTxt = (TextView)findViewById(R.id.scan_format);
        contentTxt = (TextView)findViewById(R.id.scan_content);
        btnScan.setOnClickListener(this);
        btnTest.setOnClickListener(this);


    }// OnCreate


            @Override
            public void onClick (View view)
            {
                // student wishes to sign into class
                if(view.getId()==R.id.scan_button)     //scan
                {
                    IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                    scanIntegrator.initiateScan();
                }// if

                if(view.getId()==R.id.test_button)     //scan
                {
                    // Launching All products Activity
                    Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                    startActivity(i);
                }// if


            }// onClick


    // Step 4 - Return Scanning Results
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null)
        {

            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);

            Toast toast = Toast.makeText(getApplicationContext(),
                    "FORMAT: " + scanFormat + "\nCONTENT: " + scanContent, Toast.LENGTH_LONG);
            toast.show();

            // Launching SignIn Activity
            Intent i = new Intent(getApplicationContext(), SignIn.class);
            String scannedInfo = scanContent;
            i.putExtra("Info", scannedInfo);
            startActivity(i);
        } else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }// if-else

    }// onActivityResult

}// MainScreenActivity