package dev.soffa.foundation.metrics;

public interface CoreMetrics {

    String AMQP_INVALID_MESSAGE = "app_amqp_invalid_message";
    String AMQP_INVALID_PAYLOAD = "app_amqp_invalid_payload";
    String AMQP_UNSUPPORTED_OPERATION = "app_amqp_unsupported_operation";
    String AMQP_EVENT_PROCESSING_FAILED = "app_amqp_event_processing_failed";
    String AMQP_EVENT_PROCESSED = "app_amqp_event_processed";

    String NATS_EVENT_PROCESSED = "app_nats_event_processed";
    String NATS_EVENT_SKIPPED = "app_nats_event_skipped";
    String NATS_EVENT_PROCESSING_FAILED = "app_nats_event_failed";
    String NATS_REQUEST = "app_nats_request";

    String JOBS = "app_jobs";


    String HTTP_REQUEST = "app_http_request";

    String NATS_PUBLISH = "app_nats_publish";
    String NATS_BROADCAST = "app_nats_broadcast";

    String INVALID_OPERATION = "app_operation_invalid";
    String OPERATION_PREFIX = "app_operation_";
}
