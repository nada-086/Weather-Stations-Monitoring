package CentralStation;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParquetStatusWriter {
    private static final String OUTPUT_PATH = System.getenv("PARQUET_DIRECTORY");
    private static final int BATCH_SIZE = 100;
    private final List<Station> recordBuffer;
    private static final HashMap<Long, String> paths = new HashMap<>();
    private static final HashMap<Long, ParquetWriter<GenericRecord>> writers = new HashMap<>();

    public ParquetStatusWriter() {
        recordBuffer = new ArrayList<>();
    }

    private static Schema createSchema() {
        return SchemaBuilder.record("weather_statuses")
            .fields()
            .name("station_id").type().longType().noDefault()
            .name("s_no").type().longType().noDefault()
            .name("battery_status").type().stringType().noDefault()
            .name("status_timestamp").type().longType().noDefault()
            .name("weather_humidity").type().intType().noDefault()
            .name("weather_temperature").type().intType().noDefault()
            .name("weather_wind_speed").type().intType().noDefault()
            .endRecord();
    }

    public void archiveWeatherStatus(Station status) throws IOException {
        recordBuffer.add(status);
        if (recordBuffer.size() >= BATCH_SIZE) {
            writeBatchToParquet();
        }
    }

    private void writeBatchToParquet() throws IOException {
        try {
            for (Station status : recordBuffer) {
                long stationID = status.getStationId();
                String generatedPath = generatePartitionPath(status);
                if (paths.get(stationID) == null || !generatedPath.equals(paths.get(stationID))) {
                    createWriter(generatedPath, stationID);
                }
                System.out.println("Parquet File Path Generated");
                GenericRecord record = new GenericData.Record(createSchema());
                                        record.put("station_id", stationID);
                                        record.put("s_no", status.getSNo());
                                        record.put("battery_status", status.getBatteryStatus());
                                        record.put("status_timestamp", status.getStatusTimestamp());
                                        record.put("weather_humidity", status.getWeather().getHumidity());
                                        record.put("weather_temperature", status.getWeather().getTemperature());
                                        record.put("weather_wind_speed", status.getWeather().getWindSpeed());
                writers.get(stationID).write(record);
                System.out.println("Record Written");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing batch to Parquet: " + e.getMessage(), e);
        } finally {
            recordBuffer.clear(); // Clear the buffer after writing
        }
    }

    private void createWriter(String generatedPath, long stationID) throws IOException {
        System.out.println("Creating writer for path: " + generatedPath + " and stationID: " + stationID);
        paths.put(stationID, generatedPath);
        CompressionCodecName codec = CompressionCodecName.SNAPPY;
        if (writers.get(stationID) != null) {
            writers.get(stationID).close();
        }
        writers.put(stationID, AvroParquetWriter.<GenericRecord>builder(new Path(generatedPath))
                .withSchema(createSchema())
                .withCompressionCodec(codec)
                .withDataModel(GenericData.get())
                .withConf(new Configuration())
                .build());
        System.out.println("Writer created successfully");
    }

    private String generatePartitionPath(Station status) {
        Instant instant = Instant.ofEpochSecond(status.getStatusTimestamp());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        int year = dateTime.getYear();
        String month = Month.of(dateTime.getMonthValue()).toString();
        int day = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();

        return OUTPUT_PATH + "/" + year + "-" + month + "-" + day +
            "/" + "Station" + status.getStationId() +
            "/" + hour + "-" + minute + ".parquet";
    }
}
