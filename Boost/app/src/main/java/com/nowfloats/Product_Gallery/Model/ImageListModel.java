package com.nowfloats.Product_Gallery.Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by guru on 09-06-2015.
 */
public class ImageListModel  implements Parcelable {
    public String ImageUri;
    public String TileImageUri;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(ImageUri);
        parcel.writeString(TileImageUri);
    }
    public ImageListModel(Parcel in){
        this.ImageUri = in.readString();
        this.TileImageUri = in.readString();
    }

    public static final Parcelable.Creator<ImageListModel> CREATOR = new Parcelable.Creator<ImageListModel>() {

        public ImageListModel createFromParcel(Parcel in) {
            return new ImageListModel(in);
        }

        public ImageListModel[] newArray(int size) {
            return new ImageListModel[size];
        }
    };
}
