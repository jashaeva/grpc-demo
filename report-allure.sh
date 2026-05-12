#!/bin/bash

echo "Generating Allure report..."

mkdir -p build/all-allure-results

if [ -d "grpc-server/build/allure-results" ]; then
    echo "Copying results from grpc-server"
    cp grpc-server/build/allure-results/* build/all-allure-results/
else
    echo "ERROR: allure-results not found"
    exit 1
fi

echo "Generating report"
allure generate build/all-allure-results -o build/reports/allure-aggregated --clean

echo "Report generated at build/reports/allure-aggregated/index.html"

# Не открываем отчет в CI (убираем allure open)
# Вместо этого показываем путь к файлу
echo "::notice title=Allure Report::Report available at build/reports/allure-aggregated/index.html"