package com.example.habittrack;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class IconsAdapter extends BaseAdapter {

    public static final String TAG = "IconsAdapter";

    private Context context;
    private List<Bitmap> iconBitmaps = new ArrayList<>();

    private void getIconBitmaps() throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] iconPaths = assetManager.list("icons");
        for (int i = 0; i < iconPaths.length; i++) {
            InputStream inputStream = assetManager.open("icons/" + iconPaths[i]);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            iconBitmaps.add(bitmap);
        }
    }

    public IconsAdapter(Context context) {
        this.context = context;
        try {
            getIconBitmaps();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return iconBitmaps.size();
    }

    @Override
    public Object getItem(int position) {
        return iconBitmaps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(iconBitmaps.get(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
        imageView.setPadding(4, 4, 4, 4);
        return imageView;
    }
}
