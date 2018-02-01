package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.triadicsoftware.surveyapp.database.DataSourceAccessor;
import com.triadicsoftware.surveyapp.database.SurveyAppSQLiteHelper;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity{
	SurveyAppSQLiteHelper helper;
	SQLiteDatabase db;
	private boolean isShowing;
	DataSourceAccessor mDsa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		helper = new SurveyAppSQLiteHelper(this);
		db = helper.getWritableDatabase();

		isShowing = true;

		new Timer().schedule(new TimerTask(){
        	public void run()
        	{
        		if(isShowing){
        			Intent i=new Intent();
            		i.setClass(SplashActivity.this, MainActivity.class);
        			startActivity(i);
        			finish();
        		}
        	}
        },3000);
	}

	@Override
	protected void onRestart() {

		if(!isShowing){
			Intent i=new Intent();
    		i.setClass(SplashActivity.this, MainActivity.class);
			startActivity(i);
			finish();
		}
		super.onRestart();
	}

	@Override
	protected void onStop() {
		isShowing = false;
		super.onStop();
		db.close();
	}


}
