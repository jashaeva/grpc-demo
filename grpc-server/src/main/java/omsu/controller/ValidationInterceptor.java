package omsu.controller;

import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidationException;
import io.envoyproxy.pgv.Validator;
import io.envoyproxy.pgv.ValidatorIndex;
import io.grpc.*;
import omsu.dto.LogEvent;
import omsu.kafka.KafkaLogProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationInterceptor implements ServerInterceptor {
    private final ValidatorIndex validatorIndex = new ReflectiveValidatorIndex();
    private static final Logger log = LoggerFactory.getLogger(ValidationInterceptor.class);
    private final KafkaLogProducer kafkaLogProducer;

    public ValidationInterceptor(KafkaLogProducer kafkaLogProducer) {
        this.kafkaLogProducer = kafkaLogProducer;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Оборачиваем call, чтобы предотвратить повторное закрытие
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            private boolean closed = false;

            @Override
            public void close(Status status, Metadata trailers) {
                if (!closed) {
                    closed = true;
                    super.close(status, trailers);
                }
            }
        };

        ServerCall.Listener<ReqT> delegate = next.startCall(wrappedCall, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {

            @Override
            public void onMessage(ReqT message) {
                try {
                    @SuppressWarnings("unchecked")
                    Validator<ReqT> validator = (Validator<ReqT>) validatorIndex.validatorFor(message.getClass());
                    if (validator != null) {
                        validator.assertValid(message);
                    }
                    delegate.onMessage(message);
                } catch (ValidationException e) {
                    log.debug("Validation failed: {}", e.getMessage());
                    kafkaLogProducer.sendLog(new LogEvent(
                            "VALIDATION EXCEPTION",
                            message.toString(),
                            e.getMessage() )
                    );
                    wrappedCall.close(Status.INVALID_ARGUMENT.withDescription(e.getMessage()), new Metadata());
                }
            }
        };
    }
}