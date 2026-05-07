package omsu.steps;
import io.qameta.allure.Step;
import omsu.grpc.CreateRequest;
import omsu.grpc.IdMessage;

import java.util.UUID;

public class InventoryTestDataFactory {

    @Step("Create test inventory data with name='{productName}', count={count}")
    public static CreateRequest createRequest(final String productName, final long count){
        return CreateRequest.newBuilder()
                .setCount(count)
                .setName(productName)
                .build();
    }

    @Step("Create id message with id='{uuid}'")
    public static IdMessage createIdMessage(final String uuid) {
        return IdMessage.newBuilder()
                .setId(uuid)
                .build();
    }

    @Step("Create id message with id='{uuid}'")
    public static IdMessage createIdMessage(final UUID uuid) {
        return IdMessage.newBuilder()
                .setId(uuid.toString())
                .build();
    }
}
