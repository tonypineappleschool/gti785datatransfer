package tonyd.gti785datatransfer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class Folder implements Parcelable{

    private String name;
    private Date date;

    public Folder(String name, Date date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeLong(date.getTime());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    protected Folder(Parcel in){
        name = in.readString();
        date = new Date(in.readLong());
    }
}
