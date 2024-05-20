import os
import pandas as pd
import time
import sys
from elasticsearch import Elasticsearch

def log_message(message):
    print(message)
    sys.stdout.flush()


# Check environment variables
es_host = os.getenv("ES_SERVER")
directory_path = "/home/data/parquet"


# Connect to Elasticsearch
try:
    es = Elasticsearch([f'{es_host}'])
    # Check if the connection is successful
    if es.ping():
        print("Connected to Elasticsearch successfully.")
    else:
        print("Failed to connect to Elasticsearch.")
except Exception as e:
    print(f"Error connecting to Elasticsearch: {e}")


# Process each Parquet file in the directory
log_message('============ Indexer Started ============')
while True:
    try:
        for folder in os.listdir(directory_path):
            path = directory_path + '/' + folder
            log_message(f'Current Directory: {directory_path}')
            for station in os.listdir(path):
                station_path = os.path.join(path, station)
                log_message(f'{station_path} is Read.')
                if os.path.isdir(station_path):
                    log_message(f'Processing station directory: {station_path}')
                    for file_name in os.listdir(station_path):
                        if file_name.endswith(".parquet"):
                            file_path = os.path.join(station_path, file_name)
                            log_message(f'Found Parquet file: {file_path}')
                            try:
                                # Read Parquet file
                                df = pd.read_parquet(file_path)
                                log_message(f'Read file {file_name}')
                                # Convert DataFrame to JSON documents
                                docs = df.to_dict(orient='records')
                                log_message(f'Converted {file_name} to JSON')
                                # Index documents into Elasticsearch
                                for doc in docs:
                                    es.index(index='weather_stations_new', body=doc)
                                log_message(f'Indexed documents from {file_name}')
                            except Exception as e:
                                log_message(f"Error indexing document {file_name}: {e}")
            # Sleep for a while before next check
            time.sleep(60)
    except Exception as e:
        log_message(f"Error processing directory {directory_path}: {e}")
        time.sleep(60)