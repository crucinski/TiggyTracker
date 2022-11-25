package com.princeton2010.tiggy;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MusicFragment extends Fragment {
	
	//You guessed it, the root view of the View hierarchy for this fragment
	private View rootView;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
	    ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.music_fragment, container, false);
		((Button) rootView.findViewById(R.id.update)).setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View arg0) {
				update();
			}
		});
		((Button) rootView.findViewById(R.id.request_button)).setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View arg0) {
				EditText v = (EditText) rootView.findViewById(R.id.editText);
				if (v.getTextColors().getDefaultColor() == Color.parseColor("#888888")) {
					v.setTextColor(Color.parseColor("#000000"));
					v.setText("");
				}
				tweet();
			}
		});
	    ((EditText) rootView.findViewById(R.id.editText)).setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean focus) {
				if (focus && ((EditText) v).getTextColors().getDefaultColor() == Color.parseColor("#888888")) {
					((EditText) v).setTextColor(Color.parseColor("#000000"));
					((EditText) v).setText("");
				}
			}
	    });
		return rootView;
	}
	
	//Bit of a misnomer, this just brings up the picker screen for picking an app. Presumably the user will pick Twitter,
	//but they don't necessarily have to.
	public void tweet() {
		
		String request = ((TextView) rootView.findViewById(R.id.editText)).getText().toString().trim();
		boolean checked = ((CheckBox) rootView.findViewById(R.id.checkbox)).isChecked();
		
		Intent i=new Intent(android.content.Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_TEXT, "@TiggyTigerator " + request + (checked ? " #TiggySongRequest" : ""));
		startActivity(Intent.createChooser(i, "Tweet for Tiggy"));
		
		((TextView) rootView.findViewById(R.id.editText)).setText("");
		
	}
	
	//Update everything on this fragment that depends on information from the server
	//We send two requests serially, beginning with whether anything is playing
	public void update() {
		MainActivity.instance.requestHTTP(URL_Method_Pair.IS_PLAYING);
	}
	
	//callback for getting whether a song is playing
	public void updateIsPlaying(JSONObject json, String msg) {
		
		updateTime();
		
		if (json == null) {
			MainActivity.instance.toast("Failed to get music status: " + msg);
			return;
		}
		
		try {
			boolean is_playing = json.getBoolean("is_playing");
			if (is_playing) {
				//The second of our serial requests
				((TextView) rootView.findViewById(R.id.now_playing)).setText("");
				MainActivity.instance.requestHTTP(URL_Method_Pair.MUSIC);
			} else {
				((TextView) rootView.findViewById(R.id.now_playing)).setText("\nThe Tiggy boombox is silent.");
				setVisible((TextView) rootView.findViewById(R.id.title), false);
				setVisible((TextView) rootView.findViewById(R.id.artist), false);
				setVisible((TextView) rootView.findViewById(R.id.album), false);
				MainActivity.instance.toastShort("Music updated");
				//no need to send the request for the music info if nothing is playing
			}
		} catch (JSONException e) {
			Log.e(MainActivity.TAG, Log.getStackTraceString(e));
			MainActivity.instance.toast("Failed to get music status");
			return;
		}
		
	}
	
	private void updateTime() {
		
		Time t = new Time();
		t.setToNow();
		String format = String.format("%02d:%02d:%02d", t.hour, t.minute, t.second);
		((TextView) rootView.findViewById(R.id.last_update)).setText(format);
		
	}
	
	//Updates the text of the view with the value corresponding to the key in the json object
	private void updateField(TextView view, String key, JSONObject json) {
		
		try {
			String val = json.getString(key);
			val.trim();
			if (!val.equals("")) {
				view.setText(val);
				setVisible(view, true);
			} else {
				setVisible(view, false);
			}
		} catch (JSONException e) {
			view.setText("Unkown");
		}

	}
	
	private void setVisible(TextView field, boolean b) {
		
		TextView label;
		if (field.equals(rootView.findViewById(R.id.title))) {
			label = ((TextView) rootView.findViewById(R.id.title_label));
		} else if (field.equals(rootView.findViewById(R.id.artist))) {
			label = ((TextView) rootView.findViewById(R.id.artist_label));
		} else if (field.equals(rootView.findViewById(R.id.album))) {
			label = ((TextView) rootView.findViewById(R.id.album_label));
		} else {
			throw new IllegalArgumentException("Unexpected field: " + field);
		}
		
		field.setVisibility(b ? View.VISIBLE : View.GONE);
		label.setVisibility(b ? View.VISIBLE : View.GONE);
		
	}
	
	//callback for when our HTTP GET request about music information gets back to us
	public void updateMusic(JSONObject json, String msg) {
		
		updateTime();
		
		if (json == null) {
			MainActivity.instance.toast("Failed to get music information: " + msg);
			return;
		}
		
		updateField((TextView) rootView.findViewById(R.id.title), "title", json);
		updateField((TextView) rootView.findViewById(R.id.artist), "artist", json);
		updateField((TextView) rootView.findViewById(R.id.album), "album", json);
		
		MainActivity.instance.toastShort("Music updated");
		
	}
	
}