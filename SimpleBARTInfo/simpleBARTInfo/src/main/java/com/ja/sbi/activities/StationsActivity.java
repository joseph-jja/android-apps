package com.ja.sbi.activities;

import java.util.List;

import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ja.database.Table;
import com.ja.sbi.R;
import com.ja.sbi.SimpleBARTInfo;
import com.ja.sbi.adapters.StationListAdapter;
import com.ja.sbi.listeners.StationListener;
import com.ja.sbi.table.DataManager;

public class StationsActivity extends SBIBaseActivity {

	private final String LOG_NAME = this.getClass().getName();
	
    public boolean onContextItemSelected(MenuItem item) {
    	
    	Log.d(LOG_NAME, "Context menu item selected.");
    	final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	View view = info.targetView;
    	final LinearLayout layout = ((LinearLayout)view);
    	View dbView = layout.findViewById(R.id.station_short_name);
    	
    	switch (item.getItemId()) {
    		case R.id.context_add:
    			if ( dbView != null ) { 
    				TextView tv = (TextView)dbView;
    				final String value = tv.getText().toString();
    				final List<Table> results = this.dbAdapter.find(DataManager.SETTINGS_TABLE, DataManager.VALUE_COL, "\'" + value + "\'");
    				if ( results == null || results.size() <= 0 ) { 
	        			this.dbAdapter.beginTransaction();
	        			final Table addFavorite = new Table();
	        			addFavorite.setTableName(DataManager.SETTINGS_TABLE);
	        			addFavorite.setColumnValue(DataManager.NAME_COL, DataManager.FAVORITES_SETTING);
	        			addFavorite.setColumnValue(DataManager.VALUE_COL, value);
	        			try { 
		        			this.dbAdapter.insert(DataManager.SETTINGS_TABLE, addFavorite.getInternalData());
		        			this.dbAdapter.setTransactionSuccessful();
	        			} catch (Exception ex) { 
	        				ex.printStackTrace();
	        			}
	        			this.dbAdapter.endTransaction();
	        			Log.d(LOG_NAME, "Should have added a row to the database: " + addFavorite.getInternalData());
    				}
    			}
    			break;
    	}
    	return super.onContextItemSelected(item);
    }
	
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		Log.d(LOG_NAME, "In create context menu + " + v.toString());
		// only create this menu if the view is the feeds screen
		final MenuInflater inflater = getMenuInflater();
		
		if ( this.isViewStations() ) {
			Log.d(LOG_NAME, "Got correct view: favorites");
		    inflater.inflate(R.menu.add_favorite, menu);
			super.onCreateContextMenu(menu, v, menuInfo);
		} 
	}

    public void setupView() {
		
		setContentView(R.layout.stations); 
    	
		final View view = this.findViewById(R.id.simple_bart_info_title);
		if ( view == null ) { 
			Log.d(LOG_NAME, "Guess we did not find the view?");
			return;
		}
    	
    	TextView tview = (TextView)view;
    	tview.setText("Stations");
    	
    	final ListView feedList = (ListView)this.findViewById(R.id.st_list_rows);
    	Log.d(LOG_NAME, "Do we have any stations? " + this.stations);
		feedList.setAdapter( new StationListAdapter(this, R.layout.data_row, this.stations) );	
		feedList.setOnItemClickListener( new StationListener() );
		this.registerForContextMenu(feedList);
		
		this.setViewStations(true);
		selectedStationName = null;
		selectedStationShortName = null;
		
		this.getIntent().putExtra(SimpleBARTInfo.DATA_KEY, SimpleBARTInfo.ALL_STATIONS);
	}	
}