package tonyd.gti785datatransfer;

import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class FileSync {

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

    public enum Status {
        ADDED,
        UPDATED,
        DELETED
    }
}
