package com.example.impaqsampleproject;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.impaqsampleproject.Contact;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class ContactsViewerActivity extends Activity {
	
	private final int CONTACTS_LIMIT = 5;
	
	private MyContactAdapter contactAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//get ref to the objects 
		ListView myListView = (ListView)findViewById(R.id.myListView);
		defineButtonClick();


		
		// Create the Array List of to do items
		final ArrayList<Contact> listContacts = new ArrayList<Contact>();

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
			 
			   Contact contact = contactList.get(position);
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
	
	/** Defines "send" button listener */
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
				for (Contact c : selectedContacts){
					JSONObject json = new JSONObject();
					try {
						json.put("sname", c.getGivenName());
						json.put("fname", c.getFamilyName());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Log.d("debug", json.toString());
				}
				
				JSONObject json = new JSONObject();
			}
			
		});
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
}
