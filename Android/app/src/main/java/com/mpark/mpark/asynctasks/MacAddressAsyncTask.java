package com.mpark.mpark.asynctasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.mpark.mpark.activities.SplashScreen;
import com.mpark.mpark.dbOperations.DatabaseHelper;
import com.mpark.mpark.dbOperations.DbHelper;
import com.mpark.mpark.pojo.ParkingLot;
import com.mpark.mpark.utilities.AppGlobalData;
import com.mpark.mpark.utilities.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by saif on 2018-01-16.
 */

public class MacAddressAsyncTask extends AsyncTask<Void,Void,Void>{
    ProgressDialog pDialog;
    Context context;
    public static final String url = "http://quicsolv.com/m-park/api/parking-list.php";

    public MacAddressAsyncTask(Context context){
        this.context=context;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AppGlobalData.parkingsList.clear();
        // Showing progress dialog
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    protected Void doInBackground(Void... arg0) {
        HttpHandler sh = new HttpHandler();

        // Making a request to url and getting response
        String jsonStr = sh.makeServiceCall(url);

        Log.d(TAG, "Response from url: " + jsonStr);

        DbHelper helper = new DbHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        DatabaseHelper source = new DatabaseHelper(database);

        if (jsonStr != null) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                String code = jsonObj.getString("code");
                // Getting JSON Array node
                JSONArray parkingLots = jsonObj.getJSONArray("list");

                // looping through All Contacts
                for (int i = 0; i < parkingLots.length(); i++) {
                    JSONArray currentParking = parkingLots.getJSONArray(i);
                    //0 : id, 1: name, 2: mac
                    String id = currentParking.getString(0);
                    String name = currentParking.getString(1);
                    String mac = currentParking.getString(2);
                    ParkingLot parkingLot = new ParkingLot(id,name,mac);
                    AppGlobalData.parkingsList.add(parkingLot);

                    source.insert(parkingLot);
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Couldn't get json from server.");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        System.out.println("THE PARKING MAC ADDRESSES ARE : ");
        for(int i =0;i<AppGlobalData.parkingsList.size();i++){
            System.out.println(AppGlobalData.parkingsList.get(i).getMacAddress());
        }
        // Dismiss the progress dialog
        if (pDialog.isShowing())
            pDialog.dismiss();

        //Redirect now
        SplashScreen splashScreen = (SplashScreen)context;
        splashScreen.redirect();

        /**
         * Updating parsed JSON data into ListView
         * */

    }

}

