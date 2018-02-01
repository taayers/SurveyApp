package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by todd on 1/26/16.
 */
public class FinishSurveyActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_next_district);

        Button startNextDistrict = (Button)findViewById(R.id.start_new_district_btn);

        startNextDistrict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startNextDistrictActivity = new Intent(FinishSurveyActivity.this, MainActivity.class);
                startActivity(startNextDistrictActivity);

            }
        });
    }
}
