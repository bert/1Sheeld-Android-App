package com.integreight.onesheeld.adapters;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.integreight.onesheeld.R;
import com.integreight.onesheeld.UIShield;
import com.integreight.onesheeld.activities.MainActivity;

public class ShieldsListAdapter extends BaseAdapter {
	MainActivity activity;
	List<UIShield> shieldList;
	LayoutInflater inflater;
	
	

	public ShieldsListAdapter(Activity a) {
		this.activity = (MainActivity)a;
		this.shieldList = Arrays.asList(UIShield.values());
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return shieldList.size();
	}

	public UIShield getItem(int position) {
		return shieldList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		View row = convertView;
		Holder holder = null;
		if (row == null) {

			row = inflater.inflate(R.layout.shield_list_item, parent, false);

			holder = new Holder();
			holder.name = (TextView) row.findViewById(R.id.shield_list_item_name_textview);
			holder.icon = (ImageView) row.findViewById(R.id.shield_list_item_symbol_imageview);
			holder.selectionButton=(ToggleButton)row.findViewById(R.id.shield_list_item_selection_toggle_button);
			holder.selectionCircle=(ImageView) row.findViewById(R.id.shield_list_item_selection_circle_imageview);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		UIShield shield=shieldList.get(position);
		String name = shield.getName();
		Integer iconId = shield.getSymbolId();
		Integer imageId = shield.getMainImageStripId();

		holder.name.setText(name);
		holder.icon.setBackgroundResource(iconId);

		row.setBackgroundResource(imageId);
		
		if(shield.isMainActivitySelection()){
			holder.selectionButton.setChecked(true);
			holder.selectionButton.setVisibility(View.VISIBLE);
			holder.selectionCircle.setVisibility(View.VISIBLE);
		}
		else{
			holder.selectionButton.setChecked(false);
			holder.selectionButton.setVisibility(View.INVISIBLE);
			holder.selectionCircle.setVisibility(View.INVISIBLE);
		}
//		RelativeLayout.LayoutParams head_params = (RelativeLayout.LayoutParams)((RelativeLayout)row).getLayoutParams();
//		head_params.setMargins(0, -20, 0, 0); //substitute parameters for left, top, right, bottom
//		row.setLayoutParams(head_params);

		// row.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// //iv.animate();
		// // String url = data.get(position).getUrl();
		// // Intent intent = new Intent(context, Tutorial3Activity.class);
		// // intent.putExtra("itemId", data.get(position).getId());
		// // context.startActivity(intent);
		// }
		// });
		return row;
	}

	static class Holder {
		TextView name;
		ImageView icon;
		ToggleButton selectionButton;
		ImageView selectionCircle;
	}
	


}