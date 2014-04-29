package com.example.webnewsreader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class SecondActivity extends Activity {

	AppInfo appInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		appInfo = AppInfo.getInstance(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Writes the string from main activity.
		SharedPreferences settings = getSharedPreferences(MainActivity.MYPREFS, 0);
		String myTitle = settings.getString(MainActivity.PREF_TITLE, "");
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setText(myTitle);
		
		// and the one from the singleton object
		//TextView tv2 = (TextView) findViewById(R.id.textView3);
		//tv2.setText(appInfo.sharedString);		
		
	}
	
	public void clickBack(View V) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		// finish();
	}


}
