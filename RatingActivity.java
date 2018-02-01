package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.triadicsoftware.surveyapp.database.DataSourceAccessor;
import com.triadicsoftware.surveyapp.database.Survey;

public class RatingActivity extends Activity implements OnClickListener {
    private Survey mSurvey;
    private DataSourceAccessor mDsa;
    private String rating;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        mSurvey = Survey.getInstance();

        Integer site = mSurvey.getTopicId();

        titleText = (TextView)findViewById(R.id.activity_title_text);
        titleText.setText("Survey Site " + site);

        Button nextButton = (Button) findViewById(R.id.next_bt);
        nextButton.setOnClickListener(this);

        Button exampleButton = (Button)findViewById(R.id.view_examples_btn);
        exampleButton.setOnClickListener(this);

        mDsa = new DataSourceAccessor(this);

    }

    @Override
    public void onBackPressed() {
    }

    public void goBack(View v){

        super.onBackPressed();
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.rating_one:
                if (checked)
                    rating = "10";
                    break;
            case R.id.rating_onefive:
                if (checked)
                    rating = "15";
                break;
            case R.id.rating_two:
                if (checked)
                    rating = "20";
                    break;
            case R.id.rating_twofive:
                if (checked)
                    rating = "25";
                break;
            case R.id.rating_three:
                if (checked)
                    rating = "30";
                    break;
            case R.id.rating_threefive:
                if (checked)
                    rating = "35";
                break;
            case R.id.rating_four:
                if (checked)
                    rating = "40";
                    break;

        }
    }

    @Override
    public void onClick(View button) {
        // TODO Auto-generated method stub
        switch (button.getId()) {


            case (R.id.next_bt):

                mSurvey.setRating(rating);

                Intent uploadActivity = new Intent(RatingActivity.this, UploadActivity.class);
                startActivity(uploadActivity);

                break;

            case (R.id.view_examples_btn):

                Intent exampleActivity = new Intent(RatingActivity.this, ExampleActivity.class);
                startActivity(exampleActivity);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.SURVEY_COMPLETE_RESULT && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }


}
