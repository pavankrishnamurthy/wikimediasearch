package com.pa.eg.wikimediasearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pavan on 12/26/15.
 */
public class ImageViewAdapter extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String, String>> urlList;
    private LayoutInflater inflater;

    public ImageViewAdapter (Context context, ArrayList<HashMap<String, String>> pageList) {
        super();
        this.context = context;
        this.urlList = pageList;
        this.inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public int getCount() {
        return urlList.size();
    }

    public HashMap<String, String> getItem(int position) {
        return urlList.get(position);
    }

    public long getItemId(int position) {
        return urlList.get(position).hashCode();
    }

    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = inflater.inflate(R.layout.listitem,null);
            HashMap<String, String> pageMap = urlList.get(position);
            String pageId, url, pageTitle;
            url = pageMap.get("imageSource");
            pageId = pageMap.get("pageid");
            pageTitle = pageMap.get("title");

            ImageView iv = (ImageView)view.findViewById(R.id.imgview);
            Picasso.with(context).load(url).into(iv);

            TextView tvPageid = (TextView) view.findViewById(R.id.pageid);
            tvPageid.setText(pageId);

            TextView tvTitle = (TextView) view.findViewById(R.id.pagetitle);
            tvTitle.setText(pageTitle);
        }
        return view;
    }
}
