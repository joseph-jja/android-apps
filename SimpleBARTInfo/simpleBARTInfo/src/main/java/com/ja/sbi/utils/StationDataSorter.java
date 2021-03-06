package com.ja.sbi.utils;

import com.ja.sbi.beans.StationData;

import java.util.Comparator;

public final class StationDataSorter implements Comparator<StationData> {

    public int compare(StationData stationOne, StationData stationTwo) {

        if (stationOne == null) {
            return -1;
        }
        if (stationTwo == null) {
            return 1;
        }
        return stationOne.getStationName().compareTo(stationTwo.getStationName());
    }
}
