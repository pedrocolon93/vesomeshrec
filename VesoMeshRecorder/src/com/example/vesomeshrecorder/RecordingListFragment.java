package com.example.vesomeshrecorder;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class RecordingListFragment extends SherlockListFragment {
	private File directory = new File(Environment.getExternalStorageDirectory().toString()+"/recordings");
	private ArrayList<String> filelist = new ArrayList<String>();
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		filelist.add("None");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, filelist);
		setListAdapter(adapter);
		updateList();

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String filename = filelist.get(position);
		File[] fl  = directory.listFiles();
		File selection = null;
		if(directory.exists()){
			for(File f: fl){
				if(f.getName().equals(filename)){
					selection = f;
				}
			}
		}
		if(selection==null){
			//TODO handle errror
		}
		else{
			//Play the file
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(selection.getAbsolutePath())); 
			String c = MimeTypeMap.getSingleton().getMimeTypeFromExtension("wav");
			intent.setDataAndType(Uri.fromFile(selection), MimeTypeMap.getSingleton().getMimeTypeFromExtension("wav"));  
			intent.setAction(android.content.Intent.ACTION_VIEW);  
			startActivity(intent);
		}
	}
	
	public void updateList(){
		if(directory.exists()){
			String [] filenames;
			filenames = directory.list();
			filelist.clear();
			for(String s :filenames){
				filelist.add(s);
			}
			if(filelist.size()==0){
				filelist.add("None");
			}
			((ArrayAdapter<String>)this.getListAdapter()).notifyDataSetChanged();
		}
	}
}
