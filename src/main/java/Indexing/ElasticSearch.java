// package Indexing;

// import org.apache.avro.generic.GenericRecord;
// import org.apache.http.HttpHost;
// import org.apache.parquet.avro.AvroParquetReader;

// import org.elasticsearch.action.index.IndexRequest;
// import org.elasticsearch.action.index.IndexResponse;
// import org.elasticsearch.client.RequestOptions;
// import org.elasticsearch.client.RestClient;
// import org.elasticsearch.client.RestHighLevelClient;
// import org.elasticsearch.common.xcontent.XContentType;

// import java.io.File;
// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.Objects;

// import com.fasterxml.jackson.databind.ObjectMapper;

// public class ElasticSearch {
//     @SuppressWarnings("deprecation")
//     public static void main(String[] args) throws IOException {
//         RestHighLevelClient client = new RestHighLevelClient(
//                 RestClient.builder(new HttpHost("localhost", 9200, "http")));

//         // Directory containing Parquet files
//         String directoryPath = "weather_statuses_directory";

//         // Process each Parquet file in the directory
//         File directory = new File(directoryPath);
//         for (File file : Objects.requireNonNull(directory.listFiles())) {
//             if (file.isFile() && file.getName().endsWith(".parquet")) {
//                 String filePath = file.getAbsolutePath();
//                 Map<String, Object> doc = parquetToJson(filePath);

//                 // Index document into Elasticsearch
//                 IndexRequest request = new IndexRequest("weather_statuses")
//                         .source(doc, XContentType.JSON);
//                 IndexResponse response = client.index(request, RequestOptions.DEFAULT);
//                 String index = response.getIndex();
//                 System.out.println("Document indexed to index: " + index);
//             }
//         }

//         client.close();
//     }

//     public static Map<String, Object> parquetToJson(String parquetFilePath) throws IOException {
//         Map<String, Object> documents = new HashMap<>();

//         try (AvroParquetReader<GenericRecord> reader = new AvroParquetReader<>(new org.apache.hadoop.fs.Path(parquetFilePath))) {
//             GenericRecord record;
//             ObjectMapper objectMapper = new ObjectMapper();

//             // Read each record and convert it to JSON
//             while ((record = reader.read()) != null) {
//                 // Convert GenericRecord to JSON string
//                 String json = objectMapper.writeValueAsString(record);
//                 documents.put("data", json);
//             }
//         }
//         return documents;
//     }
// }
