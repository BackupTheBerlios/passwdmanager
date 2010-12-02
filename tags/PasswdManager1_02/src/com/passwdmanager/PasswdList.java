/*
 *  Copyright (C) 2010, Raúl Román López.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/. 
 *
 *  Author : Raúl Román López <rroman@gsyc.es>
 *
 */

package com.passwdmanager;


import java.util.ArrayList;

import com.passwdmanager.files.FileManager;
import com.passwdmanager.security.SecurityManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PasswdList extends ListActivity{
	private static final int DIALOG_SHOW = 0;
	private static final int DIALOG_DELETE = 1;
	private static final int DIALOG_EXPORT = 2;
	private static final int DIALOG_PBAR = 3;
	
	private static final int MENU_DELETE = Menu.FIRST + 1;
	private static final int MENU_EXPORT = Menu.FIRST + 2;
	
	private static ArrayList<PasswdResource> passwords = null;
	private static User user = null;
	
	private PwdAdapter mAdapter;
	
	private static int pwdClicked = -1;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			removeDialog(DIALOG_PBAR);
			if(msg.what == 0)
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.ok), 
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.error), 
						Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);		 
		
		registerForContextMenu(this.getListView());
		
		getListView().setBackgroundResource(R.drawable.background);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		getListView().setDivider(null);
		
		if (passwords == null || passwords.isEmpty()){
			passwords = (ArrayList<PasswdResource>)getIntent().getSerializableExtra("LIST");
			user = (User)getIntent().getSerializableExtra("USER");
		}
		mAdapter = new PwdAdapter(getBaseContext());
		setListAdapter(mAdapter);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if ((passwords != null) && (keyCode == KeyEvent.KEYCODE_BACK))
			passwords.clear();

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		pwdClicked = position;
		showDialog(DIALOG_SHOW);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,  ContextMenuInfo menuInfo){ 
		super.onCreateContextMenu(menu, view, menuInfo);
		
		menu.add(0, MENU_DELETE, 0 , getResources().getString(R.string.list_delete));
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case MENU_DELETE:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
					.getMenuInfo();
			pwdClicked = info.position;
			showDialog(DIALOG_DELETE);
			break;
		}
		return true;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	menu.add(0, MENU_EXPORT, 0, R.string.list_export)
    	.setIcon(R.drawable.icon_export);
    	
        super.onCreateOptionsMenu(menu);        
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch(item.getItemId()){
    		case MENU_EXPORT:
    			showDialog(DIALOG_EXPORT);
    			break;
    	}
        return super.onOptionsItemSelected(item);
    }
	
	@Override
    protected Dialog onCreateDialog(int id) {       
    	
    	switch (id) {
    	case DIALOG_PBAR:
    		ProgressDialog dialog = new ProgressDialog(this);
    		dialog.setMessage(getResources().getString(R.string.dialog_pbar));
    		dialog.setIndeterminate(true);
    		dialog.setCancelable(false);
    		return dialog;
    	case DIALOG_DELETE:
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setIcon(R.drawable.icon)
    		.setTitle(getResources().getString(R.string.list_delete))
    		.setMessage(getResources().getString(R.string.dialog_delete))
    		.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_DELETE);
				}
			})
    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_DELETE);
					FileManager.getInstance().removeNode(getBaseContext(), user.getUsername(), passwords.get(pwdClicked));
					passwords.remove(pwdClicked);
					mAdapter.notifyDataSetChanged();
				}
			})
    		.create();
    		
    	case DIALOG_SHOW:
    		LayoutInflater factory = LayoutInflater.from(this);
    		final View textEntryView = factory.inflate(R.layout.passwd_info, null);
    		
    		PasswdResource pr = passwords.get(pwdClicked);
    		
    		TextView tv = (TextView)textEntryView.findViewById(R.id.info_site);
    		tv.setText(pr.getSite());
    		
    		tv = (TextView)textEntryView.findViewById(R.id.info_user);
    		tv.setText(pr.getName());
    		
    		tv = (TextView)textEntryView.findViewById(R.id.info_password);
    		tv.setText(SecurityManager.decodeXOR(pr.getPassword(), user.getPassword()));
    		
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setView(textEntryView)
    		.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_SHOW);
				}
			})
    		.create();
    		
    	case DIALOG_EXPORT:
    		return new AlertDialog.Builder(this)
    		.setTitle(R.string.list_export)
    		.setIcon(R.drawable.icon)
    		.setCancelable(true)
    		.setSingleChoiceItems(R.array.formats, -1,
    				new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				pwdClicked = which;
    			}
    		})
    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_EXPORT);
					showDialog(DIALOG_PBAR);
					
					new Thread(){
						public void run(){
							if(FileManager.getInstance().createExternalUserPasswords(user.getUsername(), passwords, pwdClicked))
								mHandler.sendEmptyMessage(0);
							else
								mHandler.sendEmptyMessage(-1);
						}
					}.start();
				}
			})
    		.create();
    		
    	}
    	return null;
	}
	
	
	
	
	
	
	
	
	private static class PwdAdapter extends BaseAdapter {

		private Context mContext;

		public PwdAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			// TODO Auto-generated method stub
			if(passwords == null)
				return 0;
			return passwords.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view;

			if (convertView == null) {
				// Make up a new view
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.passwd_item, null);
			} else {
				// Use convertView if it is available
				view = convertView;
			}
			
			TextView tv_site = (TextView) view.findViewById(R.id.list_site);
			tv_site.setText(passwords.get(position).getSite());
			
			return view;

		}

	}
}