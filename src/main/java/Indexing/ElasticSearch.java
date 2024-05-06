package Indexing;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.http.HttpHost;
import org.apache.parquet.avro.AvroParquetReader;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("deprecation")
public class ElasticSearch {
    private static final Logger LOGGER = Logger.getLogger(ElasticSearch.class.getName());

    public static void main(String[] args) throws IOException {
        
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        // Directory containing Parquet files
        String directoryPath = "/home/toka/Documents/weather_statuses_directory/2024-MAY-5";

        // Process each Parquet file in the directory
        File stationDirectory = new File(directoryPath);
        for (File station : Objects.requireNonNull(stationDirectory.listFiles())) {
            if (station.isDirectory()) {
                // String stationId = station.getName().substring("Station".length());

                // Process each Parquet file in the station directory
                for (File file : Objects.requireNonNull(station.listFiles())) {
                    if (file.isFile() && file.getName().endsWith(".parquet")) {
                        String filePath = file.getAbsolutePath();

                        try {
                            List<String> doc = parquetToJson(filePath);
                            System.out.println(doc.get(0));


                            // Index document into Elasticsearch
                            if (doc != null) {
                                System.out.println("innnnnnnnnnn");
                                IndexRequest request = new IndexRequest("weather_statuses_2")
                                        .source(doc, XContentType.JSON);
                                System.out.println("out reqqqqqqqqq");
                                // IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                                // System.out.println("ahhhhhhhhhhhhh:  "+response.getId());
                                // String index = response.getIndex();
                                // System.out.println("laaaaaaaaaaaaaaaa: "+index);
                                // LOGGER.info("Document indexed to index: " + index);
                            }
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Error indexing document", e);
                        }
                    }
                }
            }
        }

        client.close();
    }

    public static List<String> parquetToJson(String parquetFilePath) throws IOException {
    List<String> jsonDocuments = new ArrayList<>();

    try (AvroParquetReader<GenericRecord> reader = new AvroParquetReader<>(new org.apache.hadoop.fs.Path(parquetFilePath))) {
        GenericRecord record;
        ObjectMapper objectMapper = new ObjectMapper();

        // Read each record and convert it to JSON
        while ((record = reader.read()) != null) {
            // Convert GenericRecord to a Map
            Map<String, Object> recordMap = genericRecordToMap(record);
            // Convert Map to JSON string
            String json = objectMapper.writeValueAsString(recordMap);
            // Add JSON string to the list
            jsonDocuments.add(json);
        }
    }
    return jsonDocuments.isEmpty() ? null : jsonDocuments;
    }
       // Convert GenericRecord to a Map
    private static Map<String, Object> genericRecordToMap(GenericRecord record) {
        Map<String, Object> recordMap = new HashMap<>();
        for (Schema.Field field : record.getSchema().getFields()) {
            recordMap.put(field.name(), record.get(field.name()));
        }
        return recordMap;
    }


}