package com.e.bidding.bidding_service.kafka;

import com.e.bidding.dtos.WinningMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WinningMessageProducer {
    private final NewTopic winningMessageTopic;
    private final KafkaTemplate<String, WinningMessageDTO> kafkaTemplate;


    public WinningMessageProducer(NewTopic winningMessageTopic , KafkaTemplate<String, WinningMessageDTO> kafkaTemplate) {
        this.winningMessageTopic = winningMessageTopic;
        this.kafkaTemplate=kafkaTemplate;
    }
    public void SendMessage(WinningMessageDTO event){
        log.info("Message to be sent : {}",event);
        Message<WinningMessageDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC,winningMessageTopic.name())
                .build();
        kafkaTemplate.send(message);
    }
}
