package omsu.inventory.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import omsu.grpc.*;
import omsu.inventory.model.OrderEntity;
import omsu.inventory.model.OrderInfoEntity;
import omsu.inventory.services.IOrderService;
import omsu.inventory.utils.ProtoTimestampConverter;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderGrpcImpl extends OrderGrpc.OrderImplBase {

private final IOrderService service;

public OrderGrpcImpl(IOrderService service) {
    this.service = service;
}

//
//
//    rpc getOrderInfo (IdMessage) returns (OrderInfo) {}
//
//    rpc getInvCountById (IdMessage) returns (CountData) {}
//    rpc getInventoryByOrderId (IdMessage) returns (stream InventoryData) {}
//    rpc getAllOrders (stream IdMessage) returns (stream OrderInfo) {}
//

    @Override
    public void createOrder (OrderData request, StreamObserver<IdMessage> response) {
        OrderEntity order = new OrderEntity(request.getUser(), request.getStatus());
        UUID uuid = service.create(order);

        IdMessage res = IdMessage.newBuilder()
                .setId(uuid.toString())
                .build();

        response.onNext(res);
        response.onCompleted();

    }

    @Override
    public void updateOrder (OrderDataWithId request, StreamObserver<Empty> responseObserver) {
        OrderEntity order = new OrderEntity(
                UUID.fromString(request.getId()),
                request.getUser(),
                request.getStatus(),
                ProtoTimestampConverter.toInstant(request.getCreatedAt())
        );
        service.update(order);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();

    }

    @Override
    public void getOrderInfo(IdMessage request, StreamObserver<OrderInfo> responseObserver) {
       OrderInfoEntity entity = service.getOrderInfo(request);

       OrderInfo orderInfo = OrderInfo.newBuilder()
               .build();
        responseObserver.onNext(orderInfo);
        responseObserver.onCompleted();
    }

/*
    @Override
    public void getCountByInvId(InventoryByIdRequest request, StreamObserver<CountData> response) {
        for (int i = 1; i <= 5; i++) {
            StockQuote stockQuote = StockQuote.newBuilder()

                    .build();
            responseObserver.onNext(stockQuote);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<>  getCountListByIdList(IdMessage request, StreamObserver<CountData> response) {
        return new StreamObserver<Stock>() {
            @Override
            public void onNext(Stock request) {
                for (int i = 1; i <= 5; i++) {
                    StockQuote stockQuote = StockQuote.newBuilder()
                            .setPrice(fetchStockPriceBid(request))
                            .setOfferNumber(i)
                            .setDescription("Price for stock:" + request.getTickerSymbol())
                            .build();
                    responseObserver.onNext(stockQuote);
                }
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }

            //handle OnError() ...
        };
    }*/
}
