package com.example.dimitris.apothnarxh24;

/**
 * Created by dimitris on 8/7/2018.
 */


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import java.text.DateFormat;
import java.util.Date;
import static java.lang.String.valueOf;


public class DatabaseActivity
        extends Activity
        implements SensorEventListener , LocationListener, GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener {

    ListView listView ;
    private static final int UPDATE_THRESHOLD = 500;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long mLastUpdate;
    public TextView mZValueView;

    private DatabaseOpenHelper helper;
    private SimpleCursorAdapter mAdapter;

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    public Location mCurrentLocation;
    String mLastUpdateTime;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    String lat;
    String lon;

    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION =
            {android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database);

        Log.d(TAG, "onCreate ...............................");
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (null == mAccelerometer){ finish();}
        mZValueView = (TextView) findViewById(R.id.z_value_view);
        listView = (ListView) findViewById(R.id.list);

        helper = new DatabaseOpenHelper(this);
        helper.deleteDatabase();

        SQLiteDatabase db = helper.getWritableDatabase();
        String [] projection = {
                helper.id,
                helper.axe,
                helper.lat,
                helper.lon
        };
        String selection = null;
        String [] selectionArgs = new String[]{};

        Cursor c = db.query(DatabaseOpenHelper.table,
                projection ,
                selection ,
                selectionArgs,
                null,
                null,
                null);
        mAdapter = new SimpleCursorAdapter(this, R.layout.list, c,
                DatabaseOpenHelper.columns, new int[] { R.id._id, R.id.value_axe, R.id.value_lat, R.id.value_lon },
                0);
        listView.setAdapter(mAdapter);

        Button fixButton = (Button) findViewById(R.id.fix_button);
        fixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fix();
            }
        });
        fixButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //clearAll();
                return true;
            }
        });
    }

    public void selfDestruct(View view) {
        switch (view.getId()) {
            case R.id.button:
                Intent i = new Intent(this, HolesActivity.class);
                startActivity(i);
                onPause();
                break;
        }
    }

    public void fix() { onPause(); }

    @Override
    protected void onDestroy() {
        helper.getWritableDatabase().close();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (ContextCompat.checkSelfPermission(DatabaseActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permissions have not been granted.
            Log.i(TAG, "Database permissions has NOT been granted. Requesting permissions.");
            ActivityCompat.requestPermissions(DatabaseActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long actualTime = System.currentTimeMillis();
            if (actualTime - mLastUpdate > UPDATE_THRESHOLD) {
                mLastUpdate = actualTime;
                float z = event.values[2];
                mZValueView.setText(valueOf(z));
                ContentValues values = new ContentValues();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                values.put(DatabaseOpenHelper.axe, valueOf(z));
                values.put(DatabaseOpenHelper.lat, valueOf(lat));
                values.put(DatabaseOpenHelper.lon, valueOf(lon));
                helper.getWritableDatabase().insert(DatabaseOpenHelper.table, null, values);
                mAdapter.getCursor().requery();
                mAdapter.notifyDataSetChanged();
                if (mCurrentLocation != null) {
                    updateUI();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        if (null != mCurrentLocation) {
            lat = String.valueOf(mCurrentLocation.getLatitude());
            lon = String.valueOf(mCurrentLocation.getLongitude());
        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
    if (ContextCompat.checkSelfPermission(DatabaseActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||  ActivityCompat.checkSelfPermission(DatabaseActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
                {   ActivityCompat.requestPermissions(DatabaseActivity.this, PERMISSIONS_LOCATION, REQUEST_LOCATION); }
                PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(DatabaseActivity.this, " Access .... location", Toast.LENGTH_SHORT).show();
                    startLocationUpdates();
                } else {
                    Toast.makeText(DatabaseActivity.this, " Denied .... location", Toast.LENGTH_SHORT).show();
                }
                return;}
            case REQUEST_WRITE_EXTERNAL_STORAGE:{
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(DatabaseActivity.this, " Access ... memory write", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DatabaseActivity.this, " Denied ... memory write", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
