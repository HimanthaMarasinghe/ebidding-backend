package com.e.bidding.user_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    //spring bean for kafka topic
    @Bean
    public NewTopic profileCreationTopic(){
        return TopicBuilder.name("profile_creation_topic").build();
    }

    @Bean
    public NewTopic authUserCreationTopic(){
        return TopicBuilder.name("auth_user_creation_topic").build();
    }
}