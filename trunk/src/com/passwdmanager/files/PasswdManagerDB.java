package com.passwdmanager.files;

import java.util.ArrayList;

import com.passwdmanager.PasswdResource;
import com.passwdmanager.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PasswdManagerDB{

	private static final String DATABASE_NAME = "passwdmanager";

	private static final String USERS_TABLE = "users";
	private static final String NAME_COL = "name";
	private static final String PASSWD_COL = "passwd";
	private static final String CREATE_TABLE_USERS = "create table " + USERS_TABLE + " (" + NAME_COL + " text primary key, " + PASSWD_COL + " text not null);";


	private static final String PASSWDS_TABLE = "_passwds";
	private static final String SITE_COL = "site";
	private static final String CREATE_TABLE = "create table ";
	private static final String CREATE_TABLE_PASSWDS1 =	" (" + SITE_COL + " text primary key, " 
	+ NAME_COL + " text not null, " 
	+ PASSWD_COL + " text not null);";

	private SQLiteDatabase db;

	private static PasswdManagerDB passwdDB = null;

	public synchronized static PasswdManagerDB getInstance(Context context){
		if(passwdDB == null)
			passwdDB = new PasswdManagerDB(context);
		return passwdDB;
	}

	private PasswdManagerDB(Context ctx) {
		try {
			db = ctx.openOrCreateDatabase(DATABASE_NAME, 0, null);
			db.execSQL(CREATE_TABLE_USERS);
		} catch (Exception e) {
			Log.e("PasswdManagerDB", "", e);
		}
	}

	/* Managing users */

	public boolean createUser(User user){
		ContentValues values = new ContentValues();
		values.put(NAME_COL, user.getUsername());
		values.put(PASSWD_COL, user.getPassword());
		boolean res = (db.insert(USERS_TABLE, null, values) >= 0);
		try{
			if(res)
				db.execSQL(CREATE_TABLE + user.getUsername() + PASSWDS_TABLE + CREATE_TABLE_PASSWDS1);
		}catch(Exception e){
			Log.e("PasswdManagerDB", "", e);
		}

		return res;
	}

	public boolean checkUser(User user){
		if(user == null)
			return false;

		User stored = getUser(user.getUsername());
		if(stored == null)
			return false;

		return stored.getPassword().equals(user.getPassword());
	}

	public User getUser(String name){
		if(name == null)
			return null;
		try{
			Cursor c = db.query(USERS_TABLE, new String[] { NAME_COL, PASSWD_COL }, 
					NAME_COL + "=\"" + name + "\"", null, null, null, null);
			c.moveToFirst();
			User user = new User();
			user.setUsername(c.getString(0));
			user.setPassword(c.getString(1));
			c.close();

			return user;
		}catch(Exception e){
			Log.e("PasswdManagerDB", "", e);
		}
		return null;
	}

	public boolean deleteUser(String name) {
		try{
			db.execSQL("DROP TABLE IF EXISTS " + name + PASSWDS_TABLE);
		}catch(Exception e){
			Log.e("PasswdManagerDB", "", e);
			return false;
		}
		return (db.delete(USERS_TABLE, NAME_COL + "=\"" + name + "\"", null) > 0);
	}


	/* Managing passwords */

	public boolean insertPasswd(User user, PasswdResource passwdResource) {
		if(user==null || passwdResource == null)
			return false;

		ContentValues values = new ContentValues();
		values.put(SITE_COL, passwdResource.getSite());
		values.put(NAME_COL, passwdResource.getName());
		values.put(PASSWD_COL, passwdResource.getPassword());
		return (db.insert(user.getUsername() + PASSWDS_TABLE, null, values) >= 0);
	}
	
	public boolean updatePasswd(User user, PasswdResource passwdResource) {
		if(user==null || passwdResource == null)
			return false;

		ContentValues values = new ContentValues();
		values.put(NAME_COL, passwdResource.getName());
		values.put(PASSWD_COL, passwdResource.getPassword());
		return (db.update(user.getUsername() + PASSWDS_TABLE, values, SITE_COL + "=\"" + passwdResource.getSite() + "\"", null) > 0);
	}

	public boolean deletePasswd(User user, PasswdResource passwdResource) {
		if(user==null || passwdResource == null)
			return false;
		
		return (db.delete(user.getUsername() + PASSWDS_TABLE, SITE_COL + "=\"" + passwdResource.getSite() + "\"", null) > 0);
	}
	
	public boolean deleteAllPasswd(User user) {
		if(user==null)
			return false;
		
		return (db.delete(user.getUsername() + PASSWDS_TABLE, null, null) > 0);
	}

	public ArrayList<PasswdResource> getPasswordsList(User user, String site_pattern, String username_pattern){
		ArrayList<PasswdResource> passwd_list = new ArrayList<PasswdResource>();
		
		String where = null;
		if((site_pattern != null) && (!site_pattern.equals("")))
			where = SITE_COL + " like '%" + site_pattern + "%'";
		if((username_pattern != null) && (!username_pattern.equals(""))){
			if(where == null)
				where = "";
			else
				where += " AND ";
			where += NAME_COL + " like '%" + username_pattern + "%'";
		}
		
		try{
			Cursor c = db.query(user.getUsername() + PASSWDS_TABLE, new String[] { SITE_COL, NAME_COL, PASSWD_COL }, 
					where, null, null, null, "lower(" + SITE_COL + ") ASC");

			int numRows = c.getCount();
			c.moveToFirst();
			for(int i = 0; i<numRows; i++){
				PasswdResource pd = new PasswdResource();
				pd.setSite(c.getString(0));
				pd.setName(c.getString(1));
				pd.setPassword(c.getString(2));
				passwd_list.add(pd);
				c.moveToNext();
			}
			c.close();
		}catch(Exception e){
			Log.e("PasswdManagerDB", "", e);
		}

		return passwd_list;
	}








	//	
	//	
	//	public boolean insertFav(String theater_name, String url) {
	//		ContentValues values = new ContentValues();
	//		values.put(NAME_COL, theater_name);
	//		values.put(PASSWD_COL, url);
	//		return (db.insert(FAVS_TABLE, null, values) > 0);
	//	}
	//
	//	public boolean deleteFav(String theater_name) {
	//		return (db.delete(FAVS_TABLE, NAME_COL + "=\"" + theater_name + "\"", null) > 0);
	//	}
	//
	//	public boolean deleteAllFavs() {
	//		return (db.delete(FAVS_TABLE, null, null) > 0);
	//	}
	//
	//	public PasswdResource getPasswdResource(String theater_name){
	//		PasswdResource passwd = new PasswdResource();
	//
	//		try{
	//			Cursor c = db.query(FAVS_TABLE, new String[] { NAME_COL, PASSWD_COL }, 
	//					NAME_COL + "=\"" + theater_name + "\"", null, null, null, null);
	//			c.moveToFirst();
	//			passwd.setSite(c.getString(0));
	//			passwd.setName(c.getString(1));
	//			passwd.setPassword(c.getString(2));
	//			c.close();
	//		}catch(Exception e){
	//			Log.e("PasswdManagerDB", "", e);
	//			return null;
	//		}
	//
	//		return passwd;
	//	}
	//
	//	public ArrayList<Theater> getFavList(){
	//		ArrayList<Theater> theater_list = new ArrayList<Theater>();
	//
	//		try{
	//			Cursor c = db.query(FAVS_TABLE, new String[] { NAME_COL, PASSWD_COL }, 
	//					null, null, null, null, NAME_COL + " ASC");
	//
	//			int numRows = c.getCount();
	//			c.moveToFirst();
	//			for(int i = 0; i<numRows; i++){
	//				Theater theater = new Theater();
	//				theater.setName(c.getString(0));
	//				theater.setUrl(c.getString(1));
	//				theater_list.add(theater);
	//				c.moveToNext();
	//			}
	//			c.close();
	//		}catch(Exception e){
	//			Log.e("PasswdManagerDB", "", e);
	//		}
	//
	//		return theater_list;
	//	}
	//
	//	public boolean isEmpty(){
	//		try{
	//			Cursor c = db.query(FAVS_TABLE, new String[] { NAME_COL, PASSWD_COL }, 
	//					null, null, null, null, null);
	//
	//			int numRows = c.getCount();
	//			c.close();
	//			
	//			if(numRows > 0)
	//				return false;
	//		}catch(Exception e){
	//			Log.e("PasswdManagerDB", "", e);
	//		}
	//
	//		return true;
	//	}
	//	
	//	public void clearAll(){
	//		if(db != null)
	//			db.close();
	//		passwdDB = null;
	//	}

}