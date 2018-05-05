package com.lbsphoto.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.lbsphoto.app.R;
import com.lbsphoto.app.adapter.AlbumsAdapter;
import com.lbsphoto.app.bean.PhotoUpImageBucket;
import com.lbsphoto.app.util.PhotoUpAlbumHelper;

import java.util.List;

public class AlbumsActivity extends BaseActivity {

	private GridView gridView;
	private AlbumsAdapter adapter;
	private PhotoUpAlbumHelper photoUpAlbumHelper;
	private List<PhotoUpImageBucket> list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.albums_gridview);
		init();
		loadData();
		onItemClick();
	}
	private void init(){
		gridView = (GridView) findViewById(R.id.album_gridv);
		adapter = new AlbumsAdapter(AlbumsActivity.this);
		gridView.setAdapter(adapter);
	}
	
	private void loadData(){
		photoUpAlbumHelper = PhotoUpAlbumHelper.getHelper();
		photoUpAlbumHelper.init(AlbumsActivity.this);
		photoUpAlbumHelper.setGetAlbumList(new PhotoUpAlbumHelper.GetAlbumList() {
			@Override
			public void getAlbumList(List<PhotoUpImageBucket> list) {
				adapter.setArrayList(list);
				adapter.notifyDataSetChanged();
				AlbumsActivity.this.list = list;
			}
		});
		photoUpAlbumHelper.execute(false);
	}
	
	private void onItemClick(){
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(AlbumsActivity.this,AlbumItemActivity.class);
				intent.putExtra("imagelist", list.get(position));
				startActivity(intent);
			}
		});

		findViewById(R.id.image_item_return).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
