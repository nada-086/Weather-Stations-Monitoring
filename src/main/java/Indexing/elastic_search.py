from elasticsearch import Elasticsearch
from avro.datafile import DataFileReader
from avro.io import DatumReader
import os
import json
import pandas as pd

# Connect to Elasticsearch
client = Elasticsearch(['http://localhost:9200'])

# Directory containing Parquet files
directory_path = "/home/toka/Documents/weather_statuses_directory/2024-MAY-5"

# Process each Parquet file in the directory
for station in os.listdir(directory_path):
    station_path = os.path.join(directory_path, station)
    if os.path.isdir(station_path):
        # Process each Parquet file in the station directory
        for file_name in os.listdir(station_path):
            if file_name.endswith(".parquet"):
                file_path = os.path.join(station_path, file_name)
               # print(file_path)

                try:
                    # Read Parquet file
                    df = pd.read_parquet(file_path)

                    # Convert DataFrame to JSON documents
                    docs = df.to_dict(orient='records')

                    # Index documents into Elasticsearch
                    for doc in docs:
                        client.index(index='weather_stations', body=doc)
                except Exception as e:
                    print(f"Error indexing document: {e}")

# Close Elasticsearch client
#client.close()

