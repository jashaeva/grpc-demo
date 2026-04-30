package omsu.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import omsu.grpc.*;
import omsu.model.OrderEntity;
import omsu.model.OrderInfoEntity;
import omsu.services.IOrderService;
import omsu.utils.TimestampConverter;

import java.util.UUID;

public class OrderGrpcImpl extends OrderGrpc.OrderImplBase {

private final IOrderService service;

public OrderGrpcImpl(IOrderService service) {
    this.service = service;
}

//    rpc getInvCountById (IdMessage) returns (CountData) {}
//    rpc getInventoryByOrderId (IdMessage) returns (stream InventoryData) {}
//    rpc getAllOrders (stream IdMessage) returns (stream OrderInfo) {}

    @Override
    public void createOrder (OrderData request, StreamObserver<IdMessage> response) {
        UUID uuid = service.create(request);

        IdMessage res = IdMessage.newBuilder()
                .setId(uuid.toString())
                .build();

        response.onNext(res);
        response.onCompleted();

    }

    @Override
    public void updateOrder (OrderDataWithId request, StreamObserver<Empty> responseObserver) {
        service.update(request);
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
