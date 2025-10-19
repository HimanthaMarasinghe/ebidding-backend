package com.e.bidding.bidding_service.job;

import com.e.bidding.bidding_service.scheduler.AuctionScheduler;
import com.e.bidding.bidding_service.service.AuctionEndService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuctionEndJob implements Job {
    @Autowired
    AuctionScheduler auctionScheduler;
    @Autowired
    AuctionEndService auctionEndService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long itemId = context.getJobDetail().getJobDataMap().getLong("itemID");

        // Logic to find winner and notify them
        System.out.println("Auction with Item ID" + itemId + " ended! Notifying winner.");
        String winningBidder=auctionEndService.handleAuctionEnd(itemId);

        //scheduling claim check after 3 days
        if(winningBidder!=null){
            try{
                auctionScheduler.scheduleClaimCheck(itemId,winningBidder,1);
            }
            catch (SchedulerException e){
                throw new JobExecutionException(e);

            }
        }
    }

}
