# Dockerfile for Elasticsearch Script
FROM python:3.8-slim

WORKDIR /app

# Install required Python packages
COPY requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Copy the script into the container
COPY elastic_search.py elastic_search.py

# Set the entrypoint for the container
ENTRYPOINT ["python", "elastic_search.py"]