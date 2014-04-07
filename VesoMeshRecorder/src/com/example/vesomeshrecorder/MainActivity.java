package com.example.vesomeshrecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class MainActivity extends SherlockFragmentActivity {
	private Button mRecButton, mStopButton;
	private MediaRecorder recorder;
	private boolean mBoolRecording;
	private FragmentManager fm;
	private RecordingListFragment rlf;

	private String outname;
	private int seconds;
	private long timeInMillis;
	private boolean recording, doneRec;
	private Thread recordingThread;
	private AudioRecord audioRecorder;
	private byte Data[];
	private int bufferSizeInBytes;
	private Handler handler = new Handler();
	private String filepath;
	private FileOutputStream os = null;
	private long finalTime;
	private long timeSwap;
	private long startTime=0l;
	private TextView timer;
	private boolean nameofoutput = false;

	public  void startRecording() {
		audioRecorder.startRecording();
		recordingThread = new Thread(new Runnable() {
			public  void run() {
				if(!recordingThread.isInterrupted()){
					if(os==null){
						try
						{
							os = new FileOutputStream(filepath+"/"+outname+".pcm");
						} catch (FileNotFoundException e) {
							Log.e("Problem creating buffer", e.toString());
						}
					}
					while(recording) {
						audioRecorder.read(Data, 0, Data.length);
						try 
						{
							if(os!=null)
								os.write(Data, 0, bufferSizeInBytes);
						} catch (IOException e) {
							Log.e("Problem writing os", e.toString());
						}
					}
				}
			}
		});
		recordingThread.start();
	}

	public synchronized void cleanup() {
		handler.removeCallbacks(updateTimerMethod);
		mRecButton.setText("Start");
		nameofoutput = false;
		
		recording=false;	
		if (null != audioRecorder) 
		{
			audioRecorder.stop();
		}
		finalTime = 0;
		int seconds = (int) (finalTime / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		int milliseconds = (int) (finalTime % 1000);
		
		timer.setText("" + minutes + ":"+ String.format("%02d", seconds) + ":"+ String.format("%03d", milliseconds));
		try {
			if(os!=null){
				os.close();
				os.flush();
			}

		} catch (IOException e) {
			Log.e("Problem closing buffer", e.toString());
		}

		os=null;
		try {
			PcmAudioHelper.convertRawToWav(recordFormat, new File(filepath+"/"+outname+".pcm"), new File(filepath+"/"+outname+".wav"));
			File outpcm = new File(filepath+"/"+outname+".pcm");
			outpcm.delete();
		} catch (IOException e1) {
			Log.e("Create wav", e1.toString());
		}
		rlf.updateList();


	}

	private Runnable updateTimerMethod = new Runnable() {
		public void run() {
			timeInMillis = SystemClock.uptimeMillis()-startTime;
			finalTime = timeSwap + timeInMillis;
			seconds = (int) (finalTime / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;
			int milliseconds = (int) (finalTime % 1000);


			timer.setText("" + minutes + ":"+ String.format("%02d", seconds) + ":"+ String.format("%03d", milliseconds));

			handler.postDelayed(this, 0);

		}
	};
	private WavAudioFormat recordFormat;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getSupportActionBar();
		setContentView(R.layout.activity_main_alt);
		
		filepath = Environment.getExternalStorageDirectory().toString()+"/recordings";
		File directory = new File(filepath);
		if(!directory.exists()){
			directory.mkdirs();
		}
		
		timer = (TextView) this.findViewById(R.id.TIMER);

		fm = getSupportFragmentManager();

		if(rlf == null){
			rlf = new RecordingListFragment();
			fm.beginTransaction().add(R.id.reclist,rlf).commit();
		}
		
		
		
		int audioSource = AudioSource.MIC;
		int sampleRateInHz = 8000;
		int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		recordFormat = WavAudioFormat.mono16Bit(sampleRateInHz);

		Data = new byte[bufferSizeInBytes];

		audioRecorder = new AudioRecord(audioSource,sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

		while(audioRecorder.getState()!=AudioRecord.STATE_INITIALIZED){
			audioRecorder = new AudioRecord(audioSource,sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
		}

		mRecButton = (Button) findViewById(R.id.startbutton);
		mRecButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!recording)
				{

					if(nameofoutput){
						startTime=SystemClock.uptimeMillis();
						handler.postDelayed(updateTimerMethod, 0);
						doneRec = false;
						recording=true;
						mRecButton.setText("Pause");
						startRecording();
					}
					else{
						AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

						alert.setTitle("Save as...");
						alert.setMessage("Please enter a name for enroll session:");

						// Set an EditText view to get user input 
						final EditText input = new EditText(MainActivity.this);
						alert.setView(input);

						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {



							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if(input.getText().toString().equals("")||input.getText().toString().matches("^\\s*$")){
									Toast.makeText(MainActivity.this, "Please write a valid name", Toast.LENGTH_LONG).show();;
								}
								else{
									outname = input.getText().toString();

									outname = outname.replaceAll("\\s+","_");
									outname = outname.replaceAll("\\.","");

									nameofoutput = true;
									startTime=SystemClock.uptimeMillis();
									handler.postDelayed(updateTimerMethod, 0);
									doneRec = false;
									recording=true;
									mRecButton.setText("Pause");
									startRecording();
								}

							}
						});
						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								// Canceled.
							}
						});

						alert.show();
					}
				}
				else
				{
					timeSwap += timeInMillis; 
					handler.removeCallbacks(updateTimerMethod);
					mRecButton.setText("Continue");
					recording=false;	
					if (null != audioRecorder) 
					{
						audioRecorder.stop();
					}

				}

			}
		});

		mStopButton = (Button) findViewById(R.id.stopbutton);
		mStopButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(recorder!=null){
					recorder.stop();
					recorder.release();
					recorder = null;
				}
				mRecButton.setText("Start");
				cleanup();

			}
		});


		

	}
	@Override
	public void onPause() {
		super.onPause();
		if (recorder != null) {
			if(mBoolRecording){
				recorder.stop();	
			}
			recorder.release();
			recorder = null;
		}
	}



}
