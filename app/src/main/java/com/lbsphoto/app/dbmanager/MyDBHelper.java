package com.lbsphoto.app.dbmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDBHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "music";
    private final static int DB_VERSION = 1;
    public final static String T_NAME = "user";
    public final static String TABLE_ID = "_id";
    public final static String TABLE_NAME = "name";
    public final static String TABLE_SEX = "sex";
    public final static String TABLE_PASSWORD = "password";
    public final static String TABLE_MOBILE = "mobile";
    public final static String TABLE_EMAIL = "email";
    public final static String TABLE_AVATAR = "avatar";
    

    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //定义参数简单地构造方法
    public MyDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //数据库版本发生更新时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //更新数据库
    }

    //打开数据库时调用
    @Override
    public void onOpen(SQLiteDatabase db) {
        //如果数据库已经存在，则再次运行不执行onCreate()方法，而是执行onOpen()打开数据库
        super.onOpen(db);
    } 

	@Override
	public void onCreate(SQLiteDatabase db) {
		//如果写有创建表的Sql语句时，就会在创建数据的时候创建对应的表格
        String sql = "create table if not exists " + T_NAME + "("
                + TABLE_ID + " Integer primary key AUTOINCREMENT, "
                + TABLE_NAME + " TEXT, "
                + TABLE_PASSWORD + " TEXT, "
                + TABLE_MOBILE + " TEXT, "
                + TABLE_EMAIL + " TEXT, "
                + TABLE_SEX + " INTEGER, "
                + TABLE_AVATAR + " TEXT"
                + ");";
        //执行创建表格语句
        db.execSQL(sql);
	}
}
