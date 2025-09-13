package com.e.bidding.bidding_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic outBidAlertEventTopic(){
        return TopicBuilder.name("outbid_alert_topic").build();
    }


}
