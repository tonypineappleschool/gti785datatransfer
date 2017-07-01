package tonyd.gti785datatransfer;

import java.util.Date;

/**
 * Created by stefaniekoy on 2017-06-28.
 */

public class Pair {

    private int id;
    private String name;
    private String ip;
    private int port;
    private Date lastAccessed;
    private Location location;

    public Pair(int id, String name, String ip, int port, Date lastAccessed){
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.lastAccessed = lastAccessed;
        this.location = new Location(0.0, 0.0);
    }

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

    public class Location {
        private double longitude;
        private double latitude;

        public Location(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

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
    }

}
