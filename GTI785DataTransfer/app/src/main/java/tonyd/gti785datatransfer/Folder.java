package tonyd.gti785datatransfer;

import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class Folder {

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

}
