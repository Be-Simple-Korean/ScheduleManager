package com.example.schedulemanager.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite 테이블 생성, 버전 업그레이드 처리
 */
class DBHelper(context: Context, dbName:String):SQLiteOpenHelper(context,dbName,null,4){
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table calendar(" +
                "id integer primary key AUTOINCREMENT,"+
                "title varchar(30) not null," +
                "date varchar(15) not null,"+ //yyyy.m.d
                "time varchar(10) default \'\',"+ //h:m
                "place varchar(500) default \'\',"+
                "placeX varchar(100) default \'\',"+
                "placeY varchar(100) default \'\',"+
                "contents varchar(500) default \'\',"+
                "alarmTime varchar(5) default \'\'"+
                ");")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS calendar")
        onCreate(db)
    }

}