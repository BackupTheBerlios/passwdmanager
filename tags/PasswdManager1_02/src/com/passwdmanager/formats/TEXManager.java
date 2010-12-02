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

public class TEXManager{
	
	private static final String FIELD_DELIMITER = " & ";
	private static final String LINE_DELIMITER = "\\\\ \\hline\n";
	private static final String BEGIN_CENTER = "\\begin{center}\n";
	private static final String END_CENTER = "\\end{center}\n";
	private static final String BEGIN_TABULAR = "\\begin{tabular}{|c|c|c|}\\hline\n";
	private static final String END_TABULAR = "\\end{tabular}\n";
	private static final String HEADER = "\\textbf{Site} & \\textbf{Usename} & \\textbf{Password} \\\\ \\hline\n";
	
	public static String makeFile(String username, ArrayList<PasswdResource> passwords){
		String data = "";
		data += BEGIN_CENTER;
		data += BEGIN_TABULAR;
		data += HEADER;
		
		int max = passwords.size();
		for(int i = 0; i < max; i++){
			PasswdResource pr = passwords.get(i);
			data += pr.getSite() + FIELD_DELIMITER + pr.getName() + FIELD_DELIMITER + pr.getPassword() + LINE_DELIMITER;
		}
		
		data += END_TABULAR;
		data += END_CENTER;
		
		return data;
	}
	
	public static ArrayList<PasswdResource> loadFile(String file){
		if(file == null)
			return null;
		
		String[] lines = file.substring(file.indexOf(HEADER) + HEADER.length(), 
				file.indexOf(END_TABULAR)).replace(LINE_DELIMITER, "\n").split("\n");
		int max = lines.length;
		if(max < 0)
			return null;
		
		ArrayList<PasswdResource> passwords = new ArrayList<PasswdResource>();
		for(int i = 0; i < max; i++){
			PasswdResource pr = new PasswdResource();
			String[] parts = lines[i].split(FIELD_DELIMITER);
			
			pr.setSite(parts[0]);
			pr.setName(parts[1]);
			pr.setPassword(parts[2]);
			passwords.add(pr);
		}
		
		return passwords;
	}
	
}