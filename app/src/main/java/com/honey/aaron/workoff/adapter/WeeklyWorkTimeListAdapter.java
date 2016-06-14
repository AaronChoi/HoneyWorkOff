package com.honey.aaron.workoff.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.honey.aaron.workoff.R;
import com.honey.aaron.workoff.model.WorkDay;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;

import java.util.ArrayList;

public class WeeklyWorkTimeListAdapter extends BaseAdapter {
    private static final String TAG = WeeklyWorkTimeListAdapter.class.getSimpleName();

    private LayoutInflater mInflater;
    private Context context;
    private ArrayList<WorkDay> itemList;
    WorkTimeViewHolder holder;
    TimeSharedPreferences pref;

    public WeeklyWorkTimeListAdapter(Context context, ArrayList<WorkDay> itemList, TimeSharedPreferences pref){
        this.context = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        this.itemList = itemList;
        this.pref = pref;
    }

    @Override
    public int getCount() {
        return itemList == null? 0 : itemList.size();
    }

    @Override
    public Object getItem(int position) {
        if(getCount() > position) {
            return itemList.get(position);
        }else{
            return null;
        }
    }

    public void setItemList(ArrayList<WorkDay> itemList) {
        this.itemList = itemList;
    }

    @Override
    public long getItemId(int position) {
        return itemList.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_weekly_work, null);

            holder = new WorkTimeViewHolder();
            holder.tvWorkDay = (TextView) convertView.findViewById(R.id.tv_work_day);
            holder.tvFromTime = (TextView) convertView.findViewById(R.id.tv_from_time);
            holder.tvToTime = (TextView) convertView.findViewById(R.id.tv_to_time);
            holder.tvWorkTime = (TextView) convertView.findViewById(R.id.tv_work_time);

            convertView.setTag(holder);
        } else {
            holder = (WorkTimeViewHolder) convertView.getTag();
        }

        // list view item setting
        WorkDay item = itemList.get(position);

        // work day
        holder.tvWorkDay.setText(String.format(context.getString(R.string.weekly_work_day), item.getMonth(), item.getDate(), item.getDay()));
        // work from time
        holder.tvFromTime.setText(item.getFromTime() == null || "".equals(item.getFromTime()) ? "-" : item.getFromTime());
        // work to time
        holder.tvToTime.setText(TimeUtil.isToday(item.getTimestamp()) && pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ? TimeUtil.getTime(System.currentTimeMillis()) :
                item.getToTime() == null || "".equals(item.getToTime()) ? "-" : item.getToTime());
        // work total time
        holder.tvWorkTime.setText(item.getTimestamp() == 0 || (!pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) && item.getToTime() == null) ? "00:00" :
                TimeUtil.getTotalWorkTime(item.getTimestamp(), TimeUtil.isToday(item.getTimestamp()) && pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ?
                        System.currentTimeMillis() : TimeUtil.getMillisecondsFromString(item.getYear(), item.getMonth(), item.getDate(), item.getToTime())));

        return convertView;
    }

    private class WorkTimeViewHolder {
        /**
         * holder for list items
         */
        public TextView tvWorkDay;
        public TextView tvFromTime;
        public TextView tvToTime;
        public TextView tvWorkTime;
    }
}
