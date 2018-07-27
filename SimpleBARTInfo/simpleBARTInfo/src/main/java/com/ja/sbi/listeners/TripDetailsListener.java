package com.ja.sbi.listeners;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ja.sbi.R;
import com.ja.sbi.SimpleBARTInfo;
import com.ja.sbi.adapters.TripLegAdapter;
import com.ja.sbi.beans.StationData;
import com.ja.sbi.beans.Trip;

import java.util.List;

public class TripDetailsListener implements AdapterView.OnItemClickListener  {

    private List<Trip> trips;
    private List<StationData> stationData;

    public void setTripList(List<Trip> tripList) {
        this.trips = tripList;
    }

    public void setStationData(List<StationData> data) {
        this.stationData = data;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final SimpleBARTInfo sbi = (SimpleBARTInfo) parent.getContext();

        final View onDetailsView = sbi.findViewById(R.id.trip_leg_title);
        if ( onDetailsView != null ) {
            return;
        }

        final Trip currentTrip = this.trips.get(position);

        final TripLegAdapter tripLeg = new TripLegAdapter(sbi, R.layout.trip_legs, currentTrip.getLegs());
        tripLeg.setStationData(this.stationData);

        final ListView list = (ListView)sbi.findViewById(R.id.trip_planner_results);
        list.setAdapter(tripLeg);
    }

}
