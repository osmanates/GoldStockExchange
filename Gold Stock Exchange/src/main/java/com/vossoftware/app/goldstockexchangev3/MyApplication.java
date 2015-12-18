package com.vossoftware.app.goldstockexchangev3;

import android.app.Application;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Osman on 16.12.2015.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
// TODO Auto-generated method stub
        super.onCreate();
// Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);
    }
}