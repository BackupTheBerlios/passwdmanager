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

package com.passwdmanager.files;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;

import com.passwdmanager.PasswdResource;
import com.passwdmanager.User;
import com.passwdmanager.formats.CSVManager;
import com.passwdmanager.formats.JSONManager;
import com.passwdmanager.formats.TEXManager;
import com.passwdmanager.formats.XMLManager;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileManager{
	private static final boolean FILE_EXTERNAL = true;
	private static final boolean FILE_INTERNAL = false;
	
	public static final int XML = 0;
	public static final int JSON = 1;
	public static final int TEX = 2;
	public static final int CSV = 3;
	
	private static final String[] FORMATS = {".xml", ".json", ".tex", ".csv"};
	private static final String SD_DIRECTORY = Environment.getExternalStorageDirectory() + "/PasswdManager/";
	private static final String LIST_NAME = "_passwords";
	private static final String USER_DATA = "_data.json";
	
	private static FileManager singleton = null;
	
	public static FileManager getInstance(){
		if(singleton == null)
			singleton = new FileManager();
		return singleton;
	}
	
	private boolean checkSD(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	public boolean createUserData(Context context, User user){
		String filename = user.getUsername() + USER_DATA;
		String data = JSONManager.makeUserFile(user);
		
		if(exists(context, filename))
			return false;
		return write(context, filename, data);
	}
	
	public void createUserPasswords(Context context, String username, ArrayList<PasswdResource> passwords){
		String filename = username + LIST_NAME + ".json";
		String data = JSONManager.makeFile(username, passwords);

		write(context, filename, data);
	}
	
	private boolean write(Context context, String filename, String data){
		FileOutputStream fOut = null;
		OutputStreamWriter osw = null;
		try{
			fOut = context.openFileOutput(filename, Context.MODE_PRIVATE);      
			osw = new OutputStreamWriter(fOut);
			
			Log.d("FileManager", data);

			osw.write(data);
			osw.flush();

			osw.close();
			fOut.close();
			
			return true;
		}
		catch (Exception e) {      
			Log.e("FileManager", "", e);
		}
		return false;
	}
	
	private boolean exists(Context context, String filename){
		DataInputStream dis;
		FileInputStream fIn;
		
		try {
			fIn = context.openFileInput (filename);
			BufferedInputStream bis = new BufferedInputStream(fIn);  
			dis = new DataInputStream(bis);
			
			String data = "";
			String line;
			while ( (line = dis.readLine()) != null ) {  
				data += line;
			}    
	        fIn.close();
	        
	        if(!data.equals(""))
	        	return true;
	        
		} catch (FileNotFoundException e) {
			Log.e("FileManager", "", e);
		} catch (IOException e) {
			Log.e("FileManager", "", e);
		}
		return false;
	}
	
	public boolean removeUser(Context context, String username){
		String filename = username + USER_DATA;
		boolean res = context.deleteFile(filename);
		
		filename = username + LIST_NAME + ".json";
		context.deleteFile(filename);
		return res;
	}
	
	public boolean removeNode(Context context, String username, PasswdResource password){
		ArrayList<PasswdResource> passwords = readUserPasswords(context, username);
		int max = passwords.size();
		boolean res = false;
		
		for(int i = 0; i < max; i++){
			PasswdResource pr = passwords.get(i);
			if(pr.getSite().equals(password.getSite())){
				passwords.remove(pr);
				createUserPasswords(context, username, passwords);
				res = true;
				break;
			}
		}
		
		return res;
	}
	
	
	public User readUserData(Context context, String username){
		String filename = username + USER_DATA;
		String data = read(context, filename, FILE_INTERNAL);
		
		if(!exists(context, filename))
			return null;
		return JSONManager.loadUserFile(data);
	}
	
	public ArrayList<PasswdResource> readUserPasswords(Context context, String username){
		String filename = username + LIST_NAME + ".json";
		String data = read(context, filename, FILE_INTERNAL);
		
		return JSONManager.loadFile(data);
	}
	
	public boolean createExternalUserPasswords(String username, ArrayList<PasswdResource> passwords, int format){
		if(!checkSD())
			return false;
		
		File dir = new File(SD_DIRECTORY);
		if (!dir.exists())
			dir.mkdir();
		
		dir = new File(SD_DIRECTORY + username + "/" );
		if (!dir.exists())
			dir.mkdir();
		
		String data = "";
		switch(format){
		case XML:
			 data = XMLManager.makeFile(username, passwords);
			break;
		case JSON:
			data = JSONManager.makeFile(username, passwords);
			break;
		case TEX:
			data = TEXManager.makeFile(username, passwords);
			break;
		case CSV:
			data = CSVManager.makeFile(username, passwords);
			break;
		}
		String date = Calendar.getInstance().get(Calendar.YEAR) + "-" +
				(Calendar.getInstance().get(Calendar.MONTH) + 1) + "-" +
				Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" +
				Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + "-" +
				Calendar.getInstance().get(Calendar.MINUTE) + "-" +
				Calendar.getInstance().get(Calendar.SECOND);
		String filename = date + FORMATS[format];
		
		File outputFile = new File(SD_DIRECTORY + username + "/" + filename); 
		if(outputFile.exists()){
			outputFile.delete();
			outputFile = new File(SD_DIRECTORY + username + "/" + filename); 
		}
		
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(outputFile,false));
			out.write(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("FileManager", "", e);
			return false;
		} 
		
		return true;
	}
	
	public boolean removeExternalFile(String path){
		File outputFile = new File(path); 
		if(outputFile.exists())
			return outputFile.delete();
		return false;
	}
	
	public ArrayList<PasswdResource> readExternalUserPasswords(String path){
		ArrayList<PasswdResource> passwords = null;
		
//		String filename = username + LIST_NAME + FORMATS[format];
//		String filepath = SD_DIRECTORY + username + "/" + filename;
		
		String data = read(null, path, FILE_EXTERNAL);
		if(data == null)
			return null;
		
		int format = -1;
		
		String[] parts = path.split("\\.");
		String ext = "." + parts[parts.length-1];
		int max = FORMATS.length;
		for(int i = 0; i < max; i++){
			if(FORMATS[i].equals(ext)){
				format = i;
				break;
			}
		}
		
		switch(format){
		case XML:
			passwords = XMLManager.loadFile(data);
			break;
		case JSON:
			passwords = JSONManager.loadFile(data);
			break;
		case TEX:
			passwords = TEXManager.loadFile(data);
			break;
		case CSV:
			passwords = CSVManager.loadFile(data);
			break;
		default:
			break;
		}
		
		return passwords;
	}
	
	private String read(Context context, String filepath, boolean external){
		try{
			DataInputStream dis;
			FileInputStream fIn = null;
			
			if (!external){
				fIn = context.openFileInput (filepath);
				BufferedInputStream bis = new BufferedInputStream(fIn);  
				dis = new DataInputStream(bis);
			}else{			
				File inputFile = new File(filepath); 
				
				BufferedInputStream bis = new BufferedInputStream( new FileInputStream(inputFile)); 
				dis = new DataInputStream(bis);				
			}
			
			String data = "";
			String line;
			while ( (line = dis.readLine()) != null ) {  
				data += line + "\n";
			}    
			
			if(!external)
				fIn.close();
            
			return data;
		} catch (FileNotFoundException e) {
			Log.e("FileManager", "", e);
		} catch (IOException e) {
			Log.e("FileManager", "", e);
		}
		
		return null;
	}
	
	public ArrayList<File> listExternalDir(String username){
		String path = SD_DIRECTORY + username + "/";
		File dir = new File(path);

    	if(!dir.exists() || !dir.isDirectory())
    		return null;
    	
    	ArrayList<File> files = new ArrayList<File>();
    	File[] ls = dir.listFiles();
    	int length = ls.length;
    	for (int i = 0; i < length; i++){
    		File file = ls[i];
    		String[] parts = file.getAbsolutePath().split("\\.");
    		String ext = "." + parts[parts.length-1];
    		int max = FORMATS.length;
    		for(int j = 0; j < max; j++)
    			if(FORMATS[j].equals(ext))
    				files.add(file);
    	}
    	
    	return files;
	}
	
}