package com.ian.roadtohanaguide;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by IanZ on 12/23/2015.
 */
public class FragmentNearestLocations extends android.app.Fragment {
    public static ListView nearestLocationsListView;
    public static ListViewAdapter adapter;
    public static TextView debugText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearest_locations, container, false);
        nearestLocationsListView = (ListView) rootView.findViewById(R.id.nearestLocationsListView);
        adapter =new ListViewAdapter(getActivity(), getResources(), ActivityMain.mGeofenceInfos);

        nearestLocationsListView.setAdapter(adapter);


        return rootView;
    }

    public static FragmentNearestLocations newInstance() {

        Bundle args = new Bundle();

        FragmentNearestLocations fragment = new FragmentNearestLocations();
        fragment.setArguments(args);
        return fragment;
    }

    public class ListViewAdapter extends BaseAdapter{
        Activity activity;
        Resources res;
        LayoutInflater inflater;
        ArrayList<GeofenceInfo> list;

        public ListViewAdapter(Activity a, Resources res, ArrayList<GeofenceInfo> list){
            activity = a;
            this.res = res;
            inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.list = list;

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = inflater.inflate(R.layout.list_item_nearest_locations, parent, false);
            TextView tv = (TextView) v.findViewById(R.id.textView);
            tv.setText(list.get(position).getRequestId()+" "+String.format("%.2f",list.get(position).getDistanceBetween()));
            return v;
        }
    }
}
