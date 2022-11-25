package com.princeton2010.tiggy;

//This class is for packaging up arguments for the HTTP GET requests
public class URL_Method_Pair {
	
	public String url; //URL to point the GET at
	public method_type method; //identifier for the callback method
	public static enum method_type {METHOD_IS_PLAYING, METHOD_MUSIC, METHOD_LOCATION, METHOD_TAP};
	
	//Turns out we really only ever need these four instances, so I just went ahead and put them here
	public static final URL_Method_Pair IS_PLAYING = new URL_Method_Pair(method_type.METHOD_IS_PLAYING);
	public static final URL_Method_Pair MUSIC = new URL_Method_Pair(method_type.METHOD_MUSIC);
	public static final URL_Method_Pair LOCATION = new URL_Method_Pair(method_type.METHOD_LOCATION);
	public static final URL_Method_Pair TAP = new URL_Method_Pair(method_type.METHOD_TAP);
	
	public URL_Method_Pair(method_type m) {
		
		this.method = m;
		this.url = getUrl(m);
		
	}
	
	public URL_Method_Pair(String url, method_type m) {
		
		this.url = url;
		this.method = m;
		
	}
	
	//Return the full URL corresponding to the type of information we want
	public static String getUrl(method_type method) {
		
		String stem = MainActivity.URL_STEM;
		if (method == method_type.METHOD_IS_PLAYING) {
			return stem + "getsongstatus";
		} else if (method == method_type.METHOD_MUSIC) {
			return stem + "getsong";
		} else if (method == method_type.METHOD_LOCATION) {
			return stem + "getlocation";
		} else if (method == method_type.METHOD_TAP) {
			return stem + "gettapstatus";
		} else {
			throw new IllegalArgumentException("Invalid method identifier: " + method);
		}
	}
	
}
