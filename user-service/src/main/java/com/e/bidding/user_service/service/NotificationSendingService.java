package com.e.bidding.user_service.service;

import com.e.bidding.dtos.OutBidNotificationDTO;
import jakarta.mail.MessagingException;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationSendingService {
    @Autowired
    private PushNotificationService pushNotificationService;

    @Autowired
    private MailService mailService;

    public void SendOutBidAlerts(OutBidNotificationDTO outbidNotification){
        System.out.println("Outbid notification : "+ outbidNotification.toString());
        String username=outbidNotification.getPrevBidder();
        long prevAmount = outbidNotification.getPrevAmount();
        long newAmount = outbidNotification.getNewAmount();
        Integer itemId= outbidNotification.getItemId();
        String pushNotificationBody="Your Bid of LKR "+ Long.toString(prevAmount) +" on Item "+itemId.toString()+" has been outbid By LKR "+ Long.toString(newAmount) +".";

        try {
            //sending push notifications
            pushNotificationService.sendPushNotifications(username,"Outbid Alert!",pushNotificationBody);

            //sending emails
            mailService.sendOutBidMail(username,itemId.toString(),newAmount,prevAmount);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


    }
}
