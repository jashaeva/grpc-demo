package omsu.inventory.controller;
import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidationException;
import io.envoyproxy.pgv.Validator;
import io.envoyproxy.pgv.ValidatorIndex;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class ValidationInterceptor implements ServerInterceptor {
    private final ValidatorIndex validatorIndex = new ReflectiveValidatorIndex();
    private static final Logger log = LoggerFactory.getLogger(ValidationInterceptor.class);
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {

            @Override
            public void onMessage(ReqT message) {
                try {
                    // Валидация через PGV
                    Validator<ReqT> validator = (Validator<ReqT>) validatorIndex.validatorFor(message.getClass());
                    if (validator != null) {
                        validator.assertValid(message);
                    }
                    delegate.onMessage(message);
                } catch (ValidationException e) {
                    // Вместо call.close() просто выбрасываем исключение
                    throw Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
                }
            }
        };
    }
}