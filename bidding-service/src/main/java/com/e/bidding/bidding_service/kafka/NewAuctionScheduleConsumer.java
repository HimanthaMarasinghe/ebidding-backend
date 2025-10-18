package com.e.bidding.bidding_service.kafka;

import com.e.bidding.bidding_service.scheduler.AuctionScheduler;
import com.e.bidding.dtos.AuctionScheduleEventDTO;
import com.e.bidding.dtos.NewLocationWithYardManDTO;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
public class NewAuctionScheduleConsumer {
    private static final Logger log = LoggerFactory.getLogger(NewAuctionScheduleConsumer.class);
    private final AuctionScheduler auctionScheduler;

    public NewAuctionScheduleConsumer(AuctionScheduler auctionScheduler) {
        this.auctionScheduler = auctionScheduler;
    }

    @KafkaListener(topics = "new_auction_schedule_topic", groupId = "item_auction")
    public void consume(AuctionScheduleEventDTO event) throws SchedulerException {
        log.info("Data recieved");
        log.info(event.toString());

        Date endDate=Date.from(event.getEndTime().atZone(ZoneId.systemDefault()).toInstant());

        auctionScheduler.scheduleAuctionEnd(event.getItemId(),endDate);
    }

    }
