package tonyd.gti785dataclient;

import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class File {
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

}
