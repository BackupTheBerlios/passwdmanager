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

import com.passwdmanager.PasswdResource;

public class CSVManager{
	
	private static final String FIELD_DELIMITER = ";";
	private static final String STRING_DELIMITER = "\"";
	
	public static String makeFile(String username, ArrayList<PasswdResource> passwords){
		String data = "";
		data += STRING_DELIMITER + "Site" + STRING_DELIMITER + FIELD_DELIMITER;
		data += STRING_DELIMITER + "Username" + STRING_DELIMITER + FIELD_DELIMITER;
		data += STRING_DELIMITER + "Password" + STRING_DELIMITER + FIELD_DELIMITER;
		data += STRING_DELIMITER + "Note" + STRING_DELIMITER + "\n";
		
		int max = passwords.size();
		for(int i = 0; i < max; i++){
			PasswdResource pr = passwords.get(i);
			data += STRING_DELIMITER + pr.getSite() + STRING_DELIMITER + FIELD_DELIMITER;
			data += STRING_DELIMITER + pr.getName() + STRING_DELIMITER + FIELD_DELIMITER;
			data += STRING_DELIMITER + pr.getPassword() + STRING_DELIMITER;
			if(pr.getNote() != null)
				data += FIELD_DELIMITER + STRING_DELIMITER + pr.getNote() + STRING_DELIMITER;
			data += "\n";
		}
		
		return data;
	}
	
	public static ArrayList<PasswdResource> loadFile(String file){
		if(file == null)
			return null;
		
		String[] lines = file.split("\n");
		int max = lines.length;
		if(max < 1)
			return null;
		
		ArrayList<PasswdResource> passwords = new ArrayList<PasswdResource>();
		for(int i = 1; i < max; i++){
			PasswdResource pr = new PasswdResource();
			String[] parts = lines[i].split(FIELD_DELIMITER);
			
			pr.setSite(parts[0].split(STRING_DELIMITER)[1]);
			pr.setName(parts[1].split(STRING_DELIMITER)[1]);
			pr.setPassword(parts[2].split(STRING_DELIMITER)[1]);
			if(parts.length == 4)
				pr.setNote(parts[3].split(STRING_DELIMITER)[1]);
			passwords.add(pr);
		}
		
		return passwords;
	}
	
}