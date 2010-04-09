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

import com.passwdmanager.files.FileManager;
import com.passwdmanager.security.SecurityManager;
import com.passwdmanager.utils.Validation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateAccount extends Activity {
	
//	private static final int MENU_NEW_ACCOUNT = Menu.FIRST + 1;
	
	private OnClickListener cl = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String username = ((EditText)findViewById(R.id.register_edit_username)).getText().toString();
			String pwd = ((EditText)findViewById(R.id.register_edit_password)).getText().toString();
			String pwd_repeat = ((EditText)findViewById(R.id.register_edit_re_password)).getText().toString();
			
			username = username.trim();
			pwd = pwd.trim();
			pwd_repeat = pwd_repeat.trim();
			
			if(!Validation.validate(username) || !Validation.validate(pwd) || !Validation.validate(pwd_repeat)){
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.error_wrongdata), 
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			
			if(!pwd.equals(pwd_repeat)){
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.register_error_passwords), 
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			String encripted_pwd = SecurityManager.encodePassword(pwd);
			
			User user = new User();
			user.setUsername(username);
			user.setPassword(encripted_pwd);
			
			if(FileManager.getInstance().createUserData(getBaseContext(), user))
				Toast.makeText(getBaseContext(), 
						"Ok", 
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.register_error_userexist), 
						Toast.LENGTH_SHORT).show();
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        Button bt_create = (Button)findViewById(R.id.register_button_ok);
		bt_create.setOnClickListener(cl);
    }
    
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//    	
//    	menu.add(0, MENU_NEW_ACCOUNT, 0, getResources().getString(R.string.login_menu_new_account));
//    	
//        super.onCreateOptionsMenu(menu);        
//        return true;
//    }
//    
//    public boolean onOptionsItemSelected (MenuItem item) {
//    	switch(item.getItemId()){
//    		case MENU_NEW_ACCOUNT:
//    			
//    			break;
//    	}
//        return super.onOptionsItemSelected(item);
//    }
}