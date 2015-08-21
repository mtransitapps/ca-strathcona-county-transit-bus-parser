#!/bin/bash
echo ">> Downloading..."
wget --header="User-Agent: MonTransit" --timeout=60 --tries=6 -i input_url -O input/gtfs.zip
echo ">> Downloading... DONE"