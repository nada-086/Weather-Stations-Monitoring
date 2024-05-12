package weatherStation;

import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class KafkaMsgProducer {
    static void sendMsg(String topic, String msg){
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        try (KafkaProducer<String,String> producer = new KafkaProducer<>(properties)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic,
                    msg);
            producer.send(record);
        }
    }
    static void rainingTriggers(String topic) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "rain-detection-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> inputStream = builder.stream(topic);

        KStream<String, String> rainStream = inputStream.filter((key, value) -> {
            // humidity value is stored in the message value json
            int humidity;
            try {
                humidity = extractHumidity(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return humidity > 70;
        });

        rainStream.mapValues(value -> {
            int humidity;
            try {
                humidity = extractHumidity(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return "Special message: Humidity is greater than 70 (" + humidity + ")";

        }).to("rain_detected_topic", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        // Shutdown hook to gracefully close the Kafka Streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static int extractHumidity(String json) throws JsonProcessingException {
        // Parse JSON using Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        // Extract humidity value
        int humidity = rootNode.path("weather").path("humidity").asInt();
        // System.out.println("Humidity: " + humidity);
        return humidity;
    }

//    public static void main(String[] args) {
//        // produce msg to kafka
////        send_msg("my_first", "Hye Kafka!are you here");
//        String json = "{ \n " +
//                "    \"station_id\": 1,\n" +
//                "    \"s_no\": 1,\n" +
//                "    \"battery_status\": \"low\",\n" +
//                "    \"status_timestamp\": 1681521224,\n" +
//                "    \"weather\": {\n" +
//                "        \"humidity\": 71,\n" +
//                "        \"temperature\": 100,\n" +
//                "        \"wind_speed\": 13\n" +
//                "    }\n" +
//                "}";
//        sendMsg("humidity_topic", json);
//        rainingTriggers();
//    }
}
