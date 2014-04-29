package com.example.webnewsreader;
import android.content.Context;


public class AppInfo {

	private static AppInfo instance = null;

	protected AppInfo() {
		// Exists only to defeat instantiation.
	}	
	
	// Here are some values we want to keep global.
	public String sharedString;
	public String givenUrl;

	public static AppInfo getInstance(Context context) {
		if(instance == null) {
			instance = new AppInfo();
			instance.sharedString = "";
		}
		return instance;
	}	
	
}
