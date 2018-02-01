package com.triadicsoftware.surveyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by todd on 12/21/15.
 */
public class MyArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final String title;

    public MyArrayAdapter(Context context, String[] values, String title) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.title = title;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.district_row, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.district_and_site_row_text);
        textView.setText(title + " " + values[position]);
        return rowView;
    }
}
