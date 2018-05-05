package com.lbsphoto.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lbsphoto.app.R;
import com.lbsphoto.app.application.LbsPhotoApplication;
import com.lbsphoto.app.bean.PhotoUpImageItem;

import java.util.ArrayList;

public class SelectedImagesAdapter extends BaseAdapter {

	private ArrayList<PhotoUpImageItem> arrayList;
	private LayoutInflater layoutInflater;
	private Context mContext;

	public SelectedImagesAdapter(Context context,ArrayList<PhotoUpImageItem> arrayList){
		this.arrayList = arrayList;
		layoutInflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		return arrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return arrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.selected_images_adapter_item, parent, false);
			holder = new Holder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.selected_image_item);
			convertView.setTag(holder);
		}else {
			holder = (Holder) convertView.getTag();
		}

		Glide.with(LbsPhotoApplication.getAppContext())
				.load("file://"+arrayList.get(position).getImagePath())
				.placeholder(R.drawable.album_default_loading_pic)
				.into(holder.imageView);
		return convertView;
	}
	class Holder{
		ImageView imageView;
	}
}