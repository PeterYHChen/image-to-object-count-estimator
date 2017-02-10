package com.example.peter.berryestimator;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "image_record_list.db";
    public static final String TABLE_IMAGE_RECORD = "imageRecord";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RECORD_ID = "recordId";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_DENSITY_IMAGE = "densityImage";

    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    // TODO: add user associate with record table
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_IMAGE_RECORD +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "recordId TEXT," +
                "data TEXT," +
                "image TEXT," +
                "densityImage TEXT)");
    }

    //if version is changed, the db system will upgrade automatically
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE_RECORD);
        onCreate(db);
    }
}
