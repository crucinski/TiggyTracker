package com.princeton2010.tiggy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.Toast;

import com.princeton2010.tiggy.CollectionPagerAdapter.frag_type;

public class MainActivity extends FragmentActivity {

	public static final String TAG = "Tiggy";
	
	//URL to send all GET requests to
	public static final String URL_STEM = "http://princetontigerator.appspot.com/";
	
	//A reference to the main activity for when other classes need a Context
	public static MainActivity instance = null;
	
	//These two handle the sort of swipe views we've got going on
	//NB: The ActionBar implementation of this isn't compatible with earlier versions of Android, so that's why I opted for the
	//somewhat clunky swiping that interferes with manipulating the map.
	CollectionPagerAdapter mPagerAdapter;
    ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		MainActivity.instance = this;
		
		initPages();
		
		if (isInternetConnected()) {
			toast("The map may take a few seconds to load.", Gravity.CENTER);
		}
		
	}
	
	public boolean isInternetConnected() {
		
		ConnectivityManager connectivityManager 
        = (ConnectivityManager) MainActivity.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		
	}
	
	public void toast(String msg, int gravity) {
		
		Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
		if (gravity != -1) {
			toast.setGravity(Gravity.CENTER, 0, 0);
		}
		toast.show();
		
	}
	
	public void toast(String msg) {
		
		toast(msg, -1);
		
	}
	
	//Includes a slight hack to make the toast shorter than usual
	public void toastShort(String msg) {
		
		final Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
		
		Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
	           @Override
	           public void run() {
	               toast.cancel(); 
	           }
	    }, 500);
		
	}
	
    // Initialize the plumbing needed to display the views, etc.
    private void initPages() {
    	
        mPagerAdapter =
                new CollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setPager(mViewPager);
    	
    }
    
    //For packaging up the responses to the HTTP GET requests
    private class JSON_Method_Pair {
    	
    	public JSONObject json; // the response
    	public URL_Method_Pair.method_type method; // identifier for the callback method
    	public String msg; // error message, will only be populated if json is null
    	
    	public JSON_Method_Pair (JSONObject json, URL_Method_Pair.method_type method) {
    		
    		this.json = json;
    		this.method = method;
    		this.msg = "";
    		
    	}
    	
    	public JSON_Method_Pair (JSONObject json, URL_Method_Pair.method_type method, String msg) {
    		
    		this.json = json;
    		this.method = method;
    		this.msg = msg;
    		
    	}
    	
    }
    
    //Overridden in this activity instead of each fragment because the each fragment actually gets focus when you load the
    //fragment next to it (when the title is displayed), and reloading stuff before the user gets to the fragment is confusing
    //for the user.
    @Override
    public void onResume() {
    	
    	super.onResume();
    	if (mViewPager.getCurrentItem() == frag_type.FRAG_MUSIC.ordinal()) {
			mPagerAdapter.musicFrag.update();
		} else if (mViewPager.getCurrentItem() == frag_type.FRAG_MAP.ordinal()) {
			mPagerAdapter.mapFrag.toastIfNoInternet();
		}
    	
    }
    
    //public interface to making HTTP GET requests
    public void requestHTTP(URL_Method_Pair arg) {
    	
    	new RequestTask().execute(arg);
    	
    }
    
	//make an HTTP GET request, necessarily asynchronous because we don't want to block the UI thread
	private class RequestTask extends AsyncTask<URL_Method_Pair, String, JSON_Method_Pair>{

	    @Override
	    protected JSON_Method_Pair doInBackground(URL_Method_Pair... args) {
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(args[0].url));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	        	Log.e(TAG, Log.getStackTraceString(e));
	        	return new JSON_Method_Pair(null, args[0].method, "Failed to send request to server");
	        } catch (IOException e) {
	        	Log.e(TAG, Log.getStackTraceString(e));
	        	return new JSON_Method_Pair(null, args[0].method, "Failed to connect to the Internet");
	        }
	        try {
				return new JSON_Method_Pair(new JSONObject(responseString), args[0].method);
			} catch (JSONException e) {
				Log.e(TAG, Log.getStackTraceString(e));
				return new JSON_Method_Pair(null, args[0].method, "Failed to parse server response");
			}
	    }

	    //callback that will execute on the UI thread
	    @Override
	    protected void onPostExecute(JSON_Method_Pair result) {
	        super.onPostExecute(result);
	        if (result.method == URL_Method_Pair.method_type.METHOD_IS_PLAYING) {
	        	((MusicFragment)mPagerAdapter.getItem(CollectionPagerAdapter.frag_type.FRAG_MUSIC)).updateIsPlaying(result.json, result.msg);
	        } else if (result.method == URL_Method_Pair.method_type.METHOD_MUSIC) {
	        	((MusicFragment)mPagerAdapter.getItem(CollectionPagerAdapter.frag_type.FRAG_MUSIC)).updateMusic(result.json, result.msg);
	        } else if (result.method == URL_Method_Pair.method_type.METHOD_LOCATION) {
	        	((MapFragment)mPagerAdapter.getItem(CollectionPagerAdapter.frag_type.FRAG_MAP)).updateLocation(result.json, result.msg);
	        } else if (result.method == URL_Method_Pair.method_type.METHOD_TAP) {
	        	((MapFragment)mPagerAdapter.getItem(CollectionPagerAdapter.frag_type.FRAG_MAP)).updateTap(result.json, result.msg);
	        } else {
				throw new IllegalArgumentException("Invalid method identifier: " + result.method);
			}
	    }
	}
	

    //plumbing for a menu w/ settings, which we don't have yet, so I've commented it out
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	*/

}
