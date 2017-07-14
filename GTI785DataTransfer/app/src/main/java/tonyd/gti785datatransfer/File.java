package tonyd.gti785datatransfer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class File implements Parcelable {
    private String name;
    private Date date;
    private int size;

    public File(String name, Date date, int size) {
        this.name = name;
        this.date = date;
        this.size = size;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "File{" +
                "name='" + name + '\'' +
                ", date=" + date +
                ", size=" + size +
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
        dest.writeInt(size);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public File createFromParcel(Parcel in) {
            return new File(in);
        }

        public File[] newArray(int size) {
            return new File[size];
        }
    };

    protected File(Parcel in){
        name = in.readString();
        date = new Date(in.readLong());
        size = in.readInt();
    }
}
