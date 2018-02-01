package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.triadicsoftware.surveyapp.database.Survey;
import com.triadicsoftware.surveyapp.database.SurveyData;

import java.util.ArrayList;

public class TopicsActivity extends Activity {

	private Survey mSurvey;
	private ListView listView;
	private int arraySize = 25;
	private ArrayList<Integer> sitesCompleted;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topic_chooser);

		mSurvey = Survey.getInstance();

		final SurveyData surveyData = (SurveyData)getApplicationContext();
		sitesCompleted = new ArrayList<Integer>();
		surveyData.setSitesCompleted(sitesCompleted);


		Button button = (Button)findViewById(R.id.navigation_back_btn);
		button.setEnabled(false);
		button.setVisibility(View.INVISIBLE);

		TextView textview = (TextView)findViewById(R.id.activity_title_text);
		textview.setText("Select District");

		String[] ints = new String[arraySize];
		for(int i = 0; i < arraySize; i++){
			ints[i] = i + 1 + "";
		}


		listView = (ListView)findViewById(R.id.topic_list);

		MyArrayAdapter adapter = new MyArrayAdapter(this, ints, "District");
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				mSurvey.setTypeId(position + 1);

				Intent nextActivity = new Intent(TopicsActivity.this, SiteActivity.class);
				startActivity(nextActivity);

			}

		});
	}

	public void onStart(){
		super.onStart();
	}

	public void onStop(){
		super.onStop();
	}

	@Override
	public void onBackPressed() {
	}

	public void goBack(View v){
		super.onBackPressed();
	}

}
