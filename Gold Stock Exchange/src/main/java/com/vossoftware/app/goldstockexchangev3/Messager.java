/*
 * Copyright Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vossoftware.app.goldstockexchangev3;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A registered entity that is able to ping another entity.
 */
public class Messager implements Parcelable {

    private final String mName;
    private final String mPictureUrl;
    private final String mRegistrationToken;
    private final String mDeviceUniqueId;

    public Messager(String name, String pictureUrl, String registrationToken, String deviceUniqueId) {
        mName = name;
        mPictureUrl = pictureUrl;
        mRegistrationToken = registrationToken;
        mDeviceUniqueId = deviceUniqueId;
    }

    public String getName() {
        return mName;
    }

    public String getPictureUrl() {
        return mPictureUrl;
    }

    public String getRegistrationToken() {
        return mRegistrationToken;
    }

    public String getDeviceUniqueID() { return  mDeviceUniqueId; }

    /**
     * Creates a new {@link Messager} from a provided {@link JSONObject}.
     *
     * @param jsonPinger The JSON representation of a {@link Messager}.
     * @return The {@link Messager} parsed out of the {@link JSONObject}.
     * @throws JSONException Thrown when parsing was not possible.
     */
    public static Messager fromJson(JSONObject jsonPinger) throws JSONException {
        return new Messager(jsonPinger.getString(Constants.NAME),
                jsonPinger.getString(Constants.PICTURE_URL),
                jsonPinger.getString(Constants.REGISTRATION_TOKEN), jsonPinger.getString(Constants.DEVICE_UNIQUE_ID));
    }

    protected Messager(Parcel in) {
        mName = in.readString();
        mPictureUrl = in.readString();
        mRegistrationToken = in.readString();
        mDeviceUniqueId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPictureUrl);
        dest.writeString(mRegistrationToken);
        dest.writeString(mDeviceUniqueId);
    }

    public static final Creator<Messager> CREATOR = new Creator<Messager>() {
        @Override
        public Messager createFromParcel(Parcel in) {
            return new Messager(in);
        }

        @Override
        public Messager[] newArray(int size) {
            return new Messager[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Messager)) {
            return false;
        }

        Messager messager = (Messager) o;

        if (!mName.equals(messager.mName)) {
            return false;
        }
        if (!mPictureUrl.equals(messager.mPictureUrl)) {
            return false;
        }
        return mRegistrationToken.equals(messager.mRegistrationToken);

    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mPictureUrl.hashCode();
        result = 31 * result + mRegistrationToken.hashCode();
        result = 31 * result + mDeviceUniqueId.hashCode();
        return result;
    }
}
