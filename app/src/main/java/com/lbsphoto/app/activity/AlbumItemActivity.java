package com.lbsphoto.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.lbsphoto.app.R;
import com.lbsphoto.app.adapter.AlbumItemAdapter;
import com.lbsphoto.app.bean.PhotoUpImageBucket;
import com.lbsphoto.app.bean.PhotoUpImageItem;

import java.util.ArrayList;

/**
 * @author lbsphoto
 */
public class AlbumItemActivity extends BaseActivity implements OnClickListener {

	private GridView gridView;
	private TextView back,ok;
	private PhotoUpImageBucket photoUpImageBucket;
	private ArrayList<PhotoUpImageItem> selectImages;
	private AlbumItemAdapter adapter;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_item_images);
		init();
		setListener();
	}
	private void init() {
		gridView = (GridView) findViewById(R.id.album_item_gridv);
		back = (TextView) findViewById(R.id.back);
		ok = (TextView) findViewById(R.id.sure);
		selectImages = new ArrayList<PhotoUpImageItem>();
		
		Intent intent = getIntent();
		photoUpImageBucket = (PhotoUpImageBucket) intent.getParcelableExtra("imagelist");
		adapter = new AlbumItemAdapter(photoUpImageBucket.getImageList(), AlbumItemActivity.this);
		gridView.setAdapter(adapter);
	}

	@Override
	protected void setListener() {
		back.setOnClickListener(this);
		ok.setOnClickListener(this);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(AlbumItemActivity.this, ImageInfoActivity.class);
				intent.putExtra("path", photoUpImageBucket.getImageList().get(position).getImagePath());
				startActivity(intent);
			}
		});

		findViewById(R.id.image_item_return).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.sure:
			Intent intent = new Intent(AlbumItemActivity.this,SelectedImagesActivity.class);
			intent.putExtra("selectIma", selectImages);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

}
