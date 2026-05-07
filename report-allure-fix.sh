#!/bin/bash

echo "Generating..."
echo "Создаем директорию для сбора результатов"
mkdir -p build/all-allure-results

echo "Копируем результаты из подпроекта сервера"
cp grpc-server/build/allure-results/* build/all-allure-results/

echo "Генерируем отчет напрямую через allure"
allure generate build/all-allure-results -o build/reports/allure-aggregated --clean

echo "Проверяем результат"
ls -la build/reports/allure-aggregated/

## Скопировать историю для трендов
#cp -r allure-report/history allure-results/history
#
## Перегенерировать с историей
#allure generate allure-results --clean -o allure-report

echo "Открываем отчет"
allure open build/reports/allure-aggregated
