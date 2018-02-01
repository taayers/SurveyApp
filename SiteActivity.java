package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.triadicsoftware.surveyapp.database.DataSourceAccessor;
import com.triadicsoftware.surveyapp.database.PhotoItems;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by todd on 12/20/15.
 */

public class SiteActivity extends Activity{

    Survey mSurvey;
    private int arraySize = 10;
    private int district;
    private ArrayList<Integer> sitesCompleted;
    private ArrayList<Survey> surveyList;
    private DataSourceAccessor mDsa;
    private ConnectivityManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_chooser);

        connectionManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        Button backButton = (Button)findViewById(R.id.navigation_back_btn);

        mDsa = new DataSourceAccessor(this);
        mDsa.open();

        surveyList = new ArrayList<Survey>();
        surveyList.addAll(mDsa.getUploadableSurveys());
        Log.i("UPLOADABLE SURVEYS", surveyList.size() + "");

        mSurvey = Survey.getInstance();
        final SurveyData surveyData = (SurveyData)getApplicationContext();

        sitesCompleted = new ArrayList<Integer>();
        if(!surveyData.getSitesCompleted().isEmpty()){
            sitesCompleted = surveyData.getSitesCompleted();
            backButton.setVisibility(View.INVISIBLE);
            backButton.setEnabled(false);
        }

        district = mSurvey.getTypeId();

        TextView textview = (TextView)findViewById(R.id.activity_title_text);
        textview.setText("Choose a Site");

        final ListView listView = (ListView) findViewById(R.id.site_list);

        String[] ints = new String[arraySize];
        for(int i = 0; i < arraySize; i++){
            ints[i] = i + 1 + "";
        }

        SiteArrayAdapter siteList = new SiteArrayAdapter(this, ints, "Site", district, sitesCompleted);

        listView.setAdapter(siteList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mSurvey.setTopicId(position + 1);

                Intent nextActivity = new Intent(SiteActivity.this, PictureActivity.class);
                startActivity(nextActivity);

            }
        });

        for(Survey item : surveyList) {

            if (isConnectedToMobileData(connectionManager) || isConnectedToWifi(connectionManager)) {
                startUploadToServer(item);
                item.setUpdated("true");
                updateDatabase(item);
                Log.i("REUPLOAD", "SUCCESSFUL");
            }else{
                Log.i("REUPLOAD", "FAILED");
            }

        }
    }



    @Override
    public void onBackPressed() {
    }

    public void goBack(View v){

        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mDsa.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDsa.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDsa.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDsa.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDsa.open();
    }

    private class SiteArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;
        private final ArrayList<Integer> sitesCompleted;
        private final String title;
        private final int district;

        public SiteArrayAdapter(Context context, String[] values, String title, int district,
                                ArrayList<Integer> sitesCompleted) {
            super(context, -1, values);
            this.context = context;
            this.values = values;
            this.title = title;
            this.district = district;
            this.sitesCompleted = sitesCompleted;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.site_row, parent, false);

            TextView textView = (TextView) rowView.findViewById(R.id.district_and_site_row_text);
            textView.setText(title + " " + district + " - " + values[position]);

            CheckBox checkBox = (CheckBox)rowView.findViewById(R.id.check_box);

            for(Integer i: sitesCompleted){
                if((position + 1) == i){
                    checkBox.setChecked(true);
                }
            }


            return rowView;
        }

        @Override
        public boolean isEnabled(int position) {
            return super.isEnabled(position);
        }
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

    public void updateDatabase(Survey survey){
        final Survey surveyToSave = survey;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<PhotoItems> photoArray = surveyToSave.getPictureLocations();
                for(int j = 0; j < photoArray.size(); j++) {
                    HashMap<String, String> uploadMap = new HashMap<String, String>();
                    uploadMap.put("surveyid", surveyToSave.getSurveyId());
                    uploadMap.put("typeid", surveyToSave.getTypeId().toString());
                    uploadMap.put("latitude", photoArray.get(j).getLatitude());
                    uploadMap.put("longitude", photoArray.get(j).getLongitude());
                    uploadMap.put("rating", surveyToSave.getRating());
                    uploadMap.put("picturelocation", photoArray.get(j).getPhotoPath());
                    uploadMap.put("updated", surveyToSave.getUpdated());
                    uploadMap.put("topicid", surveyToSave.getTopicId().toString());
                    uploadMap.put("questiontext", surveyToSave.getQuestionText());
                    uploadMap.put("answertext", surveyToSave.getAnswerText());
                    mDsa.updateDatabaseField(uploadMap, "surveys", surveyToSave.getColumnId());
                }
            }
        }).start();
    }

    public void startUploadToServer(Survey survey){
        final Survey surveyToUpload = survey;
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                ArrayList<PhotoItems> photoArray = surveyToUpload.getPictureLocations();
                for(int j = 0; j < photoArray.size(); j++){
                    HashMap<String, String> uploadMap = new HashMap<String, String>();
                    uploadMap.put("latitude", photoArray.get(j).getLatitude());
                    uploadMap.put("questions", surveyToUpload.getQuestionText());
                    uploadMap.put("comments", surveyToUpload.getAnswerText());
                    uploadMap.put("longitude", photoArray.get(j).getLongitude());
                    uploadMap.put("score", surveyToUpload.getRating());
                    uploadMap.put("councilid", surveyToUpload.getTypeId().toString());
                    uploadMap.put("siteid", surveyToUpload.getTopicId().toString());
                    uploadMap.put("photo", photoArray.get(j).getPhotoPath());
                    uploadMap.put("survey_id", surveyToUpload.getSurveyId());
                    uploadCardToServer(uploadMap, SiteActivity.this);
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


}
