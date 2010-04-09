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
import java.util.Collections;

import com.passwdmanager.files.FileManager;
import com.passwdmanager.security.SecurityManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainMenu extends Activity{
	private static final int MENU_DELETE = Menu.FIRST + 1;
	
	private static final int DIALOG_PBAR = 0;
	private static final int DIALOG_ADD = 1;
	private static final int DIALOG_SEARCH = 2;
	private static final int DIALOG_DELETE = 3;
	
	private ArrayList<PasswdResource> passwords;
	
	private User user;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			removeDialog(DIALOG_PBAR);
			if(passwords == null)
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.main_no_passwords), 
						Toast.LENGTH_SHORT);
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button bt_addpwd = (Button)findViewById(R.id.main_bt_add);
        bt_addpwd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_ADD);
			}
		});
        
        Button bt_import = (Button)findViewById(R.id.main_bt_import);
        bt_import.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), ExternalList.class);
				i.putExtra("USER", user);
				startActivity(i);
			}
		});
        
        Button bt_search = (Button)findViewById(R.id.main_bt_search);
        bt_search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_SEARCH);
			}
		});
        
        Button bt_showall = (Button)findViewById(R.id.main_bt_showall);
        bt_showall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), PasswdList.class);
				i.putExtra("LIST", passwords);
				i.putExtra("USER", user);
				startActivity(i);
			}
		});
        
        TextView tv_user = (TextView)findViewById(R.id.main_username);
        
        user = (User)getIntent().getSerializableExtra("USER");
        tv_user.setText(user.getUsername());
    }
    
    @Override
    protected void onResume(){
    	showDialog(DIALOG_PBAR);
    	new Thread(){
    		public void run(){
    	    	passwords = FileManager.getInstance().readUserPasswords(getBaseContext(), user.getUsername());
    	    	mHandler.sendEmptyMessage(0);
    		}
    	}.start();
    	super.onResume();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	menu.add(0, MENU_DELETE, 0, getResources().getString(R.string.main_menu_remove))
    	.setIcon(R.drawable.button_remove);
    	
        super.onCreateOptionsMenu(menu);        
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch(item.getItemId()){
    		case MENU_DELETE:
    			showDialog(DIALOG_DELETE);
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
					if(FileManager.getInstance().removeUser(getBaseContext(), user.getUsername())){
	    				Intent i = new Intent(getBaseContext(), PasswdManager.class);
	    				startActivity(i);
	    				finish();
	    			}else
	    				Toast.makeText(getBaseContext(), 
								getResources().getString(R.string.main_error_removing), 
								Toast.LENGTH_SHORT).show();
				}
			})
    		.create();
    		
    	case DIALOG_ADD:
    		LayoutInflater factory = LayoutInflater.from(this);
    		final View textEntryView = factory.inflate(R.layout.add_pwd, null);
    		
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setTitle(getResources().getString(R.string.add_title))
    		.setView(textEntryView)
    		.setPositiveButton(getResources().getString(R.string.add_button), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				removeDialog(DIALOG_ADD);
    				final String site = ((EditText)textEntryView.findViewById(R.id.add_edit_site)).getText().toString();
    	    		final String username = ((EditText)textEntryView.findViewById(R.id.add_edit_user)).getText().toString();
    	    		final String password = ((EditText)textEntryView.findViewById(R.id.add_edit_password)).getText().toString();
    	    		
    				if(site.equals("") || username.equals("") || password.equals("")){
    					Toast.makeText(getBaseContext(), 
    							getResources().getString(R.string.error_wrongdata), 
    							Toast.LENGTH_SHORT).show();
    					return;
    				}
    				
    				showDialog(DIALOG_PBAR);
    				new Thread(){
    					public void run(){
    						if(passwords == null)
    							passwords = new ArrayList<PasswdResource>();
    						int max = passwords.size();
    	    				for(int i = 0; i < max; i++){
    	    					if(passwords.get(i).getSite().equals(site)){
    	    						Toast.makeText(getBaseContext(), 
    	        							getResources().getString(R.string.add_error_siteexist), 
    	        							Toast.LENGTH_SHORT).show();
    	    						mHandler.sendEmptyMessage(0);
    	        					return;
    	    					}
    	    				}
    	    				String encrypted_pwd = SecurityManager.encodeXOR(password, user.getPassword());
    	    				PasswdResource pr = new PasswdResource();
    	    				pr.setSite(site);
    	    				pr.setName(username);
    	    				pr.setPassword(encrypted_pwd);
    	    				
    	    				passwords.add(pr);
    	    				
    	    				// Sort nodes by site
    	    				PasswdResourceComparator comparator = new PasswdResourceComparator();
    	    				Collections.sort(passwords, comparator);
    	    				
    	    				FileManager.getInstance().createUserPasswords(getBaseContext(), user.getUsername(), passwords);
    						mHandler.sendEmptyMessage(0);
    					}
    				}.start();
    				
    			}
    		})
    		.create();
    		
    	case DIALOG_SEARCH:
    		LayoutInflater factory2 = LayoutInflater.from(this);
    		final View textEntryView2 = factory2.inflate(R.layout.search, null);
    		
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setTitle(getResources().getString(R.string.dialog_search))
    		.setView(textEntryView2)
    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				removeDialog(DIALOG_SEARCH);
    				if(passwords == null)
    					return;
    				
    				final String site = ((EditText)textEntryView2.findViewById(R.id.search_edit_site)).getText().toString();
    	    		final String username = ((EditText)textEntryView2.findViewById(R.id.search_edit_user)).getText().toString();
    	    		
    	    		showDialog(DIALOG_PBAR);
    	    		
    	    		final ArrayList<PasswdResource> list = new ArrayList<PasswdResource>();
    	    		final Handler handler = new Handler(){
    	    			@Override
    	    			public void handleMessage(Message msg){
    	    				removeDialog(DIALOG_PBAR);
    	    				Intent i = new Intent(getBaseContext(), PasswdList.class);
    	    				i.putExtra("LIST", list);
    	    				i.putExtra("USER", user);
    	    				startActivity(i);
    	    			}
    	    		};
    	    		
    	    		new Thread(){
    	    			public void run(){
    	    				int max = passwords.size();
    	    				for(int i = 0; i < max; i++){
    	    					PasswdResource pr = passwords.get(i);
    	    					if((site.equals("") || (pr.getSite().contains(site))) &&
    	    							(username.equals("") || (pr.getName().contains(username))))
    	    						list.add(pr);
    	    				}
    	    				handler.sendEmptyMessage(0);
    	    			}
    	    		}.start();
    			}
    		})
    		.create();
    	}
    	return null;
	}
}