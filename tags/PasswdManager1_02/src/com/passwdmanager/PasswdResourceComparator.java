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

import java.util.Comparator;

public class PasswdResourceComparator implements Comparator<PasswdResource>{

	public int compare(PasswdResource res1, PasswdResource res2) {
		String pwd1 = res1.getSite().toUpperCase();
		String pwd2 = res2.getSite().toUpperCase();
		
		return pwd1.compareTo(pwd2);
	}
	
	
}