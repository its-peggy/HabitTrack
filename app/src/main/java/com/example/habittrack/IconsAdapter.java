package com.example.habittrack;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class IconsAdapter extends BaseAdapter {

    private Context context;
    // TODO: how to not hardcode these
    public Integer[] icons = {
            R.drawable.icons8_abc_100, R.drawable.icons8_airplane_take_off_100,
            R.drawable.icons8_alarm_clock_100, R.drawable.icons8_alcoholic_cocktail_100,
            R.drawable.icons8_american_football_ball_100, R.drawable.icons8_apricot_100,
            R.drawable.icons8_around_the_globe_100, R.drawable.icons8_avocado_100,
            R.drawable.icons8_baby_bottle_100, R.drawable.icons8_bag_100,
            R.drawable.icons8_bank_cards_100, R.drawable.icons8_barbell_100
    };

    public IconsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int position) {
        return icons[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(icons[position]);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
        imageView.setPadding(4, 4, 4, 4);
        return imageView;
    }
}
