package com.example.impaqsampleproject;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
//import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.example.impaqsampleproject.preferences.PreferencesActivity;

public class ContactsViewerActivity extends Activity {
	
	/** Limit of contacts retrieved from phonebook */
	private final static int CONTACTS_LIMIT = 5;
	
	/** if null the default one /res/values/ips/default_server_ip(port) is used 
	 *  if not null (see line below) - it's used instead the one from /res/values or one from SharedPreferences*/
	private final static String EMERGENCY_HOST = null;	
//	private final static String EMERGENCY_HOST = "192.168.1.103:8881";	
	
	/** Contact adapter storing CONTACTS_LIMIT nr of contacts in ArrayList */
	private MyContactAdapter contactAdapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//get ref to the objects 
		ListView myListView = (ListView)findViewById(R.id.myListView);
		defineButtonClick();
		
		
		//register preference change listerner
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		prefs.registerOnSharedPreferenceChangeListener(this);
		
		// Create the Array List of to do items
		ArrayList<Contact> listContacts = new ArrayList<Contact>();

		// Bind the Array Adapter to the List View
		populateListWithContacts(listContacts);
		
		// Create the Array Adapter to bind the array to the List View
		contactAdapter = new MyContactAdapter(this,	R.layout.single_contact,listContacts);
		myListView.setAdapter(contactAdapter);
		
	}
//	@Override
//	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
//			String key) {
//		Log.d("debug", "preference changed: " + sharedPreferences + " , " + key);
//	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts_viewer, menu);
		
		return true;
		  
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//since so far it's only one position (settings) handle it here!
		Intent intent = new Intent();
        intent.setClass(ContactsViewerActivity.this, PreferencesActivity.class);
        startActivityForResult(intent, 0); 
		
		return true;
	}
	
	/**
	 * Adapter storing the list of Contacts and defined on a basis of 
	 * "view holder" Android design patter.
	 * @author Michal Siemionczyk michal.siemionczyk@gmail.com
	 */
	private class MyContactAdapter extends ArrayAdapter<Contact> {

		private ArrayList<Contact> contactList;

		public MyContactAdapter(Context context, int textViewResourceId,
				ArrayList<Contact> countryList) {
			super(context, textViewResourceId, countryList);
			this.contactList = new ArrayList<Contact>();
			this.contactList.addAll(countryList);
		}

		/* Used to apply view holder pattern for efficiency purposes */
		private class ViewHolder {
			CheckBox name;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder = null;
//			Log.v("ConvertView", Integer.toString(position));

			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.single_contact, null);

				holder = new ViewHolder();
				holder.name = (CheckBox) convertView
						.findViewById(R.id.checkBox1);
				convertView.setTag(holder);

				holder.name.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						Contact c = contactAdapter.getItem(position);
						c.setSelected(cb.isChecked());
					}
				});
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Contact contact = contactAdapter.getItem(position);

			holder.name.setText(contact.getGivenName() + " "
					+ contact.getFamilyName());
			holder.name.setTag(contact);

			return convertView;

		}
	}

	/**
	 * Defines onClickListener of "send" button. Chooses the proper host ip
	 * (from ContactsViewerActivity.EMERGENCY_HOST variable or from Shared
	 * Preferences. *
	 */
	private void defineButtonClick(){
		Button buttonSend = (Button) findViewById(R.id.button1);
		buttonSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Log.d("debug", "button clicked");
				
				String hostIp = "";
				// Please see ContactsViewerActivity.EMERGENCY_HOST declaration
				// for explanation
				if (ContactsViewerActivity.EMERGENCY_HOST == null) {
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(getApplicationContext());
					String host = prefs.getString(PreferencesActivity.HOST_IP,
							getResources()
									.getString(R.string.default_server_ip));
					String port = prefs.getString(
							PreferencesActivity.HOST_PORT, getResources()
									.getString(R.string.default_server_port));
					hostIp = "http://" + host + ":" + port;
				} else {
					hostIp = ContactsViewerActivity.EMERGENCY_HOST;
				}
				
//				Log.d("debug", "starting JSON RequesttAndUpdate " + hostIp);
				new JSONRequestAndUpdateTask().execute(hostIp);

				Toast.makeText(
						getApplicationContext(),
						"Launched new connection asynctask"
								+ " with timeout of (ms) : "
								+ JSONRequestAndUpdateTask.CONNECTION_TIMEOUT,
						Toast.LENGTH_LONG).show();
			}
		});
	}
	/**
	 * Used for a) contacting the server that is running node-swapnames.js script (doInBackground())
	 * 			b) updating the adapter with contacts (onPostExecute())
	 * 
	 * @author Michal Siemionczyk michal.siemionczyk@gmail.com
	 */
	class JSONRequestAndUpdateTask extends AsyncTask<String, Integer, ArrayList<Pair<Integer, String>>>{
		
		public final static int CONNECTION_TIMEOUT = 5000; //in ms
		
		public final static int SO_CONNECTION_TIMEOUT = 5000; // in ms
		
		/**
		 * Performs a set of requests and responses for each selected contact.
		 * Calls are bundled in one connection. All responses are paired with 
		 * position of each contact in adapter, stored in ArrayList<>
		 * and returned to onPostExecute() method.
		 * @param params stores url in the first field of array
		 * @return ArrayList of pairs <position_of_contact, JSON_response>
		 */
		protected ArrayList<Pair<Integer, String>> doInBackground(String... params) {
//			Log.d("debug", "entered doInBackground() in AsyncTask");
			ArrayList<Pair<Integer, String>> responses = new ArrayList<Pair<Integer, String>>();

			final String url = params[0];
			
		    HttpParams myParams = new BasicHttpParams();
		    
		    HttpConnectionParams.setConnectionTimeout(myParams, CONNECTION_TIMEOUT);
		    HttpConnectionParams.setSoTimeout(myParams, SO_CONNECTION_TIMEOUT);
		    HttpClient httpclient = new DefaultHttpClient(myParams);
		 
		    try {
//		    	/*Log*/.d("debug", "from task: before loop");
		    	for (int i = 0; i < contactAdapter.getCount(); i++){
					Contact c = contactAdapter.getItem(i);
		    	
					if (c.isSelected()){
//						Log.d("debug", "from task: entered the loop!");
						
						JSONObject json = new JSONObject();
						try {
							json.put("sname", c.getGivenName());
							json.put("fname", c.getFamilyName());
						} catch (JSONException e) {
							e.printStackTrace();
						}

				        HttpPost httppost = new HttpPost(url.toString());
				      
				        httppost.setHeader("Content-type", "application/json");

				        StringEntity se = new StringEntity(json.toString(), "UTF-8");
				        
				        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				        httppost.setEntity(se); 

//				        Log.d("debug", "String to send (url : " + url + ") : " + json.toString());
				        HttpResponse response = httpclient.execute(httppost);
				        String responseStr= EntityUtils.toString(response.getEntity(), "UTF-8");;
				        
//				        Log.i("response:", responseStr);
				        responses.add(new Pair<Integer, String>(i, responseStr) );
					}
				}

		    } catch (ClientProtocolException e) {
		    	e.printStackTrace();
//		    	Log.w("warn", "catched cpe exception: " + e);
		    	this.cancel(false);

		    } catch (IOException e) {
		    	e.printStackTrace();
//		    	Log.w("warn", "catched IoException : " + e);
		    	//inform the user that the IP cannot be reached.
		    	ContactsViewerActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						AlertDialog.Builder builder = new AlertDialog.Builder(ContactsViewerActivity.this);
							builder.setTitle("Connection problem..")
							.setMessage("Could not connect to " + url + ", " +
									"try changing your host in the settings to default one :" +
									getResources().getString(R.string.default_server_ip) + ":" +  
									getResources().getString(R.string.default_server_port)) 
							.setNegativeButton("OK", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
							AlertDialog alert = builder.create();
							alert.show();
					}
				});
		    	this.cancel(false);
		    }
		    return responses;
		}
		
		/**
		 * Performs update of each contact that received a response and notifies the adapter in the end.
		 */
		protected void onPostExecute(ArrayList<Pair<Integer, String>> responses){
//			Log.d("debug", "start of onPostExecute(): ");
			try {
				for(Pair<Integer,String> response : responses){
					JSONObject json = new JSONObject(response.second);
					
					Contact oldContact = contactAdapter.getItem(response.first);
						oldContact.setGivenName(json.getString("sname"));
						oldContact.setFamilyName(json.getString("fname"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			contactAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Populated ArrayList object given as argument with a number of contacts from phoneBook.
	 * The number of contacts is  defined by constant CONTACTS_LIMIT.
	 * @param listContacts - ArrayList that contacts are saved to.
	 */
	private void populateListWithContacts(ArrayList<Contact> listContacts){
		
        String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + 
        		ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME + " NOT NULL AND "+
        		ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME + " NOT NULL";
        String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
        String sortOrder = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME + " LIMIT " + CONTACTS_LIMIT;
        
        
        Cursor nameCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, 
        		whereName, whereNameParams,sortOrder );
        while (nameCur.moveToNext()) {
            String given = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String family = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            listContacts.add(0, new Contact(given, family) );
        }
        nameCur.close();
	}
}
