package com.vossoftware.app.goldstockexchangev3;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};


    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.v(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            if (!sharedPreferences.getString(Constants.TOKEN, "").equals(token) || !sharedPreferences.getString(Constants.TOKEN, "").equals("")) sendRegistrationToServer(token);
            GcmPubSub.getInstance(this).subscribe(token, "/topics/newclient", null);

            // Subscribe to topic channels
            //subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.SENT_TOKEN_TO_SERVER, true);
            editor.putString(Constants.TOKEN, token);
            editor.apply();
            Log.d(TAG, " token on save = " + sharedPreferences.getString(Constants.TOKEN, ""));
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        //Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Add custom implementation, as needed.
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        final String senderId = getResources().getString(R.string.sender_id);
        String msg = "";
        Bundle data = new Bundle();
        data.putString("action", Constants.ACTION_REGISTER);
        data.putString(Constants.REGISTRATION_TOKEN, token);
        data.putString(Constants.DEVICE_UNIQUE_ID, Util.getUniqueDeviceId(this));
        data.putString(Constants.DISPLAY_NAME, sharedPreferences.getString(Constants.DISPLAY_NAME, "Kişi"));
        data.putString(Constants.PICTURE_URL, sharedPreferences.getString(Constants.PICTURE_URL, ""));
        try {
            gcm.send(senderId + "@gcm.googleapis.com", String.valueOf(System.currentTimeMillis()), data);
            msg = "Sent message";
            sharedPreferences.edit().putString(Constants.DEVICE_UNIQUE_ID, Util.getUniqueDeviceId(this)).apply();
        } catch (IOException e) {

            e.printStackTrace();
            msg = "Error :" + e.getMessage();
        }
        Log.d("Message= ", msg);
    }
    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    /**
     * Get device unique id doesnt change even after a factory reset and/or installing a custom rom
     * @param context
     * @return
     */
}