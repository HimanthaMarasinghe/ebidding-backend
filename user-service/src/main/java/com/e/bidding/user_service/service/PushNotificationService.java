package com.e.bidding.user_service.service;

import com.e.bidding.user_service.repo.UserProfileRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PushNotificationService {
    @Autowired
    NotificationService notificationService;

    @Autowired
    UserProfileRepo userProfileRepo;

    private static final String EXPO_API_URL = "https://exp.host/--/api/v2/push/send";
    private static final int BATCH_SIZE = 100; // notifications per a  request




    public String sendPushNotifications(String userName,String title,String body){
        Integer UserId=userProfileRepo.findByUsername(userName).getId();
        List<String> tokenList=notificationService.getPushTokensByUserID(UserId);
        if (tokenList == null || tokenList.isEmpty()) {
            return "No Tokens for the username";
        }
        List<Map<String, Object>> messages = tokenList.stream().map(
                token ->{
                    Map<String,Object> m = new HashMap<>();
                    m.put("to",token);
                    m.put("title",title);
                    m.put("body",body);
                    m.put("sound","default");
                    // further developments : m.put("data", Map.of("someKey", "someValue"));
                    return m;
                }
        ).toList();

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for(int i=0;i<=messages.size();i+=BATCH_SIZE){
            int end = Math.min(i + BATCH_SIZE, messages.size());
            List<Map<String, Object>> batch = messages.subList(i, end);

            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(batch, headers);
            ResponseEntity<String> response = rt.postForEntity(EXPO_API_URL, request, String.class);

            System.out.println("Expo response (batch " + (i / BATCH_SIZE + 1) + "): " + response.getStatusCode());
            System.out.println(response.getBody());

            return "Notifications queued for user : " + userName;


        }
        System.out.println(messages);
        return "Notifications Sended";
    }

}
