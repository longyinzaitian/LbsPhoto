package com.lbsphoto.app.dbmanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.bean.User;

public class DBManager {
    private static DBManager dbManager;
    private Context context;
    private MyDBHelper myDBHelper;

    private DBManager(Context context) {
        this.context = context;
        myDBHelper = new MyDBHelper(context);
    }
    
    public MyDBHelper getDbHelper() {
		return myDBHelper;
	}

    public static DBManager getInstance(Context context) {
        if (null == dbManager) {
            dbManager = new DBManager(context);
        }
        return dbManager;
    }

    public void execSql(String sql) {
    	SQLiteDatabase db = null;
    	if (myDBHelper != null) {
			db = myDBHelper.getWritableDatabase();
		}
        if (null != db && !TextUtils.isEmpty(sql)) {
            db.execSQL(sql);
            db.close();
        }
    }
    
    public void deleteUser(String userName) {
    	getDbHelper().getWritableDatabase().delete(MyDBHelper.T_NAME, "name=?", new String[]{userName});
    }
    
    public void insertUser(String userName, String password, int sexIndex, String mobile, String email, String avatarStr) {
    	String sql = "insert into " + MyDBHelper.T_NAME + "(name, password, sex, mobile, email, avatar) values(\'"
    			+ userName + "\', \'" + password + "\', " + sexIndex + ", \'" + mobile + "\', \'" + email + "\', \'" + avatarStr + "\');";
    	execSql(sql);
    }
    
    public boolean isExist(String userName, String password) {
    	MyDBHelper myDBHelper = getDbHelper();
		Cursor cursor = myDBHelper.getWritableDatabase().query(MyDBHelper.T_NAME, null, null, null, null, null, null);
		boolean isRegiter = false;
		if (cursor != null && cursor.getCount()>0) {
			while (cursor.moveToNext()) {
				String nameCol = cursor.getString(cursor.getColumnIndex("name"));
				String mobileCol = cursor.getString(cursor.getColumnIndex("mobile"));
				String emailCol = cursor.getString(cursor.getColumnIndex("email"));
				String passwordCol = cursor.getString(cursor.getColumnIndex("password"));
				int sexCol = cursor.getInt(cursor.getColumnIndex("sex"));
				String avatarCol = cursor.getString(cursor.getColumnIndex("avatar"));
				if (nameCol.equals(userName)
						&& password.equals(passwordCol)) {
					isRegiter = true;
					User user = new User();
					user.name = nameCol;
					user.mobile = mobileCol;
					user.email = emailCol;
					user.sex = sexCol;
					user.avatar = avatarCol;
					user.password = passwordCol;
					LbsPhotoApplication.user = user;
					break;
				}
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return isRegiter;
    }
    
    public boolean isExistByName(String userName) {
    	MyDBHelper myDBHelper = getDbHelper();
		Cursor cursor = myDBHelper.getWritableDatabase().query(MyDBHelper.T_NAME, null, null, null, null, null, null);
		boolean isRegiter = false;
		if (cursor != null && cursor.getCount()>0) {
			while (cursor.moveToNext()) {
				String nameCol = cursor.getString(cursor.getColumnIndex("name"));
				String mobileCol = cursor.getString(cursor.getColumnIndex("mobile"));
				String emailCol = cursor.getString(cursor.getColumnIndex("email"));
				String passwordCol = cursor.getString(cursor.getColumnIndex("password"));
				int sexCol = cursor.getInt(cursor.getColumnIndex("sex"));
				String avatarCol = cursor.getString(cursor.getColumnIndex("avatar"));
				if (nameCol.equals(userName)) {
					isRegiter = true;
					User user = new User();
					user.name = nameCol;
					user.mobile = mobileCol;
					user.email = emailCol;
					user.sex = sexCol;
					user.avatar = avatarCol;
					user.password = passwordCol;
					LbsPhotoApplication.user = user;
					break;
				}
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return isRegiter;
    }
}
