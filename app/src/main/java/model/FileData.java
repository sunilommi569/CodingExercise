package model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by sunil on 9/9/17.
 */

public class FileData implements Parcelable {
    public String[] mTenFileNames = new String[10];
    public double[] mTenFileSizes = new double[10];
    public String[] mFrequentFileExtensions = new String[5];
    public double mAvgFileSize = 0;
    public double mTotalMbScanned = 0;
    public int isScanning = 0;

    public FileData() {
    }

    public static final Parcelable.Creator<FileData> CREATOR = new Parcelable.Creator<FileData>() {
        @Override
        public FileData createFromParcel(Parcel in) {
            return new FileData(in);
        }

        @Override
        public FileData[] newArray(int size) {
            return new FileData[size];
        }
    };


    protected FileData(Parcel in) {
        mAvgFileSize = in.readDouble();
        in.readStringArray(mTenFileNames);
        in.readDoubleArray(mTenFileSizes);
        in.readStringArray(mFrequentFileExtensions);
        mTotalMbScanned = in.readDouble();
        isScanning = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mAvgFileSize);
        dest.writeStringArray(mTenFileNames);
        dest.writeDoubleArray(mTenFileSizes);
        dest.writeStringArray(mFrequentFileExtensions);
        dest.writeDouble(mTotalMbScanned);
        dest.writeInt(isScanning);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
