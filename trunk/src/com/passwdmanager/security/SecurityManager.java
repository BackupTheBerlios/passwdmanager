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

package com.passwdmanager.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public class SecurityManager{
	
	private static final String HEX = "0123456789ABCDEF";
	
	public static String encodePassword(String pwd){
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(pwd.getBytes("iso-8859-1"), 0, pwd.length());
			
			return convertByteToHex(md.digest());
		} catch (NoSuchAlgorithmException e) {
			Log.e("SecurityManager", "", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			Log.e("SecurityManager", "", e);
			return null;
		}
	}
	
	private static String convertByteToHex(byte[] bytes){
		
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for(byte b : bytes){
			sb.append(HEX.charAt((b & 0xf0) >> 4));
			sb.append(HEX.charAt((b & 0x0f)));
		}
		return sb.toString();
	}
	
	private static byte[] convertHexToByte(String hex){
		int max = hex.length();
		byte[] b = new byte[max/2];
		
		int i = 0;
		while(i < max){
			int b2 = HEX.indexOf(hex.charAt(i)) << 4;
			int b3 = HEX.indexOf(hex.charAt(i+1));
			
			b[i/2] = (byte) (b2 + b3);
			i += 2;
		}
		return b;
	}
	
	public static String encodeXOR(String key1, String key2){
		byte[] b = encryptionXOR(key1, key2);
		if(b == null)
			return null;
		return convertByteToHex(b);
	}
	
	public static String decodeXOR(String key1, String key2){
		byte[] b1 = convertHexToByte(key1);
		byte[] b2 = encryptionXOR(b1, key2);
		
		try {
			String key = new String(b2, "iso-8859-1");
			return key;
		} catch (UnsupportedEncodingException e) {
			Log.e("SecurityManager", "", e);
		}
		return null;
	}
	
	private static byte[] encryptionXOR(Object key1, String key2){
		
		try {
			byte[] b1 = null;
			if(String.class.isInstance(key1))
				b1 = ((String)key1).getBytes("iso-8859-1");
			else if(byte[].class.isInstance(key1))
				b1 = (byte[]) key1;
			else
				return null;
			
			byte[] b2 = key2.getBytes("iso-8859-1");
			
			byte[] b = new byte[b1.length];
			
			int max1 = b1.length;
			int max2 = b2.length;
			
			for(int i = 0; i < max1; i++)
				b[i] = (byte) (b1[i] ^ b2[i % max2]);
			
			return b;
		} catch (UnsupportedEncodingException e) {
			Log.e("SecurityManager", "", e);
		}
		
		return null;
	}
	
}