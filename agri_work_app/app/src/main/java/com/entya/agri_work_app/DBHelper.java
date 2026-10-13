package com.entya.agri_work_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context) {
        super(context, "agri.db", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE work_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "date TEXT," +
                        "job TEXT," +
                        "memo TEXT," +
                        "image_path TEXT," +
                        "state TEXT)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try (android.database.Cursor cursor = db.rawQuery("PRAGMA table_info(work_history)", null)) {
                boolean hasImagePath = false;
                while (cursor.moveToNext()) {
                    if ("image_path".equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                        hasImagePath = true;
                    }
                }
                if (!hasImagePath) db.execSQL("ALTER TABLE work_history ADD COLUMN image_path TEXT");
            }
        }
    }
}