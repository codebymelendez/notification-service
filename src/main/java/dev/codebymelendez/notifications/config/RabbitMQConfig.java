package dev.codebymelendez.notifications.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.notifications}")
    private String notificationsExchange;
    
    @Value("${app.rabbitmq.exchange.dlx}")
    private String deadLetterExchange;
    
    @Value("${app.rabbitmq.queue.notifications}")
    private String notificationsQueue;
    
    @Value("${app.rabbitmq.queue.dlq}")
    private String deadLetterQueue;
    
    @Value("${app.rabbitmq.routing-key.notifications}")
    private String notificationsRoutingKey;
    
    @Value("${app.rabbitmq.routing-key.dlq}")
    private String dlqRoutingKey;
    
    // ==================== Exchanges ====================

    @Bean
    public DirectExchange notificationsExchange() {
        return ExchangeBuilder
                .directExchange(notificationsExchange)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
                .directExchange(deadLetterExchange)
                .durable(true)
                .build();
    }
    
    // ==================== Queues ====================
    

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder
                .durable(notificationsQueue)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(deadLetterQueue)
                .build();
    }
    
    // ==================== Bindings ====================
    

    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder
                .bind(notificationsQueue())
                .to(notificationsExchange())
                .with(notificationsRoutingKey);
    }
    

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlqRoutingKey);
    }
    
    // ==================== Message Converter ====================
    

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
