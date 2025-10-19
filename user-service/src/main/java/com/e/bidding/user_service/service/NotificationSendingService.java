package com.e.bidding.user_service.service;

import com.e.bidding.dtos.ItemDTO;
import com.e.bidding.dtos.OutBidNotificationDTO;
import com.e.bidding.user_service.model.UserProfile;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class NotificationSendingService {

    private final WebClient webClient;
    private final PushNotificationService pushNotificationService;
    private final MailService mailService;
    private final UserService userService;
    public NotificationSendingService(WebClient.Builder webClientBuilder, @Value("${item.service.url}") String itemServiceUrl, PushNotificationService pushNotificationService, MailService mailService, UserService userService, UserService userService1) {
        this.pushNotificationService = pushNotificationService;
        this.mailService = mailService;
        this.userService = userService1;
        this.webClient = webClientBuilder.baseUrl(itemServiceUrl).build();
    }

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

    public void SendWinningMail(String userName,Integer itemID,Long bidAmount,Integer winningPlace){
        //get Item Details
        try {
            Integer amountToPay=Math.toIntExact(bidAmount);

            ItemDTO response = webClient.get()
                    .uri("/getItem/" + itemID)
                    .retrieve()
                    .bodyToMono(ItemDTO.class)
                    .block();
            assert response != null;

            String itemName=response.getTitle();
            String location=response.getLocation().getName();
            String address=response.getLocation().getAddress();
            String itemDetails=response.getDescription();
            LocalDateTime futureDateTime = LocalDateTime.now().plusDays(3);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a");
            String formattedDateTime = futureDateTime.format(formatter);
            UserProfile userProfile=userService.getDetailsByUserName(userName);


            log.info(response.toString());

            pushNotificationService.sendPushNotifications(userName,"Congratulations!","You have won "+itemName+" for LKR "+bidAmount.toString()+",Please Check your Email for more details");
            mailService.sendWinnerNotification(userName,userProfile.getFirstName()+" "+userProfile.getLastName(),itemID,itemName,itemDetails,formattedDateTime,amountToPay,location+" , "+address,winningPlace);
        }
        catch (Exception e){
            log.error(e.toString());
        }

    }
}
