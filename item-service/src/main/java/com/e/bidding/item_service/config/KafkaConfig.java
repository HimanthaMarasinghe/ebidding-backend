package com.e.bidding.item_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic locationApprovedTopic(){
        return TopicBuilder.name("location_approved_topic").build();
    }
}
