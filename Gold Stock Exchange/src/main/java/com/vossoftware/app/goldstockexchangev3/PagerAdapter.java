package com.vossoftware.app.goldstockexchangev3;

/**
 * Created by Osman on 11.12.2015.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ContactFragment tab1 = new ContactFragment();
                return tab1;
            case 1:
                MessageFragment tab2 = new MessageFragment();
                return tab2;
            case 2:
                ArchiveFragment tab3 = new ArchiveFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}