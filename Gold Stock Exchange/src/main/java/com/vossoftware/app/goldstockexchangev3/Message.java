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

/**
 * A ping.
 */
public class Message implements Parcelable {

    // The ping message
    private final String mBody;
    // The registration token of the sender
    private final String mFrom;

    public Message(String mBody, String mFrom) {
        this.mBody = mBody;
        this.mFrom = mFrom;
    }

    public String getBody() {
        return mBody;
    }

    public String getFrom() {
        return mFrom;
    }

    protected Message(Parcel in) {
        mBody = in.readString();
        mFrom = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBody);
        dest.writeString(mFrom);
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
