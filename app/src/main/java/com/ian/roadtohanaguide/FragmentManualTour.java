package com.ian.roadtohanaguide;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hb.views.PinnedSectionListView;

import java.util.ArrayList;

/**
 * Created by IanZ on 1/15/2016.
 */
public class FragmentManualTour extends android.app.Fragment {
    public static ListView manualTourListView;
    public static ListViewAdapter manualTourAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manual_tour, container, false);


        ActivityMain.sortGeofenceInfosOrderNumber();
        manualTourAdapter = new ListViewAdapter(getActivity(), getResources(), ActivityMain.mGeofenceInfos);
        manualTourListView = (ListView) rootView.findViewById(R.id.manualTourListView);
        manualTourListView.setAdapter(manualTourAdapter);

        return rootView;
    }

    public static FragmentManualTour newInstance() {

        Bundle args = new Bundle();

        FragmentManualTour fragment = new FragmentManualTour();
        fragment.setArguments(args);
        return fragment;
    }

    //<editor-fold desc="ListViewAdapter">
    /**
     * custom List view adapter that implements a library class allowing for sticky or "pinned" headers
     */
    //</editor-fold>
    public class ListViewAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter{
        public static final int PIN_VIEW = 0;
        public static final int REGULAR_VIEW = 1;
        Activity activity;
        Resources res;
        LayoutInflater inflater;
        ArrayList<GeofenceInfo> list;

        public ListViewAdapter(Activity a, Resources res, ArrayList<GeofenceInfo> list) {
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
            if(list.get(position).isHeader()) {
                View v = inflater.inflate(R.layout.list_item_manual_sticky_header, parent, false);
                TextView tv = (TextView) v.findViewById(R.id.stickyHeaderText);
                tv.setText(list.get(position).getRequestId());
                return v;
            }
            else{
                View v = inflater.inflate(R.layout.list_item_manual_tour, parent, false);
                TextView titleTV = (TextView) v.findViewById(R.id.listItemManualTitle);
                TextView mMTV = (TextView) v.findViewById(R.id.listItemManualMMText);
                ImageView playButton = (ImageView) v.findViewById(R.id.listItemManualPlay);
                GeofenceInfo info = list.get(position);

                titleTV.setText(info.getRequestId());
                mMTV.setText("MM " + info.getMileMarker());
                return v;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if(list.get(position).isHeader()){
                return PIN_VIEW;
            }
            else{
                return REGULAR_VIEW;
            }
        }

        @Override
        public boolean isItemViewTypePinned(int viewType) {
            return viewType ==PIN_VIEW;
        }
    }
}
