package com.example.dimitris.apothnarxh24;

/**
 * Created by dimitris on 8/7/2018.
 */


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by dimitris on 19/5/2017.
 */

public final class DatabaseOpenHelper extends SQLiteOpenHelper implements BaseColumns {

    final static String table = "value";
    final static String id = "_id";
    final static String axe = "value_axe";
    final static String lat = "value_lat";
    final static String lon = "value_lon";
    final static String[] columns = {id, axe , lat , lon};

    final private static String CREATE_CMD =

            "CREATE TABLE value ( " + id
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + axe + " INTEGER, "
                    + lat + " INTEGER, "
                    + lon + " INTEGER) ";

    final private static String NAME = "value.db";
    final private static Integer VERSION = 1;
    final private Context mContext;

    public DatabaseOpenHelper(Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CMD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // N/A
    }

    void deleteDatabase() {
        mContext.deleteDatabase(NAME);
    }
}
