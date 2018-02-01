package com.triadicsoftware.surveyapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.triadicsoftware.surveyapp.database.Questions;
import com.triadicsoftware.surveyapp.database.Survey;

import java.util.ArrayList;

public class Questions2Activity extends Activity implements OnClickListener {
    private Survey mSurvey;
    private ArrayList<Questions> items;
    private QuestionListAdapter mAdapter;
    private ListView mListView;
    private TextView mTextview;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_questions);

        mSurvey = Survey.getInstance();

        Integer site = mSurvey.getTopicId();

        titleText = (TextView)findViewById(R.id.activity_title_text);
        titleText.setText("Survey Site " + site);

        items = new ArrayList<Questions>();

        Questions row0item = new Questions();
        row0item.setQuestionText("Question Text");
        items.add(row0item);

        Questions row1item = new Questions();
        row1item.setQuestionText("Question Text");
        items.add(row1item);

        Questions row2item = new Questions();
        row2item.setQuestionText("Question Text");
        items.add(row2item);

        Questions row3item = new Questions();
        row3item.setQuestionText("Question Text");
        items.add(row3item);

        Questions row4item = new Questions();
        row4item.setQuestionText("Question Text");
        items.add(row4item);

        Button nextButton = (Button) findViewById(R.id.next_bt);
        nextButton.setOnClickListener(this);

        mTextview = (TextView)findViewById(R.id.question_text);

        mListView = (ListView)findViewById(R.id.list_view);

    }

    public void onStart(){
        super.onStart();
        mTextview.setText("Question...");

        mAdapter = new QuestionListAdapter(this, R.layout.questions_view, items);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Questions selection = (Questions)parent.getItemAtPosition(position);
                if(selection.getAnswer().equals("false")){
                    selection.setAnswer("true");
                    ((ImageView)view.findViewById(R.id.yes_bt)).setImageResource((R.drawable.yes_bt_disabled_filled));
                }else{
                    selection.setAnswer("false");
                    ((ImageView)view.findViewById(R.id.yes_bt)).setImageResource((R.drawable.yes_bt_enabled_filled));
                }
                mListView.invalidateViews();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    public void goBack(View v){

        super.onBackPressed();
    }

    public void onStop(){
        super.onStop();
    }

    @Override
    public void onClick(View button) {
        // TODO Auto-generated method stub
        switch(button.getId()){


            case (R.id.next_bt):

                mSurvey.setQuestions2(items);

                Intent nextActivity = new Intent(Questions2Activity.this, RatingActivity.class);
                startActivity(nextActivity);

                break;

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MainActivity.SURVEY_COMPLETE_RESULT && resultCode == RESULT_OK){
            setResult(RESULT_OK);
            finish();
        }
    }

    class QuestionListAdapter extends ArrayAdapter<Questions>{

        private Context mContext;
        private int mResourceId;
        private ArrayList<Questions> mItems;

        public QuestionListAdapter(Context context, int layoutResourceId, ArrayList<Questions> items) {
            super(context, layoutResourceId, items);
            mContext = context;
            mResourceId = layoutResourceId;
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if(row == null){
                LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
                row = inflater.inflate(mResourceId, parent, false);
            }

            final Questions question = mItems.get(position);

            TextView questionText = (TextView)row.findViewById(R.id.question_box);
            questionText.setText(mItems.get(position).getQuestionText());

            ImageView yesButton = (ImageView)row.findViewById(R.id.yes_bt);

            if(question.getAnswer().equals("true")){
                yesButton.setImageResource(R.drawable.yes_bt_enabled_filled);
            }
            else{
                yesButton.setImageResource(R.drawable.yes_bt_disabled_filled);
            }

            return row;
        }

    }
}
