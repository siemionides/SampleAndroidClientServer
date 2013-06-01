package com.example.impaqsampleproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsViewerActivity extends Activity {
	
	private final int CONTACTS_LIMIT = 5;
	
	private MyContactAdapter contactAdapter;
	
//	/** To store contacts that were selected with */
//	private Set<Integer> lastSelectedPos = new HashSet<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//get ref to the objects 
		ListView myListView = (ListView)findViewById(R.id.myListView);
		defineButtonClick();

		
		// Create the Array List of to do items
		ArrayList<Contact> listContacts = new ArrayList<Contact>();

		// Bind the Array Adapter to the List View
		populateListWithContacts(listContacts);
		
		// Create the Array Adapter to bind the array to the List View
		contactAdapter = new MyContactAdapter(this,	R.layout.single_contact,listContacts);
		myListView.setAdapter(contactAdapter);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contacts_viewer, menu);
		return true;
	}
	
	private class MyContactAdapter extends ArrayAdapter<Contact> {
			 
			  private ArrayList<Contact> contactList;
			 
			  public MyContactAdapter(Context context, int textViewResourceId, ArrayList<Contact> countryList) {
				   super(context, textViewResourceId, countryList);
				   this.contactList = new ArrayList<Contact>();
				   this.contactList.addAll(countryList);
			  }
			  
			  /* Used to apply view holder pattern for efficiency purposes */
			  private class ViewHolder {
				  CheckBox name;
			  }
			 
			  @Override
			  public View getView(final int position, View convertView, ViewGroup parent) {
			 
			   ViewHolder holder = null;
			   Log.v("ConvertView",Integer.toString(position));
			 
			   if (convertView == null) {
				   LayoutInflater vi = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE);
				   convertView = vi.inflate(R.layout.single_contact, null);
				 
				   holder = new ViewHolder();
				   holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
				   convertView.setTag(holder);
				 
				   holder.name.setOnClickListener( new View.OnClickListener() {  
				     public void onClick(View v) {  
				      CheckBox cb = (CheckBox) v ;  
				      Toast.makeText(getApplicationContext(), "Clicked on Checkbox: " 
				    		  + cb.getText() + " is " + cb.isChecked(), Toast.LENGTH_LONG).show();
				      setContactSelected(contactAdapter, position, cb.isChecked());
				     }  
				    });  
			   } 
			   else {
				   holder = (ViewHolder) convertView.getTag();
			   }
			 
			   Contact contact = contactAdapter.getItem(position);
	//		   contact.g
	//		   contact.
	//		   Contact con = new Contact("ja", "a");
	//		   contact.getClass()
	//		   con.getGivenName();
			   //
			   holder.name.setText(contact.getGivenName() + " " + contact.getFamilyName() );
			   holder.name.setTag(contact);
			 
			   return convertView;
			 
			  }
			 }

	/** Sets contact as selected through adapter */
	private void setContactSelected(MyContactAdapter adapter, int position, boolean isSelected){
		adapter.contactList.get(position).setSelected(isSelected);
	}
	
	/** Defines "send" button on click listener */
	private void defineButtonClick(){
		Button buttonSend = (Button) findViewById(R.id.button1);
		
		//add button listener
		buttonSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ArrayList<Contact> selectedContacts = new ArrayList<Contact>();
				ArrayList<Contact> contactList = contactAdapter.contactList;

				for (Contact c : contactList) {
					if (c.isSelected())
						selectedContacts.add(c);
				}
				
				
				Log.d("debug", "Selected contacts:");
				
				String hostIp = "http://" + 
						getResources().getString(R.string.default_server_ip) + ":" + 
						getResources().getString(R.string.default_server_port);
				
				for (Contact c : selectedContacts){
					
					new JSONRequestAndUpdateTask().execute(hostIp, c.getGivenName(), 
							c.getFamilyName(), String.valueOf(contactAdapter.getPosition(c)));
				
				}
				
				JSONObject json = new JSONObject();
			}
			
		});
	}
	/** Param, Progress, Result*/
	class JSONRequestAndUpdateTask extends AsyncTask<String, Integer, String>{

		private int positionOfContact;
		
		
		protected String doInBackground(String... params) {
			String url = params[0];
			String givenName = params[1];
			String familyName = params[2];
			String position = params[3];
			
			//safely cast int to java
			try{
				positionOfContact = Integer.parseInt(position);
			}catch (NumberFormatException nfe){
				Log.e("error", "Cannot cast String to Int within JSONRequestAndUpdateTask.doInBackground() method.");
				return "";
			}
			
			
			JSONObject json = new JSONObject();
			try {
				json.put("sname", givenName);
				json.put("fname", familyName);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// here add request and response 
			Log.d("debug", "String to send (url : " + url + ") : " + json.toString());
			//String response = postData(hostIp, json);
		    HttpParams myParams = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(myParams, 10000);
		    HttpConnectionParams.setSoTimeout(myParams, 10000);
		    HttpClient httpclient = new DefaultHttpClient();
//		    json=obj.toString();
		    String returnStr = "";
		    try {

		        HttpPost httppost = new HttpPost(url.toString());
		        httppost.setHeader("Content-type", "application/json");

		        StringEntity se = new StringEntity(json.toString()); 
		        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		        httppost.setEntity(se); 

		        HttpResponse response = httpclient.execute(httppost);
		        returnStr= EntityUtils.toString(response.getEntity());
		        Log.i("response:", returnStr);


		    } catch (ClientProtocolException e) {
		    	e.printStackTrace();

		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
		    return returnStr;
			
//			return null;
		}
		
		
		protected void onPostExecute(String response){
			Log.d("debug", "Response: " + response);
			try {
				JSONObject json = new JSONObject(response);
				
				//change contact within adateper
				Contact oldContact = contactAdapter.getItem(positionOfContact);
			
				oldContact.setGivenName(json.getString("sname"));
				oldContact.setFamilyName(json.getString("fname"));
//				oldContact = new Contact(json.getString("sname"), json.getString("fname"));
//				
//				Log.d("debug", "adapter getCount before :" + contactAdapter.getCount());
//				contactAdapter.add(oldContact);
//				contactAdapter.add(new Contact("new", "name"));
//				Log.d("debug", "Size of List after I :" + contactAdapter.contactList.size());
				contactAdapter.notifyDataSetChanged();
//				Log.d("debug", "adateper getCount After :" + contactAdapter.getCount());
				
				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void populateListWithContacts(ArrayList<Contact> listContacts){
		
        String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + 
        		ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME + " NOT NULL AND "+
        		ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME + " NOT NULL";
        String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
        String sortOrder = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME + " LIMIT " + CONTACTS_LIMIT;
        
//        Log.d("debug", "where:" + whereName);
        
        
        Cursor nameCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, 
        		whereName, whereNameParams,sortOrder );
        while (nameCur.moveToNext()) {
            String given = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String family = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
//           String display = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME));
            listContacts.add(0, new Contact(given, family) );
//            Log.d("CONTACTS", given + " "  + family + " " + display);
        }
        nameCur.close();
        
//        aa.notifyDataSetChanged();
        
	}
	
/*	*//** Synchronous method for making a post via  Apache HttpClient library*//*
	public String postData(String url,JSONObject obj) {
	    // Create a new HttpClient and Post Header


	}*/
	
}
