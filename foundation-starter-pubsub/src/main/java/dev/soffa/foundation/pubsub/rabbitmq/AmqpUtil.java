package dev.soffa.foundation.pubsub.rabbitmq;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import com.google.common.collect.ImmutableMap;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.message.pubsub.PubSubClientConfig;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AmqpUtil {

    public static final String TOPIC = ".topic";
    private static final String DLQ = ".dlq";
    private static final String VHOST = "vhost";
    private static final String EXCHANGE = "exchange";
    private static final String USER = "user";
    private static final String PASSWORD = "password";

    private AmqpUtil() {
    }

    public static CachingConnectionFactory createConnectionFactory(String... addresses) {
        if (addresses[0].contains("://embedded")) {
            return new CachingConnectionFactory(new MockConnectionFactory());
        }
        CachingConnectionFactory cf = new CachingConnectionFactory();

        Set<String> mAddresses = new HashSet<>();
        for (String address : addresses) {
            UrlInfo url = UrlInfo.parse(address.trim());
            mAddresses.add(url.getHostnameWithPort());
        }
        String addressList = String.join(",", mAddresses);
        cf.setAddresses(addressList);
        return cf;
    }

    public static RabbitAdmin configure(String applicationName, PubSubClientConfig config) {
        String[] addresses = config.getAddresses().split(",");
        CachingConnectionFactory cf = createConnectionFactory(addresses);

        Map<String, String> props = new HashMap<>();

        UrlInfo url = UrlInfo.parse(addresses[0]);

        if (TextUtil.isNotEmpty(url.getUsername())) {
            props.put(USER, url.getUsername());
            props.put(PASSWORD, url.getPassword());
        }
        if (!props.containsKey(VHOST) && url.hasParam(VHOST)) {
            url.param(VHOST).ifPresent(value -> {
                props.put(VHOST, value);
            });
        }
        if (!props.containsKey(EXCHANGE) && url.hasParam(EXCHANGE)) {
            url.param(EXCHANGE).ifPresent(value -> {
                props.put(EXCHANGE, value);
            });
        }
        /*if (!props.containsKey(ROUTING) && url.hasParam(ROUTING)) {
            url.param(ROUTING).ifPresent(value -> {
                props.put(ROUTING, value);
            });
        }*/
        if (!props.containsKey(VHOST)) {
            props.put(VHOST, "/");
        }
        /*if (!props.containsKey(ROUTING)) {
            props.put(ROUTING, "");
        }*/

        if (props.containsKey(USER)) {
            cf.setUsername(props.get(USER));
            cf.setPassword(props.get(PASSWORD));
        }
        String vhost = props.get(VHOST);
        cf.setVirtualHost(vhost);
        return createBindings(config.getBroadcasting(), applicationName, new RabbitTemplate(cf));
    }

    public static void declareQueue(RabbitAdmin admin, String name) {
        QueueInformation info = admin.getQueueInfo(name);
        if (info == null) {
            admin.declareQueue(new Queue(name));
        }
    }

    public static void createFanoutExchange(RabbitAdmin adm, String exchange, String queue) {
        FanoutExchange ex = new FanoutExchange(exchange);
        declareBinding(adm, queue, ex, "", null, false);
    }

    public static RabbitAdmin createBindings(String group, String appName, RabbitTemplate rabbitTemplate) {
        RabbitAdmin adm = new RabbitAdmin(rabbitTemplate);

        Map<String, Object> args = ImmutableMap.of(
            "x-dead-letter-exchange", appName + DLQ
        );
        Exchange ex = new TopicExchange(appName + TOPIC);
        declareBinding(adm, appName, ex, "", args, true);

        ex = new FanoutExchange(appName + DLQ);
        declareBinding(adm, appName + DLQ, ex, "", null, true);

        if (TextUtil.isNotEmpty(group)) {
            ex = new FanoutExchange(group);
            declareBinding(adm, appName, ex, appName, null, false);
        }

        return adm;

    }

    private static void declareBinding(RabbitAdmin adm, String qName, Exchange ex, String routing, Map<String, Object> args, boolean declareQueue) {
        Queue queue = new Queue(qName, true, false, false, args);

        if (declareQueue) {
            adm.declareQueue(queue);
        }
        adm.declareExchange(ex);
        adm.declareBinding(BindingBuilder.bind(queue).to(ex).with(routing).noargs());

    }

    public static SimpleMessageListenerContainer createListener(RabbitTemplate template, String queueName, String mode) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(template.getConnectionFactory());
        container.addQueueNames(queueName);

        container.setPrefetchCount(1);
        container.setReceiveTimeout(1000 * 5);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setDefaultRequeueRejected(false);

        if ("test".equalsIgnoreCase(mode)) {
            FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
            backOffPolicy.setBackOffPeriod(100);
            MethodInterceptor retryAdvice = RetryInterceptorBuilder.stateless().backOffPolicy(backOffPolicy)
                .maxAttempts(2).build();
            container.setAdviceChain(retryAdvice);
        } else {
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(1000);
            backOffPolicy.setMaxInterval(10_000);
            backOffPolicy.setMultiplier(1.5);
            MethodInterceptor retryAdvice = RetryInterceptorBuilder.stateless().backOffPolicy(backOffPolicy)
                .maxAttempts(7).build();
            container.setAdviceChain(retryAdvice);
        }
        return container;
    }
}


