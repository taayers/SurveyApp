package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.triadicsoftware.surveyapp.database.DataSourceAccessor;
import com.triadicsoftware.surveyapp.database.PhotoItems;
import com.triadicsoftware.surveyapp.database.Survey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PictureActivity extends Activity implements OnClickListener{

	public static final int ACTION_CODE = 9919;
	private ArrayList<PhotoItems> mPhotos = new ArrayList<PhotoItems>();
	private PhotoItems photoItem = new PhotoItems();
	private ImageAdapter mAdapter;
	private Survey mSurvey;
	private DataSourceAccessor mDsa;
	private LocationManager mLocManager;
	private Location loc = null;
	private int site;
	LocationListener mlocListener;
	Button continueButton;
	TextView hintText;
	TextView titleText;
	GridView grid;

	private String mFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_pictures);

		mSurvey = Survey.getInstance();

		site = mSurvey.getTopicId();

		hintText = (TextView)findViewById(R.id.hint_text);
		titleText = (TextView)findViewById(R.id.activity_title_text);

		titleText.setText("Survey Site " + site);

		PackageManager pm = getPackageManager();
		boolean hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mlocListener = new MyLocationListener();

		mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);

		Button takePictureButton = (Button) findViewById(R.id.take_picture_bt);
		takePictureButton.setOnClickListener(this);

		continueButton = (Button) findViewById(R.id.continue_bt);
		continueButton.setOnClickListener(this);

		mAdapter = new ImageAdapter(getLayoutInflater());

		grid = (GridView)findViewById(R.id.grid_view);
		grid.setAdapter(mAdapter);

		if(mPhotos != null && mPhotos.size() > 0){
			hintText.setVisibility(View.GONE);
			grid.setVisibility(View.VISIBLE);
		}

		mDsa = new DataSourceAccessor(this);


		if (hasGps) {
			loc = mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}

	}

	public void onStart(){
		mDsa.open();
		mFile = new String();
		super.onStart();

	}

	public void onStop(){
		super.onStop();
		mDsa.close();
	}

	public void onDestroy(){
		mLocManager.removeUpdates(mlocListener);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mLocManager.removeUpdates(mlocListener);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
	}

	public void goBack(View v){

		super.onBackPressed();
	}


	@Override
	public void onClick(View button) {

		switch(button.getId()){


		case (R.id.take_picture_bt):
			hintText.setVisibility(View.INVISIBLE);
			grid.setVisibility(View.VISIBLE);
			dispatchTakePictureIntent(ACTION_CODE);
		break;

		case (R.id.continue_bt):

			mSurvey.setPictureLocations(mPhotos);

			Intent nextActIntent = new Intent(PictureActivity.this, QuestionsActivity.class);
			startActivity(nextActIntent);
		break;

		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);

	  if(mFile != null){
			  savedInstanceState.putString("FILE_URL", (mFile));
			  savedInstanceState.putSerializable("PHOTO_ARRAY", mPhotos);
			  savedInstanceState.putSerializable("SURVEY", mSurvey);
	  }

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mFile = savedInstanceState.getString("FILE_URL");
		mPhotos = (ArrayList<PhotoItems>) savedInstanceState.getSerializable("PHOTO_ARRAY");
		mSurvey = (Survey)savedInstanceState.getSerializable("SURVEY");
		if(mSurvey != null){
			Survey.setInstance(mSurvey);
		}

		if(mPhotos != null && mPhotos.size() > 0){
			hintText.setVisibility(View.GONE);
			grid.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ACTION_CODE && resultCode == Activity.RESULT_OK){
			rotatePhotoIfNeeded();
			photoItem.setPhotoPath(mFile);
			if (loc != null) {
				photoItem.setLatitude(loc.getLatitude() + "");
				photoItem.setLongitude(loc.getLongitude() + "");
			}
			mPhotos.add(photoItem);
			reloadGrid();
		}
		else if(requestCode == MainActivity.SURVEY_COMPLETE_RESULT && resultCode == RESULT_OK){
			setResult(RESULT_OK);
			finish();
		}
	}

	private void reloadGrid(){
		mAdapter.notifyDataSetChanged();
	}

	private void dispatchTakePictureIntent(int actionCode) {
		if(isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE)){
			mFile = getFileName();
		    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    	takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mFile)));
		    startActivityForResult(takePictureIntent, actionCode);
		}
	}

	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}

	private String getFileName(){
		Date date = new Date();
		String retFileName = Environment.getExternalStorageDirectory() + "/Survey/" + new SimpleDateFormat("yyyy-dd-MM-HH-mm-ss-SS", Locale.US).format(date) + "_survey.jpg";

		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			File direct = new File(Environment.getExternalStorageDirectory() + "/Survey");

			if(!direct.exists()){
				boolean valid = direct.mkdir();
				Toast.makeText(PictureActivity.this, "" + valid, Toast.LENGTH_LONG).show();
			}

			return retFileName;
		}
		return null;
	}

	private void rotatePhotoIfNeeded(){
		try {
			ExifInterface exif = new ExifInterface(mFile);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

			float degreeToRotate = 0;
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degreeToRotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degreeToRotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degreeToRotate = -90;
				break;
			default:
				break;
			}
			exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;

			Bitmap bmp = BitmapFactory.decodeFile(mFile, options);
			Matrix rotateMatrix = new Matrix();
			rotateMatrix.postRotate(degreeToRotate);
			if(bmp != null){
				Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), rotateMatrix, false);

				OutputStream fOut = new FileOutputStream(mFile);
				rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

				fOut.flush();
				fOut.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class ImageAdapter extends BaseAdapter{

		private LayoutInflater mInflater;

		public ImageAdapter(LayoutInflater inflater){
			mInflater = inflater;
		}

		@Override
		public int getCount() {
			return mPhotos.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.picture_cell, null);
			}

			AQuery aq = new AQuery(convertView);
			aq.id(R.id.picture_image).image(new File(mPhotos.get(position).getPhotoPath()), 100);


			return convertView;
		}

	}

	private final class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			mSurvey.setLatitude("" + location.getLatitude());
			mSurvey.setLongitude("" + location.getLongitude());
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}


	}

}
