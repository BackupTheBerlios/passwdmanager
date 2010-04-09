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

package com.passwdmanager.utils;


public class Validation{
	public static final String PATTERN= "[^A-Za-z0-9_\\-\\.@]";
	public static final String PATTERN_SITE= "[^A-Za-z0-9_\\-\\.@ ]";
	
	public static boolean validate(String string, String pattern){

		if((string == null) || string.equals(""))
			return false;

//		if(string.contains(" "))
//			return false;

		boolean res = true;
		int max = string.length();
		for(int i = 0; i < max ; i++){
			String piece = string.charAt(i) + "";
			if(piece.matches(pattern)){
				res = false;
				break;
			}
		}
		return res;
	}
	
}