package com.lbsphoto.app.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lbsphoto.app.R;
import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.dbmanager.DBManager;
import com.lbsphoto.app.util.Bind;
import com.lbsphoto.app.util.PreferenceUtil;
import com.lbsphoto.app.util.ToastUtils;

public class LoginActivity extends BaseActivity implements OnClickListener{
	@Bind(R.id.login_user_text)
	private EditText userText;
	@Bind(R.id.login_password_text)
	private EditText passwordText;
	@Bind(R.id.login_user_avatar)
	private ImageView avatarIm;
	@Bind(R.id.login_mobile_text)
	private EditText mobileText;
	@Bind(R.id.login_btn)
	private TextView loginBtn;
	@Bind(R.id.login_register)
	private TextView registerBtn;
	@Bind(R.id.login_remember)
	private LinearLayout rememberLl;
	@Bind(R.id.login_remember_icon)
	private ImageView rememberIcon;
	private boolean isRemeber;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(userText.getText().toString().trim())) {
			if (!DBManager.getInstance(LoginActivity.this).isExistByName(userText.getText().toString())) {
				avatarIm.setImageResource(R.drawable.icon_avatar);
			} else {
				if (!TextUtils.isEmpty(LbsPhotoApplication.user.avatar)) {
					Bitmap mCoverBitmap = BitmapFactory.decodeFile(LbsPhotoApplication.user.avatar);
					avatarIm.setImageBitmap(mCoverBitmap);						
				} else {
					avatarIm.setImageResource(R.drawable.icon_avatar);
				}
			}
		}

		String name = PreferenceUtil.getLoginUser();
		if (TextUtils.isEmpty(name)) {
			rememberIcon.setImageResource(R.drawable.square_un_select);
			isRemeber = false;
		} else {
			rememberIcon.setImageResource(R.drawable.squre_select);
			isRemeber = true;
			userText.setText(name);
			passwordText.setText(PreferenceUtil.getLoginPass());
			startMusicActivity();
		}
	}

	@Override
	protected void setListener() {
		loginBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
		rememberLl.setOnClickListener(this);
		userText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				if (DBManager.getInstance(LoginActivity.this).isExistByName(arg0.toString())) {
					if (!TextUtils.isEmpty(LbsPhotoApplication.user.avatar)) {
						Bitmap mCoverBitmap = BitmapFactory.decodeFile(LbsPhotoApplication.user.avatar);
						avatarIm.setImageBitmap(mCoverBitmap);						
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if (!DBManager.getInstance(LoginActivity.this).isExistByName(arg0.toString())) {
					avatarIm.setImageResource(R.drawable.icon_avatar);
				}
			}
		});
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.login_register:
			Intent intent = new Intent();
	        intent.setClass(this, RegisterActivity.class);
	        intent.putExtras(getIntent());
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        startActivity(intent);
			break;

		case R.id.login_btn:
			String userName = userText.getText().toString().trim();
			if (TextUtils.isEmpty(userName)) {
				ToastUtils.show("昵称不能为空");
				return;
			}
			
			String password = passwordText.getText().toString().trim();
			if (TextUtils.isEmpty(password)) {
				ToastUtils.show("密码不能为空");
				return;
			}
			boolean isRegiter = DBManager.getInstance(LoginActivity.this).isExist(userName, password);
			if (isRegiter) {
				startMusicActivity();
			} else {
				ToastUtils.show("账号密码不正确");
			}
			break;

		case R.id.login_remember:
			isRemeber = !isRemeber;
			if (isRemeber) {
				rememberIcon.setImageResource(R.drawable.squre_select);
			} else {
				rememberIcon.setImageResource(R.drawable.square_un_select);
			}
			break;
		default:
			break;
		}
	}

	private void startMusicActivity() {
		if (isRemeber) {
			PreferenceUtil.saveLoginUser(userText.getText().toString().trim());
			PreferenceUtil.saveLoginPass(passwordText.getText().toString().trim());
		} else {
			PreferenceUtil.clearSp();
		}
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.putExtras(getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
	
}
