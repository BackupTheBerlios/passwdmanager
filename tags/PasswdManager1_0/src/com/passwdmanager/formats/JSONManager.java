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

package com.passwdmanager.formats;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.passwdmanager.PasswdResource;
import com.passwdmanager.User;

public class JSONManager{
	
	public static String makeFile(String username, ArrayList<PasswdResource> passwords){
		String data = "{ \n\t\"username\": \"" + username + "\", \n";
		data += "\t\"sites\": [";
		
		int max = passwords.size();
		for(int i = 0; i < max; i++){
			PasswdResource pr = passwords.get(i);
			if(i>0)
				data += ", ";
			data += "\n\t\t{";
			data += "\t\t\t\"sitename\": \"" + pr.getSite() + "\", \n";
			data += "\t\t\t\"name\": \"" + pr.getName() + "\", \n";
			data += "\t\t\t\"password\": \"" + pr.getPassword() + "\"\n";
			data += "\t\t}";
		}
		
		data += "\n\t]\n";
		data += "}";
		return data;
	}
	
	public static ArrayList<PasswdResource> loadFile(String file){
		if(file == null)
			return null;
		try {
			JSONObject jo = new JSONObject(file);
			ArrayList<PasswdResource> passwords = new ArrayList<PasswdResource>();
			if(jo.has("sites")){
				JSONArray list = jo.getJSONArray("sites");
				int max = list.length();
				for (int i = 0; i<max; i++){
					JSONObject site = list.getJSONObject(i);
					if(!site.has("sitename") || !site.has("name") || !site.has("password")){
						Log.e("JSONManager", "Wrong object!");
						continue;
					}
					PasswdResource pr = new PasswdResource();
					pr.setSite(site.getString("sitename"));
					pr.setName(site.getString("name"));
					pr.setPassword(site.getString("password"));
					passwords.add(pr);
				}
			}
			return passwords;
		} catch (JSONException e) {
			Log.e("JSONManager", "", e);
		}
		return null;
	}
	
	public static String makeUserFile(User user){

		String data = "{ \n\t\"username\": \"" + user.getUsername() + "\", \n";
		data += "\t\"password\": \"" + user.getPassword() + "\" \n}";
		
		return data;
	}
	
	public static User loadUserFile(String data){
		try {
			JSONObject jo = new JSONObject(data);
			if(jo.has("username") && jo.has("password")){
				User user = new User();
				user.setUsername(jo.getString("username"));
				user.setPassword(jo.getString("password"));
				return user;
			}
		} catch (JSONException e) {
			Log.e("JSONManager", "", e);
		}
		return null;
	}
	
}

