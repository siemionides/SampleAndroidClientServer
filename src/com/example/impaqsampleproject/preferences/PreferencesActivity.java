package com.example.impaqsampleproject.preferences;

import android.app.Activity;
import android.os.Bundle;
//import android.util.Log;

public class PreferencesActivity extends Activity {
	
		public static String HOST_IP = "PREF_IP";
		
		public static String HOST_PORT = "PREF_PORT";
	
	   @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Display the fragment as the main content.
	        getFragmentManager().beginTransaction()
	                .replace(android.R.id.content, new PreferencesFragment())
	                .commit();
	    }
	   
	
}
