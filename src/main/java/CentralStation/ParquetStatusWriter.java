package CentralStation;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParquetStatusWriter {
    private static final String OUTPUT_PATH = "/home/Documents/weather_statuses_directory";
    private static final int BATCH_SIZE = 10000; // Common batch size
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Map<String, ParquetWriter<Group>> writerMap;
    private List<Group> recordBuffer;
    private FileSystem fileSystem;

    public ParquetStatusWriter() throws IOException {
        recordBuffer = new ArrayList<>();
        writerMap = new HashMap<>();
        Configuration conf = new Configuration();
        fileSystem = FileSystem.get(conf);
    }

    private static MessageType createSchema() {
        String schemaString = "message WeatherStatus {\n" +
                "  required long station_id;\n" +
                "  required long s_no;\n" +
                "  required binary battery_status (UTF8);\n" +
                "  required int64 timestamp;\n" +
                "  required int32 humidity;\n" +
                "  required int32 temperature;\n" +
                "  required int32 wind_speed;\n" +
                "}";
        return MessageTypeParser.parseMessageType(schemaString);
    }

    public void archiveWeatherStatus(Station status) throws IOException {
        recordBuffer.add(convertToGroup(status));
        if (recordBuffer.size() >= BATCH_SIZE) {
            writeBatchToParquet();
        }
    }

    private void writeBatchToParquet() throws IOException {
        for (Group record : recordBuffer) {
            String partitionPath = generatePartitionPath(record);
            ParquetWriter<Group> writer = writerMap.computeIfAbsent(partitionPath, t -> {
                try {
                    return createWriter(t);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });
            if (writer != null) {
                writer.write(record);
            }
        }
        recordBuffer.clear();
    }

    @SuppressWarnings("deprecation")
    private ParquetWriter<Group> createWriter(String partitionPath) throws IOException {
        MessageType schema = createSchema();
        Configuration conf = new Configuration();
        GroupWriteSupport writeSupport = new GroupWriteSupport();
        GroupWriteSupport.setSchema(schema, conf);
        Path path = new Path(partitionPath);
        return new ParquetWriter<>(path, writeSupport, ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
                ParquetWriter.DEFAULT_BLOCK_SIZE, ParquetWriter.DEFAULT_PAGE_SIZE, ParquetWriter.DEFAULT_PAGE_SIZE,
                ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED, ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
                ParquetWriter.DEFAULT_WRITER_VERSION, conf);
    }

    private Group convertToGroup(Station status) {
        SimpleGroupFactory groupFactory = new SimpleGroupFactory(createSchema());
        return groupFactory.newGroup()
                .append("station_id", status.getStationId())
                .append("s_no", status.getSNo())
                .append("battery_status", status.getBatteryStatus())
                .append("timestamp", status.getStatusTimestamp())
                .append("humidity", status.getWeather().getHumidity())
                .append("temperature", status.getWeather().getTemperature())
                .append("wind_speed", status.getWeather().getWindSpeed());
    }

    private String generatePartitionPath(Group record) {
        long timestamp = record.getLong("timestamp", 3);
        long stationId = record.getLong("station_id", 0);
        Instant instant = Instant.ofEpochMilli(timestamp);
        String datePartition = TIMESTAMP_FORMATTER.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
        return OUTPUT_PATH + "/" + datePartition + "/station_" + stationId + ".parquet";
    }

    public void close() throws IOException {
        for (ParquetWriter<Group> writer : writerMap.values()) {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
