package tonyd.gti785dataclient;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.ParameterizedType;
import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class FileSync implements Parcelable{

    private String fullPath;
    private Date date;
    private int size;
    private Status status;

    public FileSync(String fullPath, Date date, int size, Status status) {
        this.fullPath = fullPath;
        this.date = date;
        this.size = size;
        this.status = status;
    }

    protected FileSync(Parcel in) {
        fullPath = in.readString();
        date = new Date(in.readLong());
        size = in.readInt();
        status = Status.valueOf(in.readString());
    }

    public static final Creator<FileSync> CREATOR = new Creator<FileSync>() {
        @Override
        public FileSync createFromParcel(Parcel in) {
            return new FileSync(in);
        }

        @Override
        public FileSync[] newArray(int size) {
            return new FileSync[size];
        }
    };

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FileSync{" +
                "fullPath='" + fullPath + '\'' +
                ", date=" + date +
                ", size=" + size +
                ", status=" + status +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullPath);
        dest.writeLong(date.getTime());
        dest.writeInt(size);
        dest.writeString(status.name());
    }

    public enum Status {
        ADDED,
        UPDATED,
        DELETED
    }
}
