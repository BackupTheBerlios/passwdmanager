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

import com.passwdmanager.files.PasswdManagerDB;
import com.passwdmanager.security.SecurityManager;
import com.passwdmanager.utils.Validation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class MainMenu extends Activity{
	private static final int MENU_DELETE = Menu.FIRST + 1;
	
	private static final int DIALOG_PBAR = 0;
	private static final int DIALOG_ADD = 1;
	private static final int DIALOG_SEARCH = 2;
	private static final int DIALOG_DELETE = 3;
	
	private User user;
	
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
				i.putExtra("USER", user);
				startActivity(i);
			}
		});
        
        TextView tv_user = (TextView)findViewById(R.id.main_username);
        
        user = (User)getIntent().getSerializableExtra("USER");
        tv_user.setText(user.getUsername());
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
					if(PasswdManagerDB.getInstance(getBaseContext()).deleteUser(user.getUsername())){
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
    		final EditText et_pwd = (EditText)textEntryView.findViewById(R.id.add_edit_password);
    		
    		((CheckBox)textEntryView.findViewById(R.id.add_cb_password)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked)
						et_pwd.setTransformationMethod(null);
					else
						et_pwd.setTransformationMethod(new PasswordTransformationMethod());
				}
			});
    		
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setIcon(R.drawable.icon)
    		.setTitle(getResources().getString(R.string.add_title))
    		.setView(textEntryView)
    		.setPositiveButton(getResources().getString(R.string.add_button), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				removeDialog(DIALOG_ADD);
    				final String site = ((EditText)textEntryView.findViewById(R.id.add_edit_site)).getText().toString();
    	    		final String username = ((EditText)textEntryView.findViewById(R.id.add_edit_user)).getText().toString();
    	    		final String password = ((EditText)textEntryView.findViewById(R.id.add_edit_password)).getText().toString();
    	    		
    	    		if(!Validation.validate(username, Validation.PATTERN) || !Validation.validate(site, Validation.PATTERN_SITE) || !Validation.validate(password, Validation.PATTERN)){
    					Toast.makeText(getBaseContext(), 
    							getResources().getString(R.string.error_wrongdata), 
    							Toast.LENGTH_SHORT).show();
    					return;
    				}

    				String encrypted_pwd = SecurityManager.encodeXOR(password, user.getPassword());
    				PasswdResource pr = new PasswdResource();
    				pr.setSite(site);
    				pr.setName(username);
    				pr.setPassword(encrypted_pwd);
    				
    				if(PasswdManagerDB.getInstance(getBaseContext()).insertPasswd(user, pr))
    					Toast.makeText(getBaseContext(), 
    							getString(R.string.ok), 
    							Toast.LENGTH_SHORT).show();
    				else
    					Toast.makeText(getBaseContext(), 
    							getString(R.string.main_error_adding), 
    							Toast.LENGTH_SHORT).show();
    				
    			}
    		})
    		.create();
    		
    	case DIALOG_SEARCH:
    		LayoutInflater factory2 = LayoutInflater.from(this);
    		final View textEntryView2 = factory2.inflate(R.layout.search, null);
    		
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setIcon(R.drawable.icon)
    		.setTitle(getResources().getString(R.string.dialog_search))
    		.setView(textEntryView2)
    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				removeDialog(DIALOG_SEARCH);
    				
    				String site = ((EditText)textEntryView2.findViewById(R.id.search_edit_site)).getText().toString();
    	    		String username = ((EditText)textEntryView2.findViewById(R.id.search_edit_user)).getText().toString();
    	    		
    				Intent i = new Intent(getBaseContext(), PasswdList.class);
    				i.putExtra("SITENAME", site);
    				i.putExtra("USERNAME", username);
    				i.putExtra("USER", user);
    				startActivity(i);
    			}
    		})
    		.create();
    	}
    	return null;
	}
}