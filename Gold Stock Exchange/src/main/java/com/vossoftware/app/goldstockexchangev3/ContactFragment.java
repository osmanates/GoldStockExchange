package com.vossoftware.app.goldstockexchangev3;

/**
 * Created by Osman on 11.12.2015.
 */
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContactFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>{
    private CursorRecyclerAdapter cursorRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private MyCursorRecycleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Cursor cursor = getActivity().getContentResolver().query(DataProvider.CONTENT_URI_PROFILE, new String[] {DataProvider.COL_ID, DataProvider.COL_NAME, DataProvider.COL_PICTURE, DataProvider.COL_COUNT},null, null, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);


        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        //mAdapter = new MyAdapter(myDataset);
        mAdapter = new MyCursorRecycleAdapter(getContext(), cursor);
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setOnClickListener();
        //setListAdapter(adapter);

/*        gcmUtil = new GcmUtil(getApplicationContext());
        connect();*/

        getLoaderManager().initLoader(0, null, this);
        //getUniqueDeviceId(getContext());
    }

    public String getUniqueDeviceId(Context context){
        String uniqueId;
        if (android.os.Build.SERIAL != null || android.os.Build.SERIAL != "") {
            uniqueId = android.os.Build.SERIAL;
        }
        else {
            TelephonyManager mngr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            uniqueId = mngr.getDeviceId();
        }
        //Log.d("DeviceUniqueId= ", uniqueId);
        return uniqueId;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment, container, false);
/*        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            }
        });*/

        //GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getContext());
        //gcm.send();

        return view;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(),
                DataProvider.CONTENT_URI_PROFILE,
                new String[]{DataProvider.COL_ID, DataProvider.COL_NAME, DataProvider.COL_PICTURE, DataProvider.COL_COUNT},
                null,
                null,
                DataProvider.COL_COUNT + " DESC, " + DataProvider.COL_ID + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}