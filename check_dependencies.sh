#!/bin/bash
for module in :grpc-common :grpc-server :grpc-client; do
    echo "=== Checking $module ==="
    ./gradlew $module:dependencies --configuration runtimeClasspath | grep -E "([0-9]+\.[0-9]+.*->|\(c\)|\(n\))" || echo "No conflicts found"
done