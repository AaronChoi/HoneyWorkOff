package com.honey.aaron.workoff.fragment;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.honey.aaron.workoff.R;
import com.honey.aaron.workoff.adapter.WeeklyWorkTimeListAdapter;
import com.honey.aaron.workoff.model.WorkDay;
import com.honey.aaron.workoff.util.TimeSharedPreferences;
import com.honey.aaron.workoff.util.TimeUtil;
import com.honey.aaron.workoff.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class WeeklyFragment extends BaseFragment {
    static WeeklyFragment weeklyFragment;

    // list of the work day
    public ArrayList<WorkDay> mList;
    WeeklyWorkTimeListAdapter mAdapter;
    Calendar cal;

    // view
    TextView tvWeeklyPeriod;
    TextView tvWeeklyWorkTime;
    ListView listWeeklyWork;


    public WeeklyFragment() {
        super();
    }

    public static WeeklyFragment newInstance() {
        if (weeklyFragment == null) {
            weeklyFragment = new WeeklyFragment();
        }
        return weeklyFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TAG = WeeklyFragment.class.getSimpleName();
        mList = new ArrayList<>();

        cal = Calendar.getInstance();
        Cursor cursor = sqlHelper.select(TimeUtil.getYear(cal.getTimeInMillis()), null, TimeUtil.getWeek(cal.getTimeInMillis()), null);
        while(cursor.moveToNext()) {
            Log.i(TAG, "cursor not null");
            mList.add(Util.makeTodayInstance(cursor));
        }
        makeEmptyDataAtDayOff();
        mAdapter = new WeeklyWorkTimeListAdapter(getActivity(), mList, pref);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly, container, false);

        tvWeeklyPeriod = (TextView) view.findViewById(R.id.tv_weekly_period);
        tvWeeklyWorkTime = (TextView) view.findViewById(R.id.tv_weekly_work_time);
        listWeeklyWork = (ListView) view.findViewById(R.id.list_weekly_work);

        tvWeeklyPeriod.setText(TimeUtil.getDatePeriodForThisWeek());
        tvWeeklyWorkTime.setText(getWeeklyWorkTime());

        listWeeklyWork.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // TODO 다이얼로그 팝업으로 시간 조정가능, 휴가 체크
                final WorkDay day = (WorkDay) parent.getAdapter().getItem(position);

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.time_update_title).setItems(R.array.time_update_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                /* User clicked so do some stuff */
                                switch (which) {
                                    case 0 :
                                        // 시작시간 변경
                                        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                // time 변경 후 list 데이터 업데이트
                                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                cal.set(Calendar.MINUTE, minute);
                                                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.KOREA);
                                                String fromTime = format.format(cal.getTime());
                                                long timestamp = TimeUtil.getMillisecondsFromString(day.getYear(), day.getMonth(), day.getDate(), fromTime);
                                                mList.get(position).setFromTime(fromTime);
                                                mList.get(position).setTimestamp(timestamp);
                                                mAdapter.setItemList(mList);
                                                mAdapter.notifyDataSetChanged();
                                                tvWeeklyWorkTime.setText(getWeeklyWorkTime());
                                                // db update
                                                Cursor cursor = sqlHelper.select(day.getYear(), day.getMonth(), null, day.getDate());
                                                if(cursor.getCount() > 0) {
                                                    sqlHelper.update(day.getYear(), day.getMonth(), day.getDate(), fromTime, null);
                                                } else {
                                                    sqlHelper.insert(day.getYear(), day.getMonth(), day.getWeek(), day.getDate(), day.getDay(), fromTime, null, String.valueOf(timestamp));
                                                }
                                            }
                                        }, day.getFromTime() == null ? 0 : Integer.parseInt(day.getFromTime().split(":")[0])
                                                , day.getFromTime() == null ? 0 : Integer.parseInt(day.getFromTime().split(":")[1]), true).show();
                                        dialog.dismiss();
                                        break;
                                    case 1 :
                                        // 종료시간 변경
                                        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                // time 변경 후 list 데이터 업데이트
                                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                                cal.set(Calendar.MINUTE, minute);
                                                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.KOREA);
                                                String toTime = format.format(cal.getTime());
                                                mList.get(position).setToTime(toTime);
                                                mAdapter.setItemList(mList);
                                                mAdapter.notifyDataSetChanged();
                                                tvWeeklyWorkTime.setText(getWeeklyWorkTime());
                                                // db update
                                                Cursor cursor = sqlHelper.select(day.getYear(), day.getMonth(), null, day.getDate());
                                                if(cursor.getCount() > 0) {
                                                    sqlHelper.update(day.getYear(), day.getMonth(), day.getDate(), null, toTime);
                                                } else {
                                                    sqlHelper.insert(day.getYear(), day.getMonth(), day.getWeek(), day.getDate(), day.getDay(), null, toTime, "0");
                                                }
                                            }
                                        }, day.getToTime() == null ? 0 : Integer.parseInt(day.getToTime().split(":")[0])
                                                , day.getToTime() == null ? 0 : Integer.parseInt(day.getToTime().split(":")[1]), true).show();
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        }).create().show();

                return true;
            }
        });
        listWeeklyWork.setAdapter(mAdapter);

        return view;
    }

    private String getWeeklyWorkTime() {
        long totalWorkTime = 0;
        for(WorkDay day : mList) {
            if(day.getTimestamp() == 0 || (!pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) && day.getToTime() == null)) continue;
            totalWorkTime += TimeUtil.getGapFromTimestamps(day.getTimestamp(), TimeUtil.isToday(day.getTimestamp()) && pref.getValue(TimeSharedPreferences.PREF_IS_WORKING, false) ?
                    Calendar.getInstance().getTimeInMillis() : TimeUtil.getMillisecondsFromString(day.getYear(), day.getMonth(), day.getDate(), day.getToTime()));
        }

        return TimeUtil.getTotalWorkTime(totalWorkTime);
    }

    private void makeEmptyDataAtDayOff() {
        if(mList.size() == 7) return;

        cal = Calendar.getInstance();
        for(int i = 0 ; i < 7 ; i++) {
            // 토요일 부터 데이터가 있는지 확인
            if(i < mList.size()) {
                cal.setTimeInMillis(mList.get(i).getTimestamp());
                if (cal.get(Calendar.DAY_OF_WEEK) != 7 - i) {
                    cal.set(Calendar.DAY_OF_WEEK, 7 - i);
                    mList.add(i, Util.makEmptyWorkDay(cal));
                    Log.d(TAG, "size : " + mList.size());
                }
            } else {
                cal.set(Calendar.DAY_OF_WEEK, 7 - i);
                mList.add(i, Util.makEmptyWorkDay(cal));
                Log.d(TAG, "size : " + mList.size());
            }
        }
    }
}
