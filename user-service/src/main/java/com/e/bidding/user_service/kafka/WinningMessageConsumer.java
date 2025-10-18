package com.e.bidding.user_service.kafka;

import com.e.bidding.dtos.WinningMessageDTO;
import com.e.bidding.user_service.service.NotificationSendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class WinningMessageConsumer {
    @Autowired
    NotificationSendingService notificationSendingService;

    @KafkaListener(topics = "winning_message_topic", groupId = "user_profile")
    public void consume(WinningMessageDTO event) {
        System.out.println(event.toString());
        String winnerUserName = event.getWinner();
        Integer itemId = event.getItemId();
        Long bidAmount=event.getBidAmount();
        Integer winningPlace=event.getWinningPlace();
        log.info("Notify : {} - {}", winnerUserName, itemId);
        notificationSendingService.SendWinningMail(winnerUserName,itemId,bidAmount,winningPlace);
    }
}
