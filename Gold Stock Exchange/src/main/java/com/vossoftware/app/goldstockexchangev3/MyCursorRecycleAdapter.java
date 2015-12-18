package com.vossoftware.app.goldstockexchangev3;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Osman on 10.12.2015.
 */
public class MyCursorRecycleAdapter extends CursorRecyclerAdapter<MyCursorRecycleAdapter.CustomViewHolder> {
    private  Context context;
    private Cursor cursor;
    ImageLoader imageLoader;
    public MyCursorRecycleAdapter(Context context, Cursor cursor) {
        super(cursor);
        this.cursor = cursor;
        this.context = context;
        imageLoader = ImageLoader.getInstance(); // Get singleton instance
        //imageLoader.init(ImageLoaderConfiguration.createDefault(context));

    }

    @Override
    public void onBindViewHolderCursor(CustomViewHolder holder, final Cursor cursor) {
        Log.d("onbindviewholdercursor ", "icinde");
        String name = cursor.getString(cursor.getColumnIndex(DataProvider.COL_NAME));
        String profid = cursor.getString(cursor.getColumnIndex(DataProvider.COL_ID));
        String profPicture = cursor.getString(cursor.getColumnIndex(DataProvider.COL_PICTURE));

        // Load image, decode it to Bitmap and display Bitmap in ImageView (or any other view
        //  which implements ImageAware interface)
        imageLoader.displayImage(profPicture, holder.avatar);
        int count = cursor.getInt(cursor.getColumnIndex(DataProvider.COL_COUNT));

        //holder.avatar.(profPicture);
        holder.text1.setText(name);
        holder.text3.setText(String.format("%d new message%s", count, count==1 ? "" : "s"));
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(context, ChatActivity.class);
                //intent.putExtra(Common.PROFILE_ID, profid);
                //context.startActivity(intent);
            }
        };
        holder.text3.setOnClickListener(clickListener);

    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Log.d("oncreateviewholder ", "icinde");
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.main_list_item, viewGroup, false);
        // set the view's size, margins, paddings and layout parameters
        CustomViewHolder vh = new CustomViewHolder(v);
        return vh;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView avatar;
        protected TextView text1;
        protected TextView text3;
        protected TextView textEmail;


        public CustomViewHolder(View view) {
            super(view);
            this.avatar = (ImageView) view.findViewById(R.id.avatar);
            this.text1 = (TextView) view.findViewById(R.id.text1);
            this.text3 = (TextView) view.findViewById(R.id.text3);
        }
    }
}
