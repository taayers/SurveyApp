package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.triadicsoftware.surveyapp.database.DataSourceAccessor;
import com.triadicsoftware.surveyapp.database.Survey;
import com.triadicsoftware.surveyapp.database.SurveyData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends Activity implements OnClickListener {
	private DataSourceAccessor mDsa;
	private Survey mSurvey = null;
	public static final int SURVEY_COMPLETE_RESULT = 9909;
	private ArrayList sitesCompleted;
	Calendar cal;
	SimpleDateFormat dateFormat;
	Date currentDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final SurveyData surveyData = (SurveyData)getApplicationContext();
		sitesCompleted = surveyData.getSitesCompleted();

		sitesCompleted.clear();
		surveyData.setSitesCompleted(sitesCompleted);

		Button newAreaButton = (Button) findViewById(R.id.newAreaEvalBt);
		newAreaButton.setOnClickListener(this);

		mSurvey = Survey.getInstance();

		cal = new GregorianCalendar();
		currentDate = cal.getTime();

		dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

		mDsa = new DataSourceAccessor(this);
	}

	public void onStart() {
		super.onStart();
	}

	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View button) {
		// TODO Auto-generated method stub

		switch (button.getId()) {

			case (R.id.newAreaEvalBt):
				mSurvey.setDateTime(dateFormat.format(currentDate));
				Intent nextActIntent = new Intent();
				nextActIntent.setClass(MainActivity.this, TopicsActivity.class);
				startActivity(nextActIntent);
				break;

		}

	}

}
