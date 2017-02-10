package com.example.peter.berryestimator;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;
    private Gson gson;

    // TODO: try contentObserver
    public DBManager(Context context) {
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    public void addList(List<ImageRecord> imageRecordList) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();

            for (ImageRecord imageRecord : imageRecordList) {
                values.put(DBHelper.COLUMN_DATA, gson.toJson(imageRecord));
                db.insert(DBHelper.TABLE_IMAGE_RECORD, null, values);
                values.clear();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // return recordId
    public String insert(ImageRecord imageRecord, String imageString){
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_IMAGE, imageString);

        //TODO: try to reduce time cost, update cost twice as insert do
        // return row id
        long rowId = db.insert(DBHelper.TABLE_IMAGE_RECORD, null, values);

        values.clear();
        imageRecord.setRecordId(rowId + "");
        values.put(DBHelper.COLUMN_RECORD_ID, imageRecord.getRecordId());
        values.put(DBHelper.COLUMN_DATA, gson.toJson(imageRecord));

        db.update(DBHelper.TABLE_IMAGE_RECORD, values, DBHelper.COLUMN_ID + " = " + rowId, null);

        return imageRecord.getRecordId();
    }

    // set param to null when it should not be set
    public void update(ImageRecord imageRecord, String imageString, String densityImageString) {
        ContentValues values = new ContentValues();

        values.put(DBHelper.COLUMN_DATA, gson.toJson(imageRecord));
        if (imageString != null) {
            values.put(DBHelper.COLUMN_IMAGE, imageString);
        }
        if (densityImageString!= null) {
            values.put(DBHelper.COLUMN_DENSITY_IMAGE, densityImageString);
        }

        db.update(DBHelper.TABLE_IMAGE_RECORD, values, DBHelper.COLUMN_RECORD_ID + " = " + imageRecord.getRecordId(), null);
    }

    public void delete(ImageRecord imageRecord) {
        db.delete(DBHelper.TABLE_IMAGE_RECORD, DBHelper.COLUMN_RECORD_ID + " = " + imageRecord.getRecordId(), null);
    }

    public void deleteAll() {
        db.delete(DBHelper.TABLE_IMAGE_RECORD, null, null);
    }

    public Cursor findRowCursor(ImageRecord imageRecord) {
        String sql = "SELECT * FROM " + DBHelper.TABLE_IMAGE_RECORD
                + " WHERE " + DBHelper.COLUMN_RECORD_ID + " = " + imageRecord.getRecordId();
        return db.rawQuery(sql, null);
    }

    public Cursor getQueryCursor(){
        return db.rawQuery("SELECT * FROM " + DBHelper.TABLE_IMAGE_RECORD, null);
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}
