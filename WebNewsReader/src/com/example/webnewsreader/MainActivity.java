package com.example.webnewsreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	static final public String MYPREFS = "myprefs";
	static final public String PREF_TITLE = "string_1";
	static final public String PREF_URL = "url";
	private static final String LOG_TAG = "MainActivity";
	
	private static final int MAX_SETUP_DOWNLOAD_TRIES = 3;
	private static final String DOWNLOAD_URL = "http://luca-ucsc.appspot.com/jsonnews/default/news_sources.json";
	
	public String[] titles = new String[20];
	public String[] urls = new String[20];
	
	// Background downloader.
	private BackgroundDownloader downloader = null;
	
	AppInfo appInfo;
	
	private class ListElement {
		ListElement() {};
		
		public String textLabel;
		public String buttonLabel;
	}
	
	private ArrayList<ListElement> aList;
	
	private class MyAdapter extends ArrayAdapter<ListElement>{

		int resource;
		Context context;
		
		public MyAdapter(Context _context, int _resource, List<ListElement> items) {
			super(_context, _resource, items);
			resource = _resource;
			context = _context;
			this.context = _context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout newView;
			
			ListElement w = getItem(position);
			
			// Inflate a new view if necessary.
			if (convertView == null) {
				newView = new LinearLayout(getContext());
				String inflater = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
				vi.inflate(resource,  newView, true);
			} else {
				newView = (LinearLayout) convertView;
			}
			
			// Fills in the view.
			TextView tv = (TextView) newView.findViewById(R.id.listText);
			Button b = (Button) newView.findViewById(R.id.listButton);
			tv.setText(w.textLabel);
			b.setText(w.buttonLabel);

			// Sets a listener for the button, and a tag for the button as well.
			b.setTag(Integer.toString(position));
			b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// Reacts to a button press.
					String s = (String) v.getTag();
					int pos = Integer.parseInt(s);
					goOther(v, pos);
					//aa.notifyDataSetChanged();
				}
			});

			return newView;
		}		
	}

	private MyAdapter aa;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		aList = new ArrayList<ListElement>();
		aa = new MyAdapter(this, R.layout.list_element, aList);
		ListView myListView = (ListView) findViewById(R.id.listView1);
		myListView.setAdapter(aa);
		aa.notifyDataSetChanged();
		startDownload();
		appInfo = AppInfo.getInstance(this);
	}
	
	//may not need this since we're not storing the data when it returns..
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = getSharedPreferences(MainActivity.MYPREFS, 0);
		String myTitle = settings.getString(MainActivity.PREF_TITLE, "");
	}
	
	public void makeList (String str)
	{
		int left, right;
		for (int i = 0; i < 20; i++)
		{
			left = str.indexOf("{", 5);
			right = str.indexOf("}") + 1;
			if(left == -1 || right == -1)
			{
				break;
			}
			String sub = str.substring(left, right);
			String temp = str;
			temp = str.replace(sub, "");
			str = temp;
			if((sub.indexOf("title") == -1) && (sub.indexOf("http") == -1))
			{
				i--;
			}
			else{
				if(sub.indexOf("http") != -1)
				{
					left = sub.indexOf("http");
					right = sub.indexOf("\"", sub.indexOf("http") +1 );
					urls[i] = sub.substring(left, right);
				}
				if(sub.indexOf("title") != -1)
				{
					left = sub.indexOf("title");
					right = sub.indexOf("\"", sub.indexOf("title") +10 );
					titles[i] = sub.substring(left + 9, right);
					makeNewElement(titles[i], i);
				}
			}
			
		}
	}
	
	public void makeNewElement(String Title, int index)
	{
		Log.d(LOG_TAG, "New element being created");
		ListElement el = new ListElement();
		el.textLabel = Title;
		if(urls[index] != null)
			el.buttonLabel = "Read";
		else
			el.buttonLabel = "NA";
		aList.add(el);
		Log.d(LOG_TAG, "The length of the list now is " + aList.size());
		aa.notifyDataSetChanged();
	}

	// Background sleeper.
	// private BackgroundSleeper sleeper = null;
	
	
	// This function can be associated e.g. with a button.
	public void startDownload() {
		downloader = new BackgroundDownloader();
		downloader.execute(DOWNLOAD_URL);
	}
	
    // This class downloads from the net the camera setup instructions.
    private class BackgroundDownloader extends AsyncTask<String, String, String> {
    	
    	protected String doInBackground(String... urls) {
    		Log.d(LOG_TAG, "Starting the download.");
    		String downloadedString = null;
    		String urlString = urls[0];
    		URI url = URI.create(urlString);
    		int numTries = 0;
    		while (downloadedString == null && numTries < MAX_SETUP_DOWNLOAD_TRIES && !isCancelled()) {
    			numTries++;
    			HttpGet request = new HttpGet(url);
    			DefaultHttpClient httpClient = new DefaultHttpClient();
    			HttpResponse response = null;
    			try {
    				response = httpClient.execute(request);
    			} catch (ClientProtocolException ex) {
    				Log.e(LOG_TAG, ex.toString());
    			} catch (IOException ex) {
    				Log.e(LOG_TAG, ex.toString());
    			}
    			if (response != null) {
    				// Checks the status code.
    				int statusCode = response.getStatusLine().getStatusCode();
    				Log.d(LOG_TAG, "Status code: " + statusCode);

    				if (statusCode == HttpURLConnection.HTTP_OK) {
    					// Correct response. Reads the real result.
    					// Extracts the string content of the response.
    					HttpEntity entity = response.getEntity();
    					InputStream iStream = null;
    					try {
    						iStream = entity.getContent();
    					} catch (IOException ex) {
    						Log.e(LOG_TAG, ex.toString());
    					}
    					if (iStream != null) {
    						downloadedString = ConvertStreamToString(iStream);
    						Log.d(LOG_TAG, "Received string: " + downloadedString);
    				    	return downloadedString;
    					}
    				}
    			}
    		}
    		// Returns the instructions, if any.
    		return downloadedString;
    	}
    	
    	protected void onPostExecute(String s) {
    		// This is executed in the UI thread.
    		makeList(s);
    	}
    	
    }
    
    
    @Override
    // This stops the downloader as soon as possible.
    public void onStop() {
    	if (downloader != null) {
    		downloader.cancel(false);
    	}
    	super.onStop();
    }
    
    public static String ConvertStreamToString(InputStream is) {
    	
    	if (is == null) {
    		return null;
    	}
    	
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        Log.d(LOG_TAG, e.toString());
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            Log.d(LOG_TAG, e.toString());
	        }
	    }
	    return sb.toString();
	}

	public void goOther(View V, int index) {
		// Grab the text, and store it in a preference.
		String text1 = titles[index];
		String text2 = urls[index];
		SharedPreferences settings = getSharedPreferences(MYPREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
	    editor.putString(PREF_TITLE, text1);
	    editor.putString(PREF_URL, text2);
	    editor.commit();
	    
	    // Let's produce a string that serializes our class, just for the fun of it.
	    SerialMe me = new SerialMe();
	    me.myURL = urls[index];
	    me.myTitle = titles[index];

		Intent intent = new Intent(this, SecondActivity.class);
		startActivity(intent);
	}


}
