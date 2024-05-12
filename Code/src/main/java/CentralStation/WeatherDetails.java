package CentralStation;


public class WeatherDetails {

    private int humidity;
    private int temperature;
    private int windSpeed;

    public WeatherDetails(int humidity, int temperature, int windSpeed){
        this.humidity = humidity;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }

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