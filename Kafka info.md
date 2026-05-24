
# Инструкция по Kafka 

Собственно Kafka запускается как докер-образ вместе с zookeeper и работает на порту localhost:9092. Настройки в файле (лежит в корне).

В проекте: 
1. подключить зависимость  `org.springframework.kafka:spring-kafka`,
2. настроить в файлах application.yaml  в свойстве **kafka**  свойства topic, producer, consumer. 
3. Описать процесс отправки сообщения для @KafkaTemplate (producer)
4. Описать процесс "слушания" сообщения для @KafkaListener (consumer)

При старте приложения спринг бут самостоятельно создаст бины, которые нужны для дальнейшей работы. 


Задача в проекте:

Логирование через Kafka
При выполнении любого gRPC-вызова на grpc-server сервер отправляет событие 
(например, "Вызов метода X с параметром Y") в топик Kafka. rest-client может подписаться на этот топик и
выводить в консоль все вызовы, которые дергает gRPC-сервер.


### Посмотреть сообщения

julia@julia-notebook:~/Study/GRPC/grpc-demo$docker ps | grep kafka
**b40eacb5fb26**   confluentinc/cp-kafka:7.3.2       "/etc/confluent/dock…"   3 hours ago   Up 3 hours   0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp                       kafka

julia@julia-notebook:~/Study/GRPC/grpc-demo$ docker exec -it **b40eacb5fb26** /usr/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic grpc-logs --from-beginning

Вывод будет примерно такой:

~~~text
{"method":"editInventory","request":"id: \"0789891a-4b92-11f1-b655-fed7fd096073\"\n","response":"id: \"0789891a-4b92-11f1-b655-fed7fd096073\"\nname: \"\\320\\255\\321\\200\\320\\263\\320\\276\\320\\275\\320\\276\\320\\274\\320\\270\\321\\207\\320\\275\\321\\213\\320\\271 \\320\\221\\320\\265\\321\\202\\320\\276\\320\\275\\320\\275\\321\\213\\320\\271 \\320\\221\\321\\203\\320\\274\\320\\260\\320\\266\\320\\275\\320\\270\\320\\272\"\ncount: 9\n"}
{"method":"createInventory","request":"name: \"Chair\"\ncount: 100\n","response":"id: \"6d6dcf6e-5792-11f1-aa04-7ae9421208de\"\n"}
{"method":"editInventory","request":"id: \"6d6dcf6e-5792-11f1-aa04-7ae9421208de\"\n","response":"id: \"6d6dcf6e-5792-11f1-aa04-7ae9421208de\"\nname: \"Chair\"\ncount: 100\n"}
{"method":"createInventory","request":"name: \"Pants\"\ncount: 23\n","response":"id: \"2e692e34-5793-11f1-aa04-7ae9421208de\"\n"}
{"method":"editInventory","request":"id: \"2e692e34-5793-11f1-aa04-7ae9421208de\"\n","response":"id: \"2e692e34-5793-11f1-aa04-7ae9421208de\"\nname: \"Pants\"\ncount: 23\n"}
^CProcessed a total of 5 messages
~~~