package com.example.schedulemanager.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.schedulemanager.viewmodel.DataBaseType
import com.example.schedulemanager.viewmodel.MyViewModel

/**
 * DataBase CRUD 처리 클래스
 */
object DBManager {
    private lateinit var db: SQLiteDatabase
    private lateinit var cursor:Cursor

    fun getId(title:String, date:String, time:String, place:String, contents:String, viewModel: MyViewModel): Int {
        val sql = "select * from calendar where title ='"+title+"' and date = '"+date+"' and time = '"+time+"' and " +
                "place = '"+place+"' and contents= '"+contents+"'"
        Log.e("sql",sql)
        db=viewModel.getDatabase(DataBaseType.READ)
        
        val cursor=db.rawQuery(sql,null)
        Log.e("cursor.count",cursor.count.toString())
        var id=-1
        if(cursor!=null) {
            while (cursor.moveToNext()){
                id=cursor.getInt(0)
            }
        }
       return id
    }

    /**
     * 데이터베이스 Select
     */
    fun select(sql: String, viewModel: MyViewModel):Cursor{
        db = viewModel.getDatabase(DataBaseType.READ)
        cursor = db.rawQuery(sql, null)
        return cursor
    }

    /**
     * 데이터베이스 Insert
     */
    fun insert(sql:String, viewModel: MyViewModel){
        db=viewModel.getDatabase(DataBaseType.WRITE)
        db.execSQL(sql)
    }

    /**
     * 데이터베이스 Delete
     */
    fun delete(sql:String, viewModel: MyViewModel){
        db=viewModel.getDatabase(DataBaseType.WRITE)
        db.execSQL(sql)
    }

    /**
     * 데이터베이스 Update
     */
    fun update(sql:String, viewModel: MyViewModel){
        db=viewModel.getDatabase(DataBaseType.WRITE)
        db.execSQL(sql)
    }

}