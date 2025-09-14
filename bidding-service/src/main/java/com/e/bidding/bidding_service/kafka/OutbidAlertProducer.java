package com.e.bidding.bidding_service.kafka;

import com.e.bidding.dtos.OutBidNotificationDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OutbidAlertProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutbidAlertProducer.class);

    private final NewTopic outBidAlertEventTopic;

    private final KafkaTemplate<String, OutBidNotificationDTO> kafkaTemplate;

    public OutbidAlertProducer(NewTopic outBidAlertEventTopic,KafkaTemplate<String,OutBidNotificationDTO> kafkaTemplate){
        this.outBidAlertEventTopic=outBidAlertEventTopic;
        this.kafkaTemplate=kafkaTemplate;
    }

    public void SendMessage(OutBidNotificationDTO event){
        LOGGER.info(String.format("Outbid alert to be sent : %s",event.toString()));

        Message<OutBidNotificationDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC,outBidAlertEventTopic.name())
                .build();

        kafkaTemplate.send(message);

    }

}
