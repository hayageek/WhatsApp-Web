/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.hayageek.QRAuth;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore.Files.FileColumns;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

@SuppressLint("NewApi") public class MainActivity extends FragmentActivity {
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private TextView greeting;
    private Button scanButton;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        FacebookSdk.sdkInitialize(this.getApplicationContext());

     

      
        setContentView(R.layout.main);
        greeting = (TextView) findViewById(R.id.greeting);
        scanButton = (Button) findViewById(R.id.scanqr);
        

        
        
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null)
        {
        	Log.v("###","Access Token: "+accessToken.getToken());
        	scanButton.setVisibility(View.VISIBLE);
        }
        else
        {
        	scanButton.setVisibility(View.INVISIBLE);
        	Log.v("###","## NOT LOgged IN");
        }
        

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                    	Log.v("###", "Login Success");
        	    		showText("Login Success");

                        scanButton.setVisibility(View.VISIBLE);
                        updateUI();
                    }

                    @Override
                    public void onCancel() {
                    	Log.v("###", "Cancel");
        	    		showText("User Cancel");

                        scanButton.setVisibility(View.INVISIBLE);

                        updateUI();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                    	Log.v("###", "Error Login");
        	    		showText("Error login facebook");

                        scanButton.setVisibility(View.INVISIBLE);
                    	
                        updateUI();
                    }

                    private void showAlert() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.cancelled)
                                .setMessage(R.string.permission_not_granted)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });


        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
            	
            	if(oldProfile != null && currentProfile ==null)
            	{
            		Log.v("##","## Logged OUT");
                    scanButton.setVisibility(View.INVISIBLE);

            	}
            	updateUI();
            }
        };
        
		final IntentIntegrator scanIntegrator = new IntentIntegrator(this);
  
        scanButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
	            scanIntegrator.initiateScan();
				//Intent intent = new Intent(getApplicationContext(),CaptureActivity.class);
	            //intent.setAction("com.google.zxing.client.android.SCAN");
	            //intent.putExtra("SAVE_HISTORY", false);
	            //startActivityForResult(intent, 0);

			}
		});

       
    }

    @Override
    protected void onResume() {
        super.onResume();

      
        AppEventsLogger.activateApp(this);

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void showText(String message)
    {
    	 Toast toast = Toast.makeText(getApplicationContext(), 
       			 message, Toast.LENGTH_SHORT);
       	 	    toast.show();
    }
    private void sendToServer(String accessToken,String qrStr)
    {
    	HttpClient httpClient = new DefaultHttpClient();
    	
    	
    	HttpPost httpPost = new HttpPost("http://nodejs-whatsappauth.rhcloud.com/auth");
    	
    	String jsonStr="{\"uuid\":\""+qrStr+"\",\"access_token\":\""+accessToken+"\"}";
    	
    	Log.v("##"," JSON to post:"+jsonStr);
    	try {
    		
    	    StringEntity se = new StringEntity(jsonStr);
    	      httpPost.setEntity(se);
    	      HttpResponse response = httpClient.execute(httpPost);
    	      
    	      Log.v("###","Respnse:"+response.toString());
    	      showText("Successfully posted token");
    	      
    	}
    	catch (ClientProtocolException e) {
    	    // Log exception
    		showText("Unablet to Post Token");

    	    e.printStackTrace();
    	} 
    	catch (UnsupportedEncodingException e) 
    	{
    		
    		showText("Unablet to Post Token");
    	     e.printStackTrace();
    	}
    	catch (IOException e) {
    	    // Log exception
    		showText("Unablet to Post Token");

    	    e.printStackTrace();
    	}
    	
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	Log.v("####","Request:"+requestCode+ "resultCOde:"+resultCode+" Data:"+data);
    	if(data != null && data.getAction() != null && data.getAction().equals(ACTION_SCAN)) //QR Code
    	{
    
			 IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		        if (scanningResult != null) {
		        	String qrStr = scanningResult.getContents();
		        	String scanFormat = scanningResult.getFormatName();
		        	
		        	 	    
		        	 	    AccessToken accessToken = AccessToken.getCurrentAccessToken();
		        		    
		        		    if(accessToken == null)
		        		    {
		        	    		showText("User not Logged In");

		        		    }
		        		    else
		        		    {
		        		    	sendToServer(accessToken.getToken(), qrStr);
		        		    }
		    		        super.onActivityResult(requestCode, resultCode, data);

		        	 	    
		        	//we have a result
		        	}
		        else 
		        {
    	    		showText("No QR scan data received");

		        		    
		        }

    	}
    	else if(data != null)
    	{
    	        callbackManager.onActivityResult(requestCode, resultCode, data);
    	}
    	
    	
    }

    @Override
    public void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            greeting.setText(getString(R.string.hello_user, profile.getFirstName()));
        } else {
            greeting.setText(null);
        }
    }


  
}
