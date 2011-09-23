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

public class XMLManager{
	
	private static final String HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	private static final String INIT = "<PasswdManager>\n";
	private static final String CLOSE_INIT = "</PasswdManager>";
	private static final String SITE = "\t\t<site>\n";
	private static final String CLOSE_SITE = "\t\t</site>\n";
	private static final String SITE_NAME = "\t\t\t<sitename>";
	private static final String CLOSE_SITE_NAME = "</sitename>\n";
	private static final String NAME = "\t\t\t<name>";
	private static final String CLOSE_NAME = "</name>\n";
	private static final String PWD = "\t\t\t<password>";
	private static final String CLOSE_PWD = "</password>\n";
	private static final String NOTE = "\t\t\t<note>";
	private static final String CLOSE_NOTE = "</note>\n";
	
	public static String makeFile(String username, ArrayList<PasswdResource> passwords){
		String data = "";
		data += HEAD;
		data += INIT;
		data += "\t<" + username + ">\n";
		
		int max = passwords.size();
		for(int i = 0; i < max; i++){
			PasswdResource pr = passwords.get(i);
			data += SITE;
			data += SITE_NAME + pr.getSite() + CLOSE_SITE_NAME;
			data += NAME + pr.getName() + CLOSE_NAME;
			data += PWD + pr.getPassword() + CLOSE_PWD;
			if(pr.getNote() != null)
				data += NOTE + pr.getNote() + CLOSE_NOTE;
			data += CLOSE_SITE;
		}
		
		data += "\t</" + username + ">\n";
		data += CLOSE_INIT;
		
		return data;
	}
	
	public static ArrayList<PasswdResource> loadFile(String file){
		if(file == null)
			return null;

		String[] blocks = file.split(SITE);
		int max = blocks.length;
		if(max < 1)
			return null;

		ArrayList<PasswdResource> passwords = new ArrayList<PasswdResource>();
		for(int i = 1; i < max; i++){
			PasswdResource pr = new PasswdResource();

			pr.setSite(blocks[i].substring(blocks[i].indexOf(SITE_NAME) + SITE_NAME.length(), 
					blocks[i].indexOf(CLOSE_SITE_NAME)));
			pr.setName(blocks[i].substring(blocks[i].indexOf(NAME) + NAME.length(), 
					blocks[i].indexOf(CLOSE_NAME)));
			pr.setPassword(blocks[i].substring(blocks[i].indexOf(PWD) + PWD.length(), 
					blocks[i].indexOf(CLOSE_PWD)));
			if((blocks[i].indexOf(NOTE) < 0) || (blocks[i].indexOf(CLOSE_NOTE) < 0))
				pr.setNote(blocks[i].substring(blocks[i].indexOf(NOTE) + NOTE.length(), 
						blocks[i].indexOf(CLOSE_NOTE)));
			passwords.add(pr);
		}
		
		return passwords;
	}
	
}