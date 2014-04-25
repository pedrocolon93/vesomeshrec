package com.example.vesomeshrecorder;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class RecordingListFragment extends SherlockListFragment {
	private String uploadAddress = "http://10.1.1.5/vesomesh/upload.php";
	private File directory = new File(Environment.getExternalStorageDirectory().toString()+"/recordings");
	private ArrayList<String> filelist = new ArrayList<String>();
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Toast.makeText(getActivity(), "Uploading", Toast.LENGTH_LONG).show();

				String filename = filelist.get(arg2);
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
					Toast.makeText(RecordingListFragment.this.getActivity(), "Could not find file.", Toast.LENGTH_LONG).show();
					return false;
					//TODO handle errror
				}
				else{
					//Upload File
					ProgressBar progress = new ProgressBar(RecordingListFragment.this.getActivity(), null, android.R.attr.progressBarStyleSmall);

					Ion.with(RecordingListFragment.this.getActivity(), uploadAddress)
					.uploadProgressBar(progress)
					.setMultipartFile(filename+".wav", selection)
					.asJsonObject()
					.setCallback(new FutureCallback<JsonObject>() {
						@Override
						public void onCompleted(Exception arg0,
								JsonObject arg1) {
							if(arg0!=null){
								Toast.makeText(RecordingListFragment.this.getActivity(), "Problem with upload"+arg0.toString(), Toast.LENGTH_LONG).show();
								return;
							}
							Toast.makeText(RecordingListFragment.this.getActivity(), "Done", Toast.LENGTH_LONG).show();


						}
					});
				}

				return true;
			}
		});		filelist.add("None");
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
