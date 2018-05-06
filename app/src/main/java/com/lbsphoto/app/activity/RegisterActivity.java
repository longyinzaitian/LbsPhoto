package com.lbsphoto.app.activity;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.lbsphoto.app.R;
import com.lbsphoto.app.application.RequestCode;
import com.lbsphoto.app.dbmanager.DBManager;
import com.lbsphoto.app.dbmanager.MyDBHelper;
import com.lbsphoto.app.util.Bind;
import com.lbsphoto.app.util.CoverLoader;
import com.lbsphoto.app.util.FileUtils;
import com.lbsphoto.app.util.PermissionReq;
import com.lbsphoto.app.util.ToastUtils;

public class RegisterActivity extends BaseActivity implements OnClickListener {
	@Bind(R.id.register_user_avatar)
	private ImageView avatar;
	@Bind(R.id.register_user_text)
	private EditText nameText;
	@Bind(R.id.register_password_text)
	private EditText passwordText;
	@Bind(R.id.register_mobile_text)
	private EditText mobileText;
	@Bind(R.id.register_email_text)
	private EditText emailText;
	@Bind(R.id.sex_radio_group)
	private RadioGroup sexRadioGroup;
	@Bind(R.id.man_radio)
	private RadioButton manRadio;
	@Bind(R.id.woman_radio)
	private RadioButton womanRadio;
	@Bind(R.id.register_btn)
	private TextView registerBtn;
	private int sexIndex = 1;
	private DBManager dbManager;
	private String avatarStr = "";
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		dbManager = DBManager.getInstance(RegisterActivity.this);
	}

	@Override
	protected void setListener() {
		sexRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				switch (arg0.getId()) {
				case R.id.man_radio:
					manRadio.setChecked(true);
					manRadio.setTextColor(getResources().getColor(R.color.color_blue));
					womanRadio.setTextColor(getResources().getColor(R.color.grey));
					sexIndex = 1;
					break;
					
				case R.id.woman_radio:
					womanRadio.setChecked(true);
					manRadio.setTextColor(getResources().getColor(R.color.grey));
					womanRadio.setTextColor(getResources().getColor(R.color.color_blue));
					sexIndex = 2;
					break;

				default:
					break;
				}
			}
		});
		
		registerBtn.setOnClickListener(this);
		avatar.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.register_btn:
			String userName = nameText.getText().toString().trim();
			if (TextUtils.isEmpty(userName)) {
				ToastUtils.show("昵称不能为空");
				return;
			}
			
			String password = passwordText.getText().toString().trim();
			if (TextUtils.isEmpty(password)) {
				ToastUtils.show("密码不能为空");
				return;
			}
			
			String mobile = mobileText.getText().toString().trim();
			if (TextUtils.isEmpty(mobile)) {
				ToastUtils.show("手机号不能为空");
				return;
			}
			
			if (!mobile.matches("1[0-9]{10}")) {
				ToastUtils.show("手机号格式不正确");
				return;
			}
			
			String email = emailText.getText().toString().trim();
			if (TextUtils.isEmpty(email)) {
				ToastUtils.show("邮箱不能为空");
				return;
			}
			
			if (!email.matches("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$")) {
				ToastUtils.show("邮箱格式不正确");
				return;
			}
			
			
			String sql = "insert into " + MyDBHelper.T_NAME + "(name, password, sex, mobile, email, avatar) values(\'"
			+ userName + "\', \'" + password + "\', " + sexIndex + ", \'" + mobile + "\', \'" + email + "\', \'" + avatarStr + "\');";
			dbManager.execSql(sql);
			
			if (dbManager.isExist(userName, password)) {
				dbManager.deleteUser(userName);
			}
			
			dbManager.insertUser(userName, password, sexIndex, mobile, email, avatarStr);
			finish();
			break;
			
		case R.id.register_user_avatar:
			PermissionReq.with(RegisterActivity.this)
            .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .result(new PermissionReq.Result() {
                @Override
                public void onGranted() {
                	Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, RequestCode.REQUEST_ALBUM);
                }
                @Override
                public void onDenied() {
                    ToastUtils.show(R.string.no_permission_select_image);
                }
            })
            .request();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == RequestCode.REQUEST_ALBUM && data != null) {
        	Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(data.getData(), "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", CoverLoader.THUMBNAIL_MAX_LENGTH);
            intent.putExtra("outputY", CoverLoader.THUMBNAIL_MAX_LENGTH);
            intent.putExtra("return-data", false);
            File outFile = new File(FileUtils.getAvatarImagePath(RegisterActivity.this));
            Uri outUri = Uri.fromFile(outFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            // 取消人脸识别
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, RequestCode.REQUEST_CORP);
        } else if (requestCode == RequestCode.REQUEST_CORP) {
            File avatarFile = new File(FileUtils.getAvatarImagePath(RegisterActivity.this));
            if (!avatarFile.exists()) {
                ToastUtils.show("图片保存失败");
                return;
            }

            Bitmap mCoverBitmap = BitmapFactory.decodeFile(avatarFile.getPath());
            avatar.setImageBitmap(mCoverBitmap);
            avatarStr = avatarFile.getPath();
            }
    }
	

}
