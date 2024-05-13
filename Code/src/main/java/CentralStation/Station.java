package CentralStation;

public class Station {
    private long stationId;
    private long s_No;
    private String batteryStatus;
    private long statusTimestamp;
    private WeatherDetails weather;
    public Station(long stationId2,long sNo, String batteryStatus, long statusTimestamp, WeatherDetails weather){
        this.stationId = stationId2;
        this.s_No = sNo;
        this.batteryStatus = batteryStatus;
        this.statusTimestamp = statusTimestamp;
        this.weather = weather;
    }

    public long getStationId() {
        return stationId;
    }
    public void setStationId(long stationId) {this.stationId = stationId;}

    public long getSNo() {
        return s_No;
    }
    public void setSNo(long sNo) {
        this.s_No = sNo;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }
    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public long getStatusTimestamp() {
        return statusTimestamp;
    }
    public void setStatusTimestamp(long statusTimestamp) {
        this.statusTimestamp = statusTimestamp;
    }

    public WeatherDetails getWeather() {
        return weather;
    }
    public void setWeather(WeatherDetails weather) {
        this.weather = weather;
    }
}

