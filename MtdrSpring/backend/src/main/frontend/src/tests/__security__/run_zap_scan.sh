#!/bin/bash

# Get absolute path to this script's directory
SCRIPT_DIR=$(dirname "$(realpath "$0")")

# Run ZAP scan and save results to the security directory
docker run --network="host" \
  -v "$SCRIPT_DIR":/zap/wrk/:rw \
  -t zaproxy/zap-stable \
  zap.sh -cmd \
  -quickurl http://http://140.84.179.223// \
  -quickout /zap/wrk/result.xml

# Optional: Print confirmation
echo "ZAP scan completed. Results saved to: $SCRIPT_DIR/result.xml"