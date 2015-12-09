package com.senic.nuimo.demo;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LogArrayAdapter extends ArrayAdapter<String[]> {

    public LogArrayAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.log_item, parent, false);
            view.setTag(new ViewHolder(view));
        }
        ((ViewHolder) view.getTag()).textView.setText(TextUtils.join(": ", getItem(position)));
        return view;
    }

    private class ViewHolder {
        TextView textView;

        public ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.text);
        }
    }
}
