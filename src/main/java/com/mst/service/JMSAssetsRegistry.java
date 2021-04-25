package com.mst.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class JMSAssetsRegistry {

	public static final String NOTIFICATION_TOPIC_EXCHANGE_NAME = "NOTIFICATION_EXCH2";

	public static final String NOTIFICATION_EMP_QUEUE_NAME = "NOTIFICATION_QUEUE2";

	public static final String DEAD_LETTER_QUEUE = "NOTIFICATION_DLQ2";

	public static Map<String, Object> args = new HashMap<String, Object>();

	static {
		args.put("x-dead-letter-exchange", "");
		args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUE);

		// args.put("x-message-ttl", 5000);
	}

	@Bean
	Queue notificationQueue() {
		return new Queue(NOTIFICATION_EMP_QUEUE_NAME, true, false, false, args);
	}

	@Bean
	TopicExchange notificationExchange() {
		return new TopicExchange(NOTIFICATION_TOPIC_EXCHANGE_NAME);
	}

	@Bean
	MessageListenerAdapter notificationListenerAdapter(JMSProcess receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	Binding notificationBinding(@Qualifier("notificationQueue") Queue queue,
			@Qualifier("notificationExchange") TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(NOTIFICATION_EMP_QUEUE_NAME);
	}

	@Bean
	SimpleMessageListenerContainer notificationContainer(ConnectionFactory connectionFactory,
			@Qualifier("notificationListenerAdapter") MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(NOTIFICATION_EMP_QUEUE_NAME);
		container.setMessageListener(listenerAdapter);
		container.setConcurrentConsumers(10);
		return container;
	}

	@Bean
	public Queue deadLetterQueue() {
		return new Queue(DEAD_LETTER_QUEUE, true);
	}

}
