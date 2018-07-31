package com.ja.minnow.listeners;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ja.activity.BaseActivity;
import com.ja.database.Table;
import com.ja.dialog.BaseDialog;
import com.ja.minnow.Constants;
import com.ja.minnow.MinnowRSS;
import com.ja.dialog.LoadingSpinner;
import com.ja.minnow.R;
import com.ja.minnow.tables.FeedsTableData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImportExportListener implements Runnable {

    private LoadingSpinner dialog;
    private static MinnowRSS context;
    private static List<Table> feedsList;

    private final Class<?> _self = getClass();
    private final String TAG = _self.getName();

    private static final String backupFilename = "MinnowRSS.csv";

    // default this to export
    private static boolean isExport = true;
    
    private final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;

    public void exportFeeds(MinnowRSS activity) {

        ImportExportListener.context = activity;
        ImportExportListener.isExport = true;
        
        boolean weGotPermissions = false;
        if (ContextCompat.checkSelfPermission(ImportExportListener.context, 
             Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) 
        {
             weGotPermissions = true;
        }
        
        // no permisiions so show message asking about them
        if (!weGotPermissions) { 
            if (ActivityCompat.shouldShowRequestPermissionRationale(ImportExportListener.context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) 
            {
                // Show an explanation to the user 
                BaseDialog.alert(this, "Sorry, this needs permissions to access internal storage to export the data.");
            } else {
                ActivityCompat.requestPermissions(ImportExportListener.context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
            }
        }

        // only call this if we have permissions
        if (weGotPermissions) {
            dialog = new LoadingSpinner((Context) activity, "Collecting feeds, please wait.");

            new Thread(this).start();
        }
    }

    public void importFeeds(MinnowRSS activity) {

        ImportExportListener.context = activity;
        ImportExportListener.isExport = false;

        boolean weGotPermissions = false;
        if (ContextCompat.checkSelfPermission(ImportExportListener.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
             weGotPermissions = true;
        }
        
        // only call this if we have permissions
        if (weGotPermissions) {
           dialog = new LoadingSpinner((Context) activity, "Collecting feeds, please wait.");

           new Thread(this).start();
        }
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String makeExternalDirs() throws IOException {

        if (isExternalStorageWritable()) {
            Log.d(TAG, "Wooo hoo we might be able to write!");
            return null;
        }

        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!sd.exists() || !sd.mkdirs()) {
            Log.d(TAG, "We tried to write the dirs, but what the fudge write?");
            return null;
        }

        File minnowRSSData = new File(sd, backupFilename);

        if (!minnowRSSData.createNewFile()) {
            Log.d(TAG, "We tried to write the file, but no love!");
            return null;
        }
        return minnowRSSData.getAbsolutePath();
    }
    
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    // do export
                    dialog = new LoadingSpinner((Context) activity, "Collecting feeds, please wait.");

                    new Thread(this).start();
                } else {
                    // permission denied, boo! 
                }
                return;
            }       }
    }

    public void run() {
        try {

            if (ImportExportListener.isExport) {
                feedsList = Constants.getFeedsservice().listFeeds(ImportExportListener.context);
                Log.d(TAG, "Got some data " + Integer.valueOf(feedsList.size()).toString());

                // need to convert the data into something useful
                StringBuilder results = new StringBuilder();
                for (Table tbl : feedsList) {
                    final String feedName = "\"" + tbl.getColumnValue(FeedsTableData.NAME_COL).toString() + "\"";
                    final String feedUrl = "\"" + tbl.getColumnValue(FeedsTableData.URL_COL).toString() + "\"";
                    // write data in csv format
                    final String feedLine = feedName + "," + feedUrl + System.getProperty("line.separator");
                    results.append(feedLine);
                }

                // TODO actually write this out somewhere
                final String datafile = makeExternalDirs();
                if (datafile != null) {
                    Log.d(TAG, "We got a file name: " + datafile);
                }

            } else{
                feedsList = new ArrayList<Table>();
                // TODO read the data file in, if it exists and then add the tables in
                // should compare data
            }

        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            loadingHandler.sendMessage(loadingHandler.obtainMessage());
        }

    }

    private final Handler loadingHandler = new Handler() {
        public void handleMessage(Message msg) {

            //ImportExportListener.context.setContentView(R.layout.feeds_list);

            dialog.dismiss();
        }
    };
}
