import elasticsearch
from elasticsearch import Elasticsearch

# Connect to Elasticsearch
es_host = os.getenv("ELASTICSEARCH_HOST", "localhost")
es_port = os.getenv("ELASTICSEARCH_PORT", "9200")
es = Elasticsearch([f'http://{es_host}:{es_port}'])

# Define the Elasticsearch query to retrieve distinct values of s_no
query = {
    "aggs": {
        "unique_sno": {
            "cardinality": {
                "field": "s_no"
            }
        }
    }
}

# Execute the Elasticsearch search query
search_results = es.search(index="weather_stations_new", body=query)

# Extract the count of distinct s_no values from the aggregation result
distinct_sno_count = search_results["aggregations"]["unique_sno"]["value"]

# Print the count of distinct s_no values
print("Number of distinct values of s_no:", distinct_sno_count)

query1 = {
    "query": {
        "match_all": {}
    }
}

# Execute the Elasticsearch search query to retrieve the total count of documents
search_results1 = es.search(index="weather_stations_new", body=query1)

# Extract the total count of documents from the search results
total_documents_count = search_results1["hits"]["total"]["value"]

# Print the total count of documents
print("Total count of documents:", total_documents_count)

total_dropped = distinct_sno_count*10 - total_documents_count
print("Total count of dropped messages:", total_dropped)


percentage = total_dropped / distinct_sno_count*10
print(f"dropped percentage: {percentage:.2f} %")