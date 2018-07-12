package com.example.dimitris.apothnarxh24;

/**
 * Created by dimitris on 8/7/2018.
 */


import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseOpenHelper helper;
    Cursor c , cc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney;
        String a , b , f , h ;
        double d , e, g , i , j , s , t , x , y , w , k ;

        helper = new DatabaseOpenHelper(this);

        c = helper.getReadableDatabase().query(DatabaseOpenHelper.table,
                DatabaseOpenHelper.columns, null, new String[] {}, null, null,
                null);

        cc = helper.getReadableDatabase().query(DatabaseOpenHelper.table,
                DatabaseOpenHelper.columns, null, new String[] {}, null, null,
                null);

        k = 0 ;
        cc.moveToFirst();

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            cc.moveToNext();
            if ( !cc.isAfterLast() ){
                x = 0 + k ;
                y = 0 + k ;
                s = 5 ;
                t = 8 ;
                w = t - s ;
                a = c.getString(c.getColumnIndex(DatabaseOpenHelper.lat));
                b = c.getString(c.getColumnIndex(DatabaseOpenHelper.lon));
                if( k < 7 ) {
                    k = k + 1;
                }
                else {
                    //a = c.getString(c.getColumnIndex(DatabaseOpenHelper.lat));
                    //b = c.getString(c.getColumnIndex(DatabaseOpenHelper.lon));
                    d = Double.parseDouble(a);
                    e = Double.parseDouble(b);

                    f = c.getString(c.getColumnIndex(DatabaseOpenHelper.axe));
                    g = Double.parseDouble(f);

                    h = cc.getString(cc.getColumnIndex(DatabaseOpenHelper.axe));
                    i = Double.parseDouble(h);
                    j = i - g ;

                    sydney = new LatLng( d , e );
                    mMap.addMarker(new MarkerOptions().position(sydney).title( a + "," + b + "," + j ));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        helper.getWritableDatabase().close();
        helper.deleteDatabase();
        super.onDestroy();
    }
}
