package tonyd.gti785dataclient;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class Pair implements Parcelable {

    private int id;
    private String name;
    private String ip;
    private int port;
    private Date lastAccessed;
    private Location location;
    private boolean status;

    public Pair(int id, String name, String ip, int port, Date lastAccessed){
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.lastAccessed = lastAccessed;
        this.location = new Location(0.0, 0.0);
    }

    protected Pair(Parcel in) {
        id = in.readInt();
        name = in.readString();
        ip = in.readString();
        port = in.readInt();
        lastAccessed = new Date(in.readLong());
        location = in.readParcelable(Location.class.getClassLoader());
    }

    public static final Creator<Pair> CREATOR = new Creator<Pair>() {
        @Override
        public Pair createFromParcel(Parcel in) {
            return new Pair(in);
        }

        @Override
        public Pair[] newArray(int size) {
            return new Pair[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", lastAccessed=" + lastAccessed +
                ", location=" + location +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(ip);
        dest.writeInt(port);
        dest.writeLong(lastAccessed.getTime());
        dest.writeParcelable(location, flags);
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public static class Location implements Parcelable{
        private double longitude;
        private double latitude;

        public Location(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        protected Location(Parcel in) {
            longitude = in.readDouble();
            latitude = in.readDouble();
        }

        public static final Creator<Location> CREATOR = new Creator<Location>() {
            @Override
            public Location createFromParcel(Parcel in) {
                return new Location(in);
            }

            @Override
            public Location[] newArray(int size) {
                return new Location[size];
            }
        };

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        @Override
        public String toString() {
            return "Location{" +
                    "longitude=" + longitude +
                    ", latitude=" + latitude +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(longitude);
            dest.writeDouble(latitude);
        }
    }

}
