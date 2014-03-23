package de.robv.android.xposed.installer.repo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import de.robv.android.xposed.installer.util.ParcelablePair;

public class Module implements Parcelable {
	public final Repository repository;
	public String packageName;
	public String name;
	public String summary;
	public String description;
	public boolean descriptionIsHtml = false;
	public String author;
	public String support;
	public final List<ParcelablePair> moreInfo = new LinkedList<ParcelablePair>();
	public final List<ModuleVersion> versions = new ArrayList<ModuleVersion>();
	public final List<String> screenshots = new ArrayList<String>();
	public long created = -1;
	public long updated = -1;
	boolean isNew = true;

	/*package*/ Module(Repository repository) {
		this.repository = repository;
	}

	public Module(Parcel in) {
		ClassLoader classLoader = Module.class.getClassLoader();
		repository = in.readParcelable(classLoader);
		packageName = in.readString();
		name = in.readString();
		summary = in.readString();
		description = in.readString();
		descriptionIsHtml = (in.readInt() == 1);
		author = in.readString();
		support = in.readString();
		in.readList(moreInfo, classLoader);
		in.readList(versions, classLoader);
		in.readList(screenshots, classLoader);
		created = in.readLong();
		updated = in.readLong();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		boolean isNewTmp = isNew;
		if (isNewTmp) {
			isNew = false;
			out.writeParcelable(repository, 0);
		}
		out.writeString(packageName);
		out.writeString(name);
		out.writeString(summary);
		out.writeString(description);
		out.writeInt(descriptionIsHtml ? 1 : 0);
		out.writeString(author);
		out.writeString(support);
		out.writeList(moreInfo);
		if (isNewTmp)
			out.writeList(versions);
		out.writeList(screenshots);
		out.writeLong(created);
		out.writeLong(updated);
	}

	public static final Creator<Module> CREATOR = new Creator<Module>() {
		@Override
		public Module createFromParcel(Parcel in) {
			return new Module(in);
		}

		@Override
		public Module[] newArray(int size) {
			return new Module[size];
		}
	};
}
