package com.vossoftware.app.goldstockexchangev3;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Osman on 12.12.2015.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String action = data.getString("action");
        Log.d(TAG, "Action: " + action);
        Log.d(TAG, "From: " + from);
        //Log.d(TAG, "Message: " + message);

        switch (action) {
            case Constants.ACTION_SEND_CLIENT_LIST:
                Log.d("Prefence token once= ", sharedPreferences.getString(Constants.TOKEN, ""));

                try {
                    final ArrayList<Messager> messagers = getMessagers(data);
                    for (int i = 0; i < messagers.size(); i++) {
                        Cursor cursor = getContentResolver().query(DataProvider.CONTENT_URI_PROFILE, new String[] {DataProvider.COL_ID,
                                        DataProvider.COL_NAME, DataProvider.COL_DEV_ID, DataProvider.COL_PICTURE, DataProvider.COL_COUNT},
                                DataProvider.COL_DEV_ID + "='" + messagers.get(i).getDeviceUniqueID() + "'", null, null);
                        Log.d("Prefence token= ", sharedPreferences.getString(Constants.TOKEN, ""));
                        Log.d("Send client token= ", messagers.get(i).getRegistrationToken());
                        /*if (getUniqueDeviceId(this).equals(messagers.get(i).getDeviceUniqueID())) {
                            Log.d("tokens" , " same");
                            break;
                        }*/ /*else {
                            Log.d("tokens" , " not same");
                            Log.d("token in else = ", sharedPreferences.getString(Constants.TOKEN, ""));
                            Log.d("client token in else= ", messagers.get(i).getRegistrationToken());
                        }*/
                        if (cursor.getCount() == 0) {
                            ContentValues values = new ContentValues();
                            values.put(DataProvider.COL_NAME, messagers.get(i).getName());
                            values.put(DataProvider.COL_REGID, messagers.get(i).getRegistrationToken());
                            values.put(DataProvider.COL_DEV_ID, messagers.get(i).getDeviceUniqueID());
                            values.put(DataProvider.COL_PICTURE, messagers.get(i).getPictureUrl());
                            values.put(DataProvider.COL_ISGROUP, 0);
                            this.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
                        } else {
                            getContentResolver().delete(DataProvider.CONTENT_URI_PROFILE, DataProvider.COL_DEV_ID + "='" + messagers.get(i).getDeviceUniqueID() + "'", null);
                            ContentValues values = new ContentValues();
                            values.put(DataProvider.COL_NAME, messagers.get(i).getName());
                            values.put(DataProvider.COL_REGID, messagers.get(i).getRegistrationToken());
                            values.put(DataProvider.COL_DEV_ID, messagers.get(i).getDeviceUniqueID());
                            values.put(DataProvider.COL_PICTURE, messagers.get(i).getPictureUrl());
                            values.put(DataProvider.COL_ISGROUP, 0);
                            this.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //broadcastIntent.putParcelableArrayListExtra(IntentExtras.PINGERS, pingers);
                break;
            case Constants.ACTION_BROADCAST_NEW_CLIENT:
                try {
                    Messager newMessager = getNewMessager(data);
                    Cursor cursor = getContentResolver().query(DataProvider.CONTENT_URI_PROFILE, new String[] {DataProvider.COL_ID,
                                    DataProvider.COL_NAME, DataProvider.COL_DEV_ID, DataProvider.COL_PICTURE, DataProvider.COL_COUNT},
                            DataProvider.COL_DEV_ID + "='" + newMessager.getDeviceUniqueID() + "'", null, null);
                    Log.d("Prefence token= ", sharedPreferences.getString(Constants.TOKEN, ""));
                    Log.d("Send client token= ", newMessager.getRegistrationToken());
                    if (Util.getUniqueDeviceId(this).equals(newMessager.getDeviceUniqueID())) {
                        break;
                    }
                    if (cursor.getCount() == 0) {
                        ContentValues values = new ContentValues();
                        values.put(DataProvider.COL_NAME, newMessager.getName());
                        values.put(DataProvider.COL_REGID, newMessager.getRegistrationToken());
                        values.put(DataProvider.COL_DEV_ID, newMessager.getDeviceUniqueID());
                        values.put(DataProvider.COL_PICTURE, newMessager.getPictureUrl());
                        values.put(DataProvider.COL_ISGROUP, 0);
                        this.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
                    } else {
                        getContentResolver().delete(DataProvider.CONTENT_URI_PROFILE, DataProvider.COL_DEV_ID + "='" + newMessager.getDeviceUniqueID() + "'", null);
                        ContentValues values = new ContentValues();
                        values.put(DataProvider.COL_NAME, newMessager.getName());
                        values.put(DataProvider.COL_REGID, newMessager.getRegistrationToken());
                        values.put(DataProvider.COL_DEV_ID, newMessager.getDeviceUniqueID());
                        values.put(DataProvider.COL_PICTURE, newMessager.getPictureUrl());
                        values.put(DataProvider.COL_ISGROUP, 0);
                        this.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //broadcastIntent.putExtra(IntentExtras.NEW_PINGER, newPinger);
                break;
            case Constants.ACTION_SEND_MESSAGE:
                try {
                    Message newMessage = getNewMessage(data);
                    sendNotification(newMessage.getBody());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //broadcastIntent.putExtra(IntentExtras.NEW_PING, newPing);
                break;
        }
        /*if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }*/

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_setting_light)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private ArrayList<Messager> getMessagers(Bundle data) throws JSONException {
        final JSONArray clients = new JSONArray(data.getString("clients"));
        ArrayList<Messager> pingers = new ArrayList<>(clients.length());
        for (int i = 0; i < clients.length(); i++) {
            JSONObject jsonPinger = clients.getJSONObject(i);
            pingers.add(Messager.fromJson(jsonPinger));
        }
        return pingers;
    }

    private Messager getNewMessager(Bundle data) throws JSONException {
        final JSONObject client = new JSONObject(data.getString("client"));
        return Messager.fromJson(client);
    }

    private Message getNewMessage(Bundle data) throws JSONException {
        final Bundle notificationData = data.getBundle(Constants.NOTIFICATION);
        return new Message(notificationData.getString(Constants.NOTIFICATION_BODY),
                data.getString(Constants.SENDER));
    }


}