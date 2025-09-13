package com.e.bidding.user_service.kafka;

import com.e.bidding.dtos.OutBidNotificationDTO;
import com.e.bidding.dtos.ProfileCreationEventDTO;
import com.e.bidding.user_service.service.NotificationSendingService;
import com.e.bidding.user_service.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OutBidAlertConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutBidAlertConsumer.class);

    private final NotificationSendingService notificationSendingService;

    public OutBidAlertConsumer(NotificationSendingService notificationSendingService){
        this.notificationSendingService=notificationSendingService;
    }
    @KafkaListener(topics = "outbid_alert_topic",groupId = "user_profile")
    public void consume(OutBidNotificationDTO event) {
        LOGGER.info(String.format("New  event => %s", event.toString()));
        if(event!=null){
            notificationSendingService.SendOutBidAlerts(event);

        }

    }



    }
