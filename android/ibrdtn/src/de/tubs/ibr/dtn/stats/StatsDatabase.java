package de.tubs.ibr.dtn.stats;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class StatsDatabase {
    private final static String TAG = "StatsDatabase";
    
    public final static String NOTIFY_DATABASE_UPDATED = "de.tubs.ibr.dtn.stats.DATABASE_UPDATED";
    
    private DBOpenHelper mHelper = null;
    private SQLiteDatabase mDatabase = null;
    private Context mContext = null;
    
    private static final String DATABASE_NAME = "stats";
    public static final String[] TABLE_NAMES = { "dtnd" };
    
    private static final int DATABASE_VERSION = 1;
    
    // Database creation sql statement
    private static final String DATABASE_CREATE_DTND = 
            "CREATE TABLE " + TABLE_NAMES[0] + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StatsEntry.TIMESTAMP + " TEXT" +
            ");";

    private class DBOpenHelper extends SQLiteOpenHelper {
        
        public DBOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_DTND);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DBOpenHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            
            for (String table : TABLE_NAMES) {
                db.execSQL("DROP TABLE IF EXISTS " + table);
            }
            onCreate(db);
        }
    };
    
    public StatsDatabase(Context context) throws SQLException
    {
        mHelper = new DBOpenHelper(context);
        mDatabase = mHelper.getWritableDatabase();
        mContext = context;
    }
    
    public void notifyDataChanged() {
        Intent i = new Intent(NOTIFY_DATABASE_UPDATED);
        mContext.sendBroadcast(i);
    }
    
    public StatsEntry get(Long id) {
        StatsEntry se = null;
        
        try {
            Cursor cur = mDatabase.query(StatsDatabase.TABLE_NAMES[0], StatsEntry.PROJECTION, StatsEntry.ID + " = ?", new String[] { id.toString() }, null, null, null, "0, 1");
            
            if (cur.moveToNext())
            {
                se = new StatsEntry(mContext, cur, new StatsEntry.ColumnsMap());
            }
            
            cur.close();
        } catch (Exception e) {
            // entry not found
            Log.e(TAG, "get() failed", e);
        }
        
        return se;
    }
    
    public Long put(StatsEntry e) {
        // TODO: store the stats object into the database
        
        ContentValues values = new ContentValues();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        values.put(StatsEntry.TIMESTAMP, dateFormat.format(e.getTimestamp()));
        
        // store the message in the database
        Long id = mDatabase.insert(StatsDatabase.TABLE_NAMES[0], null, values);
        
        notifyDataChanged();
        
        return id;
    }

    public void close()
    {
        mDatabase.close();
        mHelper.close();
    }
}
