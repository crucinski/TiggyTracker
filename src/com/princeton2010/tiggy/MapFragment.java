package com.princeton2010.tiggy;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment {
	
	//reference to the map fragment itself
	private GoogleMap map;
	
	//the root view (in the View hierarchy) of the fragment
	private View rootView;
	
	//reference to the marker pin thing that is displayed at Tiggy's location
	private Marker tiggyMarker;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
	    ViewGroup container, Bundle savedInstanceState) {
		
		//This conditional and try/catch fixes a crash when switching between tabs, which apparently
		//has to do with the map fragment reloading
		if (rootView != null) {
	        ViewGroup parent = (ViewGroup) rootView.getParent();
	        if (parent != null)
	            parent.removeView(rootView);
	    }
	    try {
	        rootView = inflater.inflate(R.layout.map_fragment, container, false);
	    } catch (InflateException e) {
	        /* map is already there, just return view as it is */
	    }
	    
	    ((Button) rootView.findViewById(R.id.update)).setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View arg0) {
				update();
			}
		});
	    ((Button) rootView.findViewById(R.id.campus)).setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View arg0) {
				zoomCampus();
			}
		});
	    
		map = ((SupportMapFragment) MainActivity.instance.getSupportFragmentManager().findFragmentById(R.id.mymap)).getMap();
		map.setMyLocationEnabled(true);
		
		zoomCampus();
				
		return rootView;
	}
	
	public void toastIfNoInternet() {

	    if (!MainActivity.instance.isInternetConnected()) {
	    	MainActivity.instance.toast("Not connected to the Internet!");
	    }
		
	}
	
	private void updateTime() {
		
		Time t = new Time();
		t.setToNow();
		String format = String.format("Updated: %02d:%02d:%02d", t.hour, t.minute, t.second);
		((TextView) rootView.findViewById(R.id.time)).setText(format);
		
	}
	
	private void zoomCampus() {
		
		updateMap(new LatLng(40.344, -74.657), 15);
		
	}
	
	private void updateMap(LatLng latlng, int zoom_factor) {

		map.moveCamera(CameraUpdateFactory.zoomTo(zoom_factor));
		map.animateCamera(CameraUpdateFactory.newLatLng(latlng));

	}
	
	//Update everything on this page that depends on updates from the server.
	//We do two requests serially, starting with the tap.
	public void update() {
		
		MainActivity.instance.requestHTTP(URL_Method_Pair.TAP);
		
	}
	
	//Callback for when the server replies with tap status
	public void updateTap(JSONObject json, String msg) {
		
		if (json == null) {
			MainActivity.instance.toast("Failed to get tap status and Tiggy's location: " + msg);
			return;
		}
		
		try {
			boolean on_tap = json.getBoolean("on_tap");
			TextView view = (TextView) rootView.findViewById(R.id.tap_status);
			view.setText(on_tap ? "On tap!" : "Not on tap");
		} catch (JSONException e) {
			Log.e(MainActivity.TAG, Log.getStackTraceString(e));
			((TextView) rootView.findViewById(R.id.tap_status)).setText("");
			MainActivity.instance.toast("Failed to get tap status");
		}
		
		//send the second of our requests
		MainActivity.instance.requestHTTP(URL_Method_Pair.LOCATION);
		
	}
	
	//callback for when the HTTP GET request about location gets back to us
	public void updateLocation(JSONObject json, String msg) {
		
		if (json == null) {
			MainActivity.instance.toast("Failed to get Tiggy's location: " + msg);
			return;
		}
		
		try {
			double lat = json.getDouble("lat");
			double lng = json.getDouble("lon");
			updateMap(new LatLng(lat, lng), 15);
			if (tiggyMarker != null) {
				tiggyMarker.remove();
			}
			tiggyMarker = map.addMarker(new MarkerOptions()
	            .position(new LatLng(lat, lng))
	            .title("Tiggy")
	            .snippet("Come for beats and beast!")
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker)));
			MainActivity.instance.toastShort("Tap status and location updated");
		} catch (JSONException e) {
			Log.e(MainActivity.TAG, Log.getStackTraceString(e));
			MainActivity.instance.toast("Failed to get Tiggy's location");
		}	
		
		updateTime();
		
	}
	
}