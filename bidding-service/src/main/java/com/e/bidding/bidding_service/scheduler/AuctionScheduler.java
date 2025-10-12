package com.e.bidding.bidding_service.scheduler;

import com.e.bidding.bidding_service.job.AuctionEndJob;
import com.e.bidding.bidding_service.job.CheckUnclaimedJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class AuctionScheduler {
    @Autowired
    private Scheduler scheduler;

    public void scheduleAuctionEnd(Long itemId, Date endTime) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(AuctionEndJob.class)
                .withIdentity("auctionJob" + itemId)
                .usingJobData("itemID", itemId)
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("auctionTrigger" + itemId)
                .startAt(endTime)  // When the auction ends
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow()) // Handle missed executions
                .build();
        log.info("Scheduled Job will run at : {}  on Item: {}", endTime.toString(),itemId);
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void scheduleClaimCheck(Long itemId,String bidderUserName,long winningPlace) throws SchedulerException{
        LocalDateTime runTime=LocalDateTime.now().plusDays(3); // the dates to claim the item (default is 3 , this can be changed by setting time to claim in  db.)
        Date checkTime=Date.from(runTime.atZone(ZoneId.systemDefault()).toInstant());

        JobDetail jobDetail=JobBuilder.newJob(CheckUnclaimedJob.class)
                .withIdentity("claimCheckJob_" + itemId + "_" + bidderUserName)
                .usingJobData("itemID", itemId)
                .usingJobData("bidderIndex", bidderUserName)
                .usingJobData("winning_place",winningPlace)
                .storeDurably()
                .build();

        Trigger trigger=TriggerBuilder.newTrigger()
                .withIdentity("claimCheckTrigger_" + itemId + "_" + bidderUserName)
                .startAt(checkTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        log.info("Scheduled job for checking claim will run at : {} on Item : {} for user : {} winningPlace : {}",checkTime,itemId,bidderUserName,winningPlace);
        scheduler.scheduleJob(jobDetail,trigger);
    }

}
