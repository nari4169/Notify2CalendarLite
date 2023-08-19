package com.nari.notify2calendar.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nari.notify2calendar.R;

import java.util.ArrayList;

public class ViewStrValueAdapter extends BaseAdapter {

    private ArrayList<ViewStrValue> listViewItemList = new ArrayList<ViewStrValue>() ;

    public ViewStrValueAdapter(ArrayList<ViewStrValue> oDate) {
        super();
        listViewItemList = oDate ;
    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.viewstrvalue_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        //ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView textString = (TextView) convertView.findViewById(R.id.textStrBody) ;
        //TextView descTextView = (TextView) convertView.findViewById(R.id.textView2) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ViewStrValue listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        //iconImageView.setImageDrawable(listViewItem.getIcon());
        textString.setText(listViewItem.getStrValue());
        //descTextView.setText(listViewItem.getDesc());

        return convertView;
    }

}
