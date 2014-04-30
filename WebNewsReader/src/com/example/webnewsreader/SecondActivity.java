package com.example.webnewsreader;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

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
		String myURL = settings.getString(MainActivity.PREF_URL, "");
		WebView newsPage = (WebView) findViewById(R.id.webView1);
		WebSettings webSettings = newsPage.getSettings();
		webSettings.setBuiltInZoomControls(true);
        newsPage.setWebViewClient(new Callback());
		newsPage.loadUrl(myURL);
	}
	
	public void clickBack(View V) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

    private class Callback extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }

    }
}
