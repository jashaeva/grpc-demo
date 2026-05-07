#!/bin/bash

echo "Running tests..."
#./gradlew clean test

echo "Generating aggregated Allure report..."
./gradlew allureAggregatedReport

echo "Allure report generated at: build/reports/allure-aggregated/index.html"

# Открыть отчет
if [[ "$OSTYPE" == "darwin"* ]]; then
    open build/reports/allure-aggregated/index.html
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    xdg-open build/reports/allure-aggregated/index.html
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    start build/reports/allure-aggregated/index.html
fi