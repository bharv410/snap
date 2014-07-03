package com.kidgeniusdesigns.snapapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.habosa.javasnap.Snapchat;
import com.habosa.javasnap.Story;

public class MainActivity extends Activity {
	GridView gridView;
	Uri currImageURI;
	String realPath, un = "boutmabenjamins", pw = "Nosm0kin";
	boolean sentOrNah;
	ProgressDialog progressDialog;
	private ArrayList<String> shords;
	
	 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SnapData.ctx = this.getApplicationContext();
		startProgressDialog();
		startNotifs();
		AsyncTaskRunner runner = new AsyncTaskRunner();
		runner.execute();
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				SnapData.currentByte = SnapData.byteList.get(position);
				if(SnapData.currentByte.length>500000){
					SnapData.imageOrNah=false;
				}else{
					SnapData.imageOrNah=true;
				}
				startActivity(new Intent(getApplicationContext(), BigView.class));
			}
		});
	}
	public void startNotifs(){		
		Calendar cal = Calendar.getInstance();
		Intent intent = new Intent(this, MyService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 24*60*60*1000, pintent);
	}

	public void startProgressDialog() {
		shords = new ArrayList<String>();
		shords.add("amber_rubino");
		shords.add("briannnax");
		shords.add("brandymillerr");
		shords.add("sophiaxoxorose");
		shords.add("emileesawyer69");
		shords.add("gillianhill");
		shords.add("crystaleee");
		shords.add("kclear24");
		shords.add("lacylaplantee");
		shords.add("itslindsanity");
		shords.add("lnsylove14");
		shords.add("sarrahmarie44");
		shords.add("menacedennis");
		shords.add("choiboiiiii");
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Loading");
		progressDialog.setMessage("Fetching snaps..");
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
	}

	public void getImageUri(View v) {
		// To open up a gallery browser
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				// currImageURI is the global variable I'm using to hold the
				// content:// URI of the image
				currImageURI = data.getData();
				InputStream iStream;
				try {
					iStream = getContentResolver()
							.openInputStream(currImageURI);
					byte[] inputData = getBytes(iStream);
					String filename = "image";
					FileOutputStream outputStream = openFileOutput(filename,
							Context.MODE_PRIVATE);
					outputStream.write(inputData);
					outputStream.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				UploadSnap upl = new UploadSnap();
				upl.execute("", "");
			}
		}
	}

	private class MyAdapter extends BaseAdapter {
		private List<String> cps = new ArrayList<String>();
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			inflater = LayoutInflater.from(context);
			for (Story s : SnapData.myStorys) {
				if ((shords.contains(s.getSender()))) {
					System.out.println("MyAdapter(Context context)"
							+ s.getSender());
					cps.add(s.getSender() + "-" + s.getCaption());
				}
			}
		}

		@Override
		public int getCount() {
			return cps.size();
		}

		@Override
		public Object getItem(int i) {
			return cps.get(i);
		}

		@Override
		public long getItemId(int i) {
			return (long) (cps.get(i).hashCode());
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			View v = view;
			ImageView picture;
			TextView name;

			if (v == null) {
				v = inflater.inflate(R.layout.gridview_item, viewGroup, false);
				v.setTag(R.id.picture, v.findViewById(R.id.picture));
				v.setTag(R.id.text, v.findViewById(R.id.text));
			}
			picture = (ImageView) v.getTag(R.id.picture);
			name = (TextView) v.getTag(R.id.text);

			byte[] storyBytes = SnapData.byteList.get(i);
			picture.setImageBitmap(BitmapFactory.decodeByteArray(storyBytes, 0,
					storyBytes.length));
			name.setText(cps.get(i));

			return v;
		}
	}

	private class AsyncTaskRunner extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... strings) {
			JSONObject loginObj = Snapchat.login(un, pw);
			try {
				// get authentication token
				if (loginObj != null) {
					SnapData.authTokenSaved = loginObj
							.getString(Snapchat.AUTH_TOKEN_KEY);
					// get friends list
					SnapData.myFriends = Snapchat.getFriends(loginObj);
					// get storys
					Story[] notdownloadable = Snapchat.getStories(un,
							SnapData.authTokenSaved);
					SnapData.myStorys = Story
							.filterDownloadable(notdownloadable);
					SnapData.byteList = new ArrayList<byte[]>();
					int count=0;
					for (Story s : SnapData.myStorys) {
						if ((shords.contains(s.getSender()))) {
							count++;
							byte[] storyBytes = Snapchat.getStory(s, un,
									SnapData.authTokenSaved);
							System.out.println("bytes length:"
									+ storyBytes.length);
							SnapData.byteList.add(storyBytes);
							publishProgress(count);
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
	    public void onProgressUpdate(Integer... args){
	                    progressDialog.setProgress(args[0]);
	                }
		@Override
		protected void onPostExecute(String result) {
			if (progressDialog != null)
				progressDialog.dismiss();
			gridView.setAdapter(new MyAdapter(getApplicationContext()));
			Toast.makeText(getApplicationContext(), "DONE", Toast.LENGTH_LONG)
					.show();
		}
	}

	public void nextTwenty(View View) {
		AsyncTaskRunner runner = new AsyncTaskRunner();
		runner.execute(un, pw);
	}

	// And to convert the image URI to the direct file system path of the image
	// file
	public String getRealPathFromURI(Uri contentUri) {

		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	public byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		return byteBuffer.toByteArray();
	}

	private class UploadSnap extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				boolean video = false;
				File cur = new File(getFilesDir() + "/image");
				String mediaId = Snapchat.upload(cur, un,
						SnapData.authTokenSaved, video);
				int viewTime = 4; // seconds
				String caption = "My Story"; // This is only shown in the story
												// list, not on the actual story
												// photo/video.
				sentOrNah = Snapchat.sendStory(mediaId, viewTime, video,
						caption, un, SnapData.authTokenSaved);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "fnf",
						Toast.LENGTH_LONG).show();
			}
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			// Toast.makeText(getApplicationContext(),
			// "ff"+sentOrNah+SnapData.authTokenSaved,
			// Toast.LENGTH_LONG).show();
			if (sentOrNah)
				Toast.makeText(getApplicationContext(),
						"Succesfully Uploaded to Story", Toast.LENGTH_SHORT)
						.show();
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}