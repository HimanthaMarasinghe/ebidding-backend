package com.e.bidding.springSecurity.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    //spring bean for kafka topic
    @Bean
    public NewTopic userRegistrationTopic(){
        return TopicBuilder.name("user_registration_topic").build();
    }

    @Bean
    public NewTopic authUserCreationResponseTopic(){
        return TopicBuilder.name("auth_user_creation_response_topic").build();
    }
}
