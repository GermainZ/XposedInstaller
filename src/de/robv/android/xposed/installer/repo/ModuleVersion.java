package de.robv.android.xposed.installer.repo;


import android.os.Parcel;
import android.os.Parcelable;

public class ModuleVersion implements Parcelable {
	public final Module module;
	public String name;
	public int code;
	public String downloadLink;
	public String md5sum;
	public String changelog;
	public boolean changelogIsHtml = false;
	public String branch;
	
	/*package*/ ModuleVersion(Module module) {
		this.module = module;
	}

	public ModuleVersion(Parcel in) {
		module = in.readParcelable(ModuleVersion.class.getClassLoader());
		name = in.readString();
		code = in.readInt();
		downloadLink = in.readString();
		md5sum = in.readString();
		changelog = in.readString();
		changelogIsHtml = in.readByte() == 1;
		branch = in.readString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(module, 0);
		out.writeString(name);
		out.writeInt(code);
		out.writeString(downloadLink);
		out.writeString(md5sum);
		out.writeString(changelog);
		out.writeByte(changelogIsHtml ? (byte) 1 : (byte) 0);
		out.writeString(branch);
	}

	public static final Creator<ModuleVersion> CREATOR = new Creator<ModuleVersion>() {
		@Override
		public ModuleVersion createFromParcel(Parcel in) {
			return new ModuleVersion(in);
		}

		@Override
		public ModuleVersion[] newArray(int size) {
			return new ModuleVersion[size];
		}
	};
}
