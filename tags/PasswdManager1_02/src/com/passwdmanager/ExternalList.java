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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.passwdmanager.files.FileManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ExternalList extends ListActivity{


	private static final int MENU_SEND = Menu.FIRST + 1;
	private static final int MENU_DELETE = Menu.FIRST + 2;
	
	private static final int DIALOG_PBAR = 0;
	private static final int DIALOG_IMPORT = 1;
	private static final int DIALOG_DELETE = 2;
	
	private static ArrayList<File> files = null;
	private static User user;
	
	private FileListAdapter mAdapter = null;
	private static int fileClicked = -1;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			removeDialog(DIALOG_PBAR);
			if(msg.what == 0)
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.ok), 
						Toast.LENGTH_SHORT).show();
			else if(msg.what == 1)
				setFileAdapter();
			else
				Toast.makeText(getBaseContext(), 
						getResources().getString(R.string.error), 
						Toast.LENGTH_SHORT).show();
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		registerForContextMenu(this.getListView());
		
		getListView().setBackgroundResource(R.drawable.background);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		getListView().setDivider(null);
		
        if (files == null){
        	user = (User)getIntent().getSerializableExtra("USER");
        	showDialog(DIALOG_PBAR);
        	new Thread(){
        		public void run(){
        			files = FileManager.getInstance().listExternalDir(user.getUsername());
        			mHandler.sendEmptyMessage(1);
        		}
        	}.start();
		}else{
			setFileAdapter();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if ((files != null) && (keyCode == KeyEvent.KEYCODE_BACK))
			files = null;

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,  ContextMenuInfo menuInfo){ 
		super.onCreateContextMenu(menu, view, menuInfo);
		
		menu.add(0, MENU_DELETE, 0 , R.string.list_delete);
		menu.add(0, MENU_SEND, 0 , R.string.list_send);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
		.getMenuInfo();
		
		switch (item.getItemId()) {
		case MENU_DELETE:
			fileClicked = info.position;
			showDialog(DIALOG_DELETE);
			break;
			
		case MENU_SEND:
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
	        
			String filename = files.get(info.position).getAbsolutePath();
			String[] parts = filename.split("\\.");
			String format = parts[parts.length -1].toLowerCase();
			
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(files.get(info.position)));
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "PasswdManager");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "");
            sendIntent.setType("text/" + format);
            startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.select)));
			break;
		}
		return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		fileClicked = position;
		showDialog(DIALOG_IMPORT);
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {       
    	
    	switch (id) {
    	case DIALOG_PBAR:
    		ProgressDialog dialog = new ProgressDialog(this);
    		dialog.setMessage(getResources().getString(R.string.dialog_pbar));
    		dialog.setIndeterminate(true);
    		dialog.setCancelable(false);
    		return dialog;
    		
    	case DIALOG_DELETE:
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setIcon(R.drawable.icon)
    		.setTitle(getResources().getString(R.string.list_delete))
    		.setMessage(getResources().getString(R.string.dialog_delete))
    		.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_DELETE);
				}
			})
    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_DELETE);
					FileManager.getInstance().removeExternalFile(files.get(fileClicked).getAbsolutePath());
					files.remove(fileClicked);
					mAdapter.notifyDataSetChanged();
				}
			})
    		.create();
    		
    	case DIALOG_IMPORT:
    		return new AlertDialog.Builder(this)	      
    		.setCancelable(true)
    		.setIcon(R.drawable.icon)
    		.setTitle(getResources().getString(R.string.main_import))
    		.setMessage(getResources().getString(R.string.dialog_delete))
    		.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_IMPORT);
				}
			})
    		.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_IMPORT);
					showDialog(DIALOG_PBAR);
					
					new Thread(){
						public void run(){
							if(importPasswdList())
								mHandler.sendEmptyMessage(0);
							else
								mHandler.sendEmptyMessage(-1);
						}
					}.start();
				}
			})
    		.create();
    	}
    	return null;
	}
	
	private void setFileAdapter(){
		mAdapter = new FileListAdapter(getBaseContext());
		setListAdapter(mAdapter);
	}
	
	private boolean importPasswdList(){
		File file = files.get(fileClicked);
		ArrayList<PasswdResource> pwd_list = new ArrayList<PasswdResource>();
		
		ArrayList<PasswdResource> pwd_imported = FileManager.getInstance().readExternalUserPasswords(file.getAbsolutePath());
		if(pwd_imported == null)
			return false;
		
		ArrayList<PasswdResource> pwd_saved = FileManager.getInstance().readUserPasswords(getBaseContext(), user.getUsername());
		if((pwd_saved == null) || (pwd_saved.isEmpty())){
			pwd_list = pwd_imported;
		}else{
			int max1 = pwd_imported.size();
			int max2 = pwd_saved.size();
			for(int i = 0; i < max1; i++){
				PasswdResource pr1 = pwd_imported.get(i);
				boolean add = true;
				for(int j = 0; j < max2; j++){
					PasswdResource pr2 = pwd_saved.get(j);
					if(pr1.getSite().equals(pr2.getSite())){
						add = false;
						break;
					}
				}
				if(add)
					pwd_list.add(pr1);
			}
			pwd_list.addAll(pwd_saved);
		}
		
		PasswdResourceComparator comparator = new PasswdResourceComparator();
		Collections.sort(pwd_list, comparator);
		
		FileManager.getInstance().createUserPasswords(getBaseContext(), user.getUsername(), pwd_list);
		
		return true;
	}
	
	
	private static class FileListAdapter extends BaseAdapter {

		private Context mContext;

		public FileListAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			// TODO Auto-generated method stub
			if(files == null)
				return 0;
			return files.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view;

			if (convertView == null) {
				// Make up a new view
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.passwd_item, null);
			} else {
				// Use convertView if it is available
				view = convertView;
			}
			
			TextView tv_site = (TextView) view.findViewById(R.id.list_site);
			tv_site.setText(files.get(position).getName());
			
			return view;

		}

	}
	
}