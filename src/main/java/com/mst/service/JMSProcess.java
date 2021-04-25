package com.mst.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mst.service.dto.JMSRecord;

@Service
public class JMSProcess {
	
	private static final Logger LOG = LoggerFactory.getLogger(JMSProcess.class);
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();
	
	public void send(JMSRecord record) {
		try {
			rabbitTemplate.convertAndSend(JMSAssetsRegistry.NOTIFICATION_EMP_QUEUE_NAME, objectMapper.writeValueAsString(record));
		} catch (Exception e) {
			LOG.error(" Error while sending Message " + record, e);
		}
	}

	public void receiveMessage(String record) {
		JMSRecord notificationRecord = null;
		try {
				notificationRecord = objectMapper.readValue(record, JMSRecord.class);
				LOG.debug("Message received " + notificationRecord);
					
			} catch (Exception e) {
				LOG.error(" Error while receiving Message " + record, e);
				throw new AmqpRejectAndDontRequeueException(e.getMessage());
			}
	}

}
