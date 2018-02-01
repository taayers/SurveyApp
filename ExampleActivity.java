package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by todd on 1/26/16.
 */
public class ExampleActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_example);

    }

    @Override
    public void onBackPressed() {
    }

    public void goBack(View v){

        super.onBackPressed();
    }
}
