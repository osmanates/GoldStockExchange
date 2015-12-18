package com.vossoftware.app.goldstockexchangev3;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Osman on 18.12.2015.
 */
public class Util {
    public static String getUniqueDeviceId(Context context){
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
}
