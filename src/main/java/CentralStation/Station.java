package CentralStation;

public class Station {
    private int stationId;
    private int s_No;
    private String batteryStatus;
    private long statusTimestamp;
    private WeatherDetails weather;

        public int getStationId() {
            return stationId;
        }
        public void setStationId(int stationId) {this.stationId = stationId;}

        public int getSNo() {
            return s_No;
        }
        public void setSNo(int sNo) {
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

class WeatherDetails {

    private int humidity;
    private int temperature;
    private int windSpeed;

    public int getHumidity() {
        return humidity;
    }
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getTemperature() {
        return temperature;
    }
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getWindSpeed() {
        return windSpeed;
    }
    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }}