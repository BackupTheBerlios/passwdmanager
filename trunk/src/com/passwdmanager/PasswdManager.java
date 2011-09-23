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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PasswdManager extends Activity {
	
	private static final int MENU_NEW_ACCOUNT = Menu.FIRST + 1;
	private static final int MENU_ABOUT = Menu.FIRST + 2;

	private static final int DIALOG_ABOUT = 0;
	
	private static final String KEY_USERNAME = "username";

	private EditText et_username;
	
	private OnClickListener loginListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String username = et_username.getText().toString();
			String pwd = ((EditText)findViewById(R.id.login_edit_password)).getText().toString();
			
			if(!Validation.validate(username, Validation.PATTERN) || !Validation.validate(pwd, Validation.PATTERN)){
				Toast.makeText(PasswdManager.this, 
						getResources().getString(R.string.error_wrongdata), 
						Toast.LENGTH_SHORT).show();
				return;
			}
			String encoded_pwd = SecurityManager.encodePassword(pwd);
			
			User user = new User();
			user.setUsername(username);
			user.setPassword(encoded_pwd);
			
			if(PasswdManagerDB.getInstance(getBaseContext()).checkUser(user)){
				user.setPassword(pwd);
				saveConfig();
				
				Intent i = new Intent(getBaseContext(), MainMenu.class);
				i.putExtra("USER", user);
				startActivity(i);
				finish();
			}else
				Toast.makeText(PasswdManager.this, 
						getResources().getString(R.string.login_error), 
						Toast.LENGTH_SHORT).show();
				
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

		et_username = (EditText)findViewById(R.id.login_edit_username);
        Button bt_login = (Button)findViewById(R.id.login_button_ok);
        bt_login.setOnClickListener(loginListener);
        
        loadConfig();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	menu.add(0, MENU_NEW_ACCOUNT, 0, R.string.login_menu_new_account)
    	.setIcon(R.drawable.icon_user);
    	menu.add(0, MENU_ABOUT, 0, R.string.login_menu_about)
    	.setIcon(R.drawable.about);
    	
        super.onCreateOptionsMenu(menu);        
        return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch(item.getItemId()){
    		case MENU_NEW_ACCOUNT:
    			Intent i = new Intent(this, CreateAccount.class);
    			startActivity(i);
    			break;
    			
    		case MENU_ABOUT:
    			showDialog(DIALOG_ABOUT);
    			break;
    	}
        return super.onOptionsItemSelected(item);
    }
    
	@Override
    protected Dialog onCreateDialog(int id) {       
    	
    	switch (id) {
    	case DIALOG_ABOUT:
    		LayoutInflater factory = LayoutInflater.from(this);
    		final View textEntryView = factory.inflate(R.layout.about, null);
    		
    		TextView tv = (TextView)textEntryView.findViewById(R.id.about);
    		try {
				tv.setText(getResources().getString(R.string.app_name) + " v" + 
						getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n" +
						getResources().getString(R.string.about));
			} catch (Exception e) {}
    		
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setView(textEntryView)
    		.setIcon(R.drawable.about)
    		.setTitle(R.string.login_menu_about)
    		.setPositiveButton(R.string.login_menu_about_web, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse("http://passwdmanager.berlios.de/"));
					startActivity(i);
				}
			})
    		.create();
    	}
    	return null;
	}
    
    private void loadConfig(){
    	
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	
		String username = sharedPreferences.getString(KEY_USERNAME, null);
		
		if (username != null)			
			et_username.setText(username);	
			
    }
    
    private void saveConfig (){
    	
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		Editor editor = sharedPreferences.edit();
		
		editor.putString(KEY_USERNAME, et_username.getText().toString());
		
		editor.commit();
    }
}