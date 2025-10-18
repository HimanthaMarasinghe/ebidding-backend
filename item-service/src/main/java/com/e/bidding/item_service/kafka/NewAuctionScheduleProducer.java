package com.e.bidding.item_service.kafka;

import com.e.bidding.dtos.AuctionScheduleEventDTO;
import com.e.bidding.dtos.OutBidNotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static com.vladmihalcea.hibernate.util.LogUtils.LOGGER;

@Slf4j
@Service
public class NewAuctionScheduleProducer {
    private final NewTopic newAuctionScheduleTopic;

    private final KafkaTemplate<String, AuctionScheduleEventDTO> kafkaTemplate;


    public NewAuctionScheduleProducer(NewTopic newAuctionScheduleTopic, KafkaTemplate<String, AuctionScheduleEventDTO> kafkaTemplate) {
        this.newAuctionScheduleTopic = newAuctionScheduleTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void SendMessage(AuctionScheduleEventDTO event){
        log.info("Message to send : "+event.toString());
        Message<AuctionScheduleEventDTO> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC,newAuctionScheduleTopic.name())
                .build();

        kafkaTemplate.send(message);
}

}