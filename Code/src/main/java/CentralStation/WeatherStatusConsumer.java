package CentralStation;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherStatusConsumer {
    private static final String TOPIC_NAME = "weather_topic";
    private static final String BOOTSTRAP_SERVERS = System.getenv("KAFKA_BROKER_URL");
    private static final Bitcask bitcask = new Bitcask(System.getenv("BITCASK_DIRECTORY"));
    private static final ParquetStatusWriter parquetWriter;
    static {
        parquetWriter = new ParquetStatusWriter();
    }

    public static void main(String[] args) {
        if (BOOTSTRAP_SERVERS == null) {
            System.err.println("Environment variable KAFKA_BROKER_URL must be set.");
            System.exit(1);
        }
        System.out.println("Kafka Broker URL: " + BOOTSTRAP_SERVERS);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer_group");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC_NAME));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                System.out.println("Fetched " + records.count() + " records");
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Processing record: " + record.value());
                    processWeatherStatus(record.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processWeatherStatus(String message) {
        System.out.println("Processing message: " + message);
        try {
            Station stationStatus = extractStationDetails(message);
            long stationID = stationStatus.getStationId();
            bitcask.put("station_" + stationID, message);
            parquetWriter.archiveWeatherStatus(stationStatus);
            System.out.println("Processed message for station ID: " + stationID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Station extractStationDetails(String jsonMessage) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonMessage);

        long stationId = jsonNode.get("station_id").asLong();
        long sNo = jsonNode.get("s_no").asLong();
        String batteryStatus = jsonNode.get("battery_status").asText();
        long statusTimestamp = jsonNode.get("status_timestamp").asLong();

        JsonNode weatherNode = jsonNode.get("weather");
        int humidity = weatherNode.get("humidity").asInt();
        int temperature = weatherNode.get("temperature").asInt();
        int windSpeed = weatherNode.get("wind_speed").asInt();

        WeatherDetails weather = new WeatherDetails(humidity, temperature, windSpeed);

        return new Station(stationId, sNo, batteryStatus, statusTimestamp, weather);
    }
}