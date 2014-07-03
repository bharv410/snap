package com.kidgeniusdesigns.snapapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.kidgeniusdesigns.snapapp.helpers.Utility;

public class BigView extends Activity {
	ImageView iv;
	File imageFileFolder, imageFileName;
	MediaScannerConnection msConn;
	FileOutputStream fileOutputStream;
	File file1;
	private MediaPlayer mediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_big_view);
		iv = (ImageView) findViewById(R.id.imageView1);
		Bitmap bm = Utility.getPhoto(SnapData.currentByte);
		iv.setImageBitmap(bm);
		if (SnapData.imageOrNah) {
			savePhoto(bm);
		} else {
			playVid(SnapData.currentByte);
		}
	}
	public void savePhoto(Bitmap bmp) {
		imageFileFolder = new File(Environment.getExternalStorageDirectory(),
				"Rotate");
		imageFileFolder.mkdir();
		FileOutputStream out = null;
		Calendar c = Calendar.getInstance();
		String date = fromInt(c.get(Calendar.MONTH))
				+ fromInt(c.get(Calendar.DAY_OF_MONTH))
				+ fromInt(c.get(Calendar.YEAR))
				+ fromInt(c.get(Calendar.HOUR_OF_DAY))
				+ fromInt(c.get(Calendar.MINUTE))
				+ fromInt(c.get(Calendar.SECOND));
		imageFileName = new File(imageFileFolder, date.toString() + ".jpg");
		try {
			out = new FileOutputStream(imageFileName);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			scanPhoto(imageFileName.toString());
			out = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String fromInt(int val) {
		return String.valueOf(val);
	}

	public void scanPhoto(final String imageFileName) {
		msConn = new MediaScannerConnection(BigView.this,
				new MediaScannerConnectionClient() {
					public void onMediaScannerConnected() {
						msConn.scanFile(imageFileName, null);
						Log.i("msClient obj  in Photo Utility",
								"connection established");
					}

					public void onScanCompleted(String path, Uri uri) {
						msConn.disconnect();
						Log.i("msClient obj in Photo Utility", "scan completed");
					}
				});
		msConn.connect();
		Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG)
				.show();
	}
	public void playVid(byte[] array) { 
		imageFileFolder = new File(Environment.getExternalStorageDirectory(),
				"Rotate");
		imageFileFolder.mkdir();
		imageFileName = new File(imageFileFolder, array.toString() + ".mp4");
	    try { 
	        FileOutputStream stream = new FileOutputStream(imageFileName); 
	        stream.write(array); 
	        stream.flush();
			stream.close();
			scanPhoto(imageFileName.toString());
			stream = null;
			Toast.makeText(getApplicationContext(), "Video", Toast.LENGTH_LONG)
			.show();
			mediaPlayer=new MediaPlayer();
			FileInputStream fileInputStream = new FileInputStream(imageFileName);
			mediaPlayer.setDataSource(fileInputStream.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
	    } catch (Exception e) { 
	        e.printStackTrace(); 
	    } 
	} 
}