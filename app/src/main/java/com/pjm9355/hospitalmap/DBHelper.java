package com.pjm9355.hospitalmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE if not exists tb_hospbasislist ("
                + "_id integer primary key autoincrement,"
                + "addr text,"
                + "clCdNm text,"
                + "drTotCnt integer,"
                + "estbDd integer,"
                + "gdrCnt integer,"
                + "hospUrl text,"
                + "intnCnt integer,"
                + "resdntCnt integer,"
                + "sdrCnt integer,"
                + "telno text,"
                + "XPos double,"
                + "YPos double,"
                + "yadmNm text);";
        db.execSQL(sql);

        String sql_dgsbjt = "CREATE TABLE if not exists tb_dgsbjt ("
                + "_id integer primary key autoincrement,"
                + "ykiho text,"
                + "dgsbjtCd text,"
                + "dgsbjtCdNm text,"
                + "dgsbjtPrSdrCnt integer,"
                + "cdiagDrCnt integer);";
        db.execSQL(sql_dgsbjt);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE if exists hospitaldb";
        db.execSQL(sql);
        onCreate(db);
    }
}
