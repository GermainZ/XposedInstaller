package de.robv.android.xposed.installer.util;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePair implements Parcelable {
	public final String first;
	public final String second;

 	public ParcelablePair(String first, String second) {
		this.first = first;
		this.second = second;
	}

	public ParcelablePair(Parcel in) {
		first = in.readString();
		second = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(first);
		out.writeString(second);
	}

	public static final Creator<ParcelablePair> CREATOR = new Creator<ParcelablePair>() {
		@Override
		public ParcelablePair createFromParcel(Parcel in) {
			return new ParcelablePair(in);
		}

		@Override
		public ParcelablePair[] newArray(int size) {
			return new ParcelablePair[size];
		}
	};

}
