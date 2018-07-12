package com.example.dimitris.apothnarxh24;

/**
 * Created by dimitris on 8/7/2018.
 */


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import static java.lang.String.valueOf;

public class HolesActivity extends Activity{

    private DatabaseOpenHelper helper;
    private SimpleCursorAdapter mAdapter;
    ListView listView;
    TextView textView, textViewAx, textViewAxNext, textViewhHole;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.holes);

        listView = (ListView) findViewById(R.id.list);
        helper = new DatabaseOpenHelper(this);

        SQLiteDatabase db = helper.getReadableDatabase();
        String [] projection = {
                helper.id,
                helper.axe,
                helper.lat,
                helper.lon,
        };

        String selection = null;
        String [] selectionArgs = new String[]{} ;

        Cursor c = db.query(DatabaseOpenHelper.table,
                projection ,
                selection ,
                selectionArgs,
                null,
                null,
                null);
        mAdapter = new SimpleCursorAdapter(this, R.layout.list, c,
                DatabaseOpenHelper.columns, new int[] { R.id._id, R.id.value_axe , R.id.value_lat , R.id.value_lon  },
                0);
        listView.setAdapter(mAdapter);

        textView = findViewById(R.id.textView);
        textViewAx = findViewById(R.id.textViewAx);
        textViewAxNext = findViewById(R.id.textViewAxNext);
        textViewhHole = findViewById(R.id.textViewHole);

        String valueOfId,valueAx,valueAxNext,stringHole;
        double d,e,f;

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            valueOfId = c.getString(c.getColumnIndex(DatabaseOpenHelper.id));
            valueAx = c.getString(c.getColumnIndex(DatabaseOpenHelper.axe));
            valueAxNext = c.getString(c.getColumnIndex(DatabaseOpenHelper.axe));
            textView.setText(valueOfId);
            textViewAx.setText(valueAx);
            textViewAxNext.setText(valueAxNext);
            d = Double.parseDouble(valueAx);
            e = Double.parseDouble(valueAxNext);
            f = d - e + 23;
            stringHole = Double.toString(f);
            textViewhHole.setText(stringHole);
        }
    }

    public void selfDestruct(View view) {
        switch (view.getId()) {
            case R.id.button_gia_xarth:
                Intent i = new Intent(this, MapsActivity.class);
                startActivity(i);
                onPause();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}