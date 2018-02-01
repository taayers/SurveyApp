package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.triadicsoftware.surveyapp.database.DataSourceAccessor;
import com.triadicsoftware.surveyapp.database.PhotoItems;
import com.triadicsoftware.surveyapp.database.Questions;
import com.triadicsoftware.surveyapp.database.Survey;
import com.triadicsoftware.surveyapp.database.SurveyData;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class UploadActivity extends Activity {
	private Survey mSurvey;
	private DataSourceAccessor mDsa;
	private ArrayList<PhotoItems> items;
	private String tempSurveyId;
	private String deviceID;
	private PhotoItems photoItem;
	private String question1Answers;
	private String question2Answers;
	private String noImagePath;
	private ArrayList sitesCompleted;
	private ArrayList<Survey> surveyList;
	private ConnectivityManager connectionManager;
	private long savedToDB;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);

		connectionManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

		mDsa = new DataSourceAccessor(this);
		mDsa.open();

		final SurveyData surveyData = (SurveyData)getApplicationContext();
		sitesCompleted = surveyData.getSitesCompleted();

		mSurvey = Survey.getInstance();
		photoItem = new PhotoItems();
		items = new ArrayList<PhotoItems>();

		final Button nextDistrictButton = (Button)findViewById(R.id.new_district_btn);
		final Button finishButton = (Button) findViewById(R.id.finish_button);

		sitesCompleted.add(mSurvey.getTopicId());
		surveyData.setSitesCompleted(sitesCompleted);
		if(sitesCompleted.size() >= 10){
			finishButton.setEnabled(false);
			nextDistrictButton.setVisibility(View.VISIBLE);
			nextDistrictButton.setEnabled(true);
		}

		finishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startNextSite = new Intent(UploadActivity.this, SiteActivity.class);
				startActivity(startNextSite);
			}
		});

		nextDistrictButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent finishSurvey = new Intent(UploadActivity.this, FinishSurveyActivity.class);
				startActivity(finishSurvey);
			}
		});

		deviceID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		if(deviceID.equals(null)){
			deviceID = "no device id found";
		}
		tempSurveyId = mSurvey.getTypeId().toString() + " - " + mSurvey.getTopicId().toString() +
				" - " + deviceID.toString();

		question1Answers = "Question....";
		for(int i = 0; i < mSurvey.getQuestions1().size(); i++){
			Questions q = (Questions)mSurvey.getQuestions1().get(i);
			if(q.getAnswer().equals("true")){
				question1Answers += q.getQuestionText() + " ";
			}
		}

		question2Answers = "Question....";
		for(int i = 0; i < mSurvey.getQuestions2().size(); i++){
			Questions r = (Questions)mSurvey.getQuestions2().get(i);
			if(r.getAnswer().equals("true")) {
				question2Answers += r.getQuestionText() + " ";
			}
		}

		if(mSurvey.getPictureLocations().isEmpty()){
			noImagePath = saveNoImageToFile();
			photoItem.setPhotoPath(noImagePath);
			items.add(photoItem);
			mSurvey.setPictureLocations(items);
		}

		if(isConnectedToMobileData(connectionManager) || isConnectedToWifi(connectionManager)) {
			startUploadToServer();
		}else{
			mSurvey.setUpdated("false");
			saveSurveyToDatabase();
		}
	}

	@Override
	public void onStart(){
		super.onStart();
		mDsa.open();
	}

	@Override
	public void onRestart(){
		super.onRestart();
		mDsa.open();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mDsa.close();
	}

	@Override
	public void onPause(){
		super.onPause();
		mDsa.close();
	}


	@Override
	public void onBackPressed() {
	}

	public void saveSurveyToDatabase(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<PhotoItems> photoArray = mSurvey.getPictureLocations();
				for(int j = 0; j < photoArray.size(); j++) {
					HashMap<String, String> uploadMap = new HashMap<String, String>();
					uploadMap.put("surveyid", tempSurveyId);
					uploadMap.put("typeid", mSurvey.getTypeId().toString());
					uploadMap.put("latitude", photoArray.get(j).getLatitude());
					uploadMap.put("longitude", photoArray.get(j).getLongitude());
					uploadMap.put("rating", mSurvey.getRating());
					uploadMap.put("picturelocation", photoArray.get(j).getPhotoPath());
					uploadMap.put("updated", mSurvey.getUpdated());
					uploadMap.put("topicid", mSurvey.getTopicId().toString());
					uploadMap.put("questiontext", question1Answers);
					uploadMap.put("answertext", question2Answers);
					savedToDB = mDsa.writeToDatabase(uploadMap, "surveys");
				}
			}
		}).start();
		Log.i("Database", savedToDB + "");
	}

	public void startUploadToServer(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ArrayList<PhotoItems> photoArray = mSurvey.getPictureLocations();
				for(int j = 0; j < photoArray.size(); j++){
					HashMap<String, String> uploadMap = new HashMap<String, String>();
					uploadMap.put("latitude", photoArray.get(j).getLatitude());
					uploadMap.put("questions", question1Answers);
					uploadMap.put("comments", question2Answers);
					uploadMap.put("longitude", photoArray.get(j).getLongitude());
					uploadMap.put("score", mSurvey.getRating());
					uploadMap.put("councilid", mSurvey.getTypeId().toString());
					uploadMap.put("siteid", mSurvey.getTopicId().toString());
					uploadMap.put("photo", photoArray.get(j).getPhotoPath());
					uploadMap.put("survey_id", tempSurveyId);
					uploadCardToServer(uploadMap, UploadActivity.this);
				}

			}
		}).start();
	}

	public static void uploadCardToServer(HashMap<String, String> cardData, final Context context){
		   InputStream is = null;
		   JSONObject jObj = null;
		   String json = "";
		   // Making HTTP request
	        try {
	            // defaultHttpClient
	            DefaultHttpClient httpClient = new DefaultHttpClient();
	            HttpPost post = new HttpPost();

	            HttpPost httppost = new HttpPost("http://siteurl.com/dv_api.php");

	            MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();

	            multipartEntity.addTextBody("function", "upload_image");
	            if(cardData.containsKey("survey_id") && cardData.get("survey_id") != null)
	            	multipartEntity.addTextBody("survey_id", cardData.get("survey_id"));
	            if(cardData.containsKey("latitude") && cardData.get("latitude") != null)
	            	multipartEntity.addTextBody("latitude", cardData.get("latitude"));
	            if(cardData.containsKey("longitude") && cardData.get("longitude") != null)
	            	multipartEntity.addTextBody("longitude", cardData.get("longitude"));
				if(cardData.containsKey("score") && cardData.get("score") != null)
					multipartEntity.addTextBody("score", cardData.get("score"));
				if(cardData.containsKey("councilid") && cardData.get("councilid") != null)
					multipartEntity.addTextBody("councilid", cardData.get("councilid"));
				if(cardData.containsKey("siteid") && cardData.get("siteid") != null)
					multipartEntity.addTextBody("siteid", cardData.get("siteid"));
				if(cardData.containsKey("questions") && cardData.get("questions") != null)
					multipartEntity.addTextBody("questions", cardData.get("questions"));
				if(cardData.containsKey("comments") && cardData.get("comments") != null)
					multipartEntity.addTextBody("comments", cardData.get("comments"));
	        	multipartEntity.addTextBody("users_id", "1");

	            if(cardData.containsKey("photo")){
		            final File file = new File(cardData.get("photo"));
		            FileBody fb = new FileBody(file);
		            multipartEntity.addPart("image", fb);
	            }

	            httppost.setEntity(multipartEntity.build());


	            httpClient.execute(httppost, new PhotoUploadResponseHandler(context));

	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        } catch (ClientProtocolException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        try {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(
	                    is, "iso-8859-1"), 8);
	            StringBuilder sb = new StringBuilder();
	            String line = null;
	            while ((line = reader.readLine()) != null) {
	                sb.append(line + "\n");
	            }
	            is.close();
	            json = sb.toString();
	        } catch (Exception e) {
	            Log.e("Buffer Error", "Error converting result " + e.toString());
	        }

	        // try parse the string to a JSON object
	        try {
	            jObj = new JSONObject(json);
	        } catch (JSONException e) {
	        }
	}

        private static class PhotoUploadResponseHandler implements ResponseHandler<Object> {

     	   private Context mContext;

     	   public PhotoUploadResponseHandler(Context context){
     		   mContext = context;
     	   }
        @Override
	    public Object handleResponse(HttpResponse response) {


	    	try{
		        HttpEntity r_entity = response.getEntity();
		        String responseString = EntityUtils.toString(r_entity);
		        Log.d("UPLOAD", "RES: " + responseString);

		        JSONObject jobject = new JSONObject(responseString);

		        if(jobject.getString("message").equals("success")) {
					Log.i("MESSAGE", "SUCCESS");
				}else{
					Log.i("MESSAGE", "FAILURE");
				}
	    	} catch(Exception e){

	    	}
	        return null;
	    }
	}

	private String saveNoImageToFile(){
		Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_image_available);
		String mFile = getFileName();
		File image = new File(mFile);

		FileOutputStream outStream;
		try {

			outStream = new FileOutputStream(image);
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, outStream);

			outStream.flush();
			outStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mFile;
	}

	private String getFileName(){
		Date date = new Date();
		String retFileName = Environment.getExternalStorageDirectory() + "/Survey/" + new SimpleDateFormat("yyyy-dd-MM-HH-mm-ss-SS", Locale.US).format(date) + "_survey.jpg";
		return retFileName;
	}

	private Boolean isConnectedToWifi(ConnectivityManager connectionManager){
		boolean connected = false;

		if(connectionManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED){
			connected = true;
		}

		return connected;
	}

	private Boolean isConnectedToMobileData(ConnectivityManager connectionManager){
		boolean connected = false;

		if(connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){
			connected = true;
		}

		return connected;
	}
}
