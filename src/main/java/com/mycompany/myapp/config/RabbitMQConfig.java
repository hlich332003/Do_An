package com.mycompany.myapp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình RabbitMQ cho CinemaTick.
 *
 * Sơ đồ luồng:
 * ┌────────────────────────────────────────────────────────────────────────────────────┐
 * │                         RABBITMQ TOPOLOGY                                          │
 * │                                                                                    │
 * │  [Email Queue]  ←─── ticket.email.exchange ─── ticket.booked                     │
 * │  [Stats Queue]  ←─── ticket.email.exchange ─── ticket.stats                      │
 * │                                                                                    │
 * │  [Delay Queue] ─(TTL 5 phút)──→ [DLX Exchange] ──→ [Payment Timeout Queue]       │
 * │                                  (auto-cancel worker lắng nghe tại đây)           │
 * └────────────────────────────────────────────────────────────────────────────────────┘
 */
@Configuration
public class RabbitMQConfig {

    // ======================== CONSTANTS ========================
    // Email / QR Code Queue
    public static final String EMAIL_QUEUE = "cinematick.email.queue";
    public static final String EMAIL_EXCHANGE = "cinematick.email.exchange";
    public static final String EMAIL_ROUTING_KEY = "ticket.booked";

    // Stats / Dashboard Queue
    public static final String STATS_QUEUE = "cinematick.stats.queue";
    public static final String STATS_ROUTING_KEY = "ticket.stats";

    // Payment Timeout (Delay Queue + Dead Letter)
    public static final String DELAY_QUEUE = "cinematick.payment.delay.queue";
    public static final String TIMEOUT_QUEUE = "cinematick.payment.timeout.queue";
    public static final String TIMEOUT_EXCHANGE = "cinematick.payment.timeout.exchange";
    public static final String TIMEOUT_ROUTING_KEY = "payment.timeout";
    public static final int PAYMENT_TTL_MS = 5 * 60 * 1000; // 5 phút tính theo ms

    // ======================== EMAIL QUEUE ========================

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    @Bean
    public Queue statsQueue() {
        return QueueBuilder.durable(STATS_QUEUE).build();
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding statsBinding(Queue statsQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(statsQueue).to(emailExchange).with(STATS_ROUTING_KEY);
    }

    // ======================== PAYMENT TIMEOUT (DLX Pattern) ========================

    /**
     * Queue chứa hóa đơn chờ thanh toán – TTL 5 phút.
     * Hết TTL, message tự động chuyển sang TIMEOUT_EXCHANGE → TIMEOUT_QUEUE.
     */
    @Bean
    public Queue paymentDelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
            .withArgument("x-dead-letter-exchange", TIMEOUT_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", TIMEOUT_ROUTING_KEY)
            .withArgument("x-message-ttl", PAYMENT_TTL_MS)
            .build();
    }

    /**
     * Dead Letter Exchange – nhận message từ Delay Queue sau khi hết TTL.
     */
    @Bean
    public DirectExchange timeoutExchange() {
        return new DirectExchange(TIMEOUT_EXCHANGE);
    }

    /**
     * Queue chứa các hóa đơn đã hết hạn thanh toán – worker sẽ lắng nghe tại đây.
     */
    @Bean
    public Queue paymentTimeoutQueue() {
        return QueueBuilder.durable(TIMEOUT_QUEUE).build();
    }

    @Bean
    public Binding timeoutBinding(Queue paymentTimeoutQueue, DirectExchange timeoutExchange) {
        return BindingBuilder.bind(paymentTimeoutQueue).to(timeoutExchange).with(TIMEOUT_ROUTING_KEY);
    }

    // ======================== SERIALIZATION & TEMPLATE ========================

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        Jackson2JsonMessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
