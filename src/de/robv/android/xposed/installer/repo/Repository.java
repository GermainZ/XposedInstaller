package de.robv.android.xposed.installer.repo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Repository implements Parcelable {
	public String name;
	public final Map<String, Module> modules = new HashMap<String, Module>();
	boolean isNew = true;

	/*package*/ Repository() {};

	public Repository(Parcel in) {
		name = in.readString();
		in.readMap(modules, Repository.class.getClassLoader());
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		if (isNew) {
			isNew = false;
			out.writeMap(modules);
		}
	}

	public static final Creator<Repository> CREATOR = new Creator<Repository>() {
		@Override
		public Repository createFromParcel(Parcel in) {
			return new Repository(in);
		}

		@Override
		public Repository[] newArray(int size) {
			return new Repository[size];
		}
	};
}
