package com.e.bidding.bidding_service.job;

import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.bidding_service.scheduler.AuctionScheduler;
import com.e.bidding.bidding_service.service.AuctionEndService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class CheckUnclaimedJob implements Job {
    @Autowired
    AuctionScheduler auctionScheduler;

    @Autowired
    AuctionEndService auctionEndService;

    @Autowired
    BidRepo bidRepo;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long itemId = context.getJobDetail().getJobDataMap().getLong("itemID");
        String winnerUsername = context.getJobDetail().getJobDataMap().getString("bidderIndex");
        long winningPlace = context.getJobDetail().getJobDataMap().getLong("winning_place");

        // System.out.println("Auction with Item ID" + itemId + " is unclaimed notify the next winner.");
        boolean claimed = auctionEndService.checkClaimed(Math.toIntExact(itemId),winnerUsername);

        if(!claimed){
            long nextWinningPlace=winningPlace+1;

            Optional<Bid> nextHighestBid=bidRepo.findBidByPlace(Math.toIntExact(itemId),Math.toIntExact(nextWinningPlace));
            if(nextHighestBid.isPresent()) {
                log.info("Auction with Item ID{} is unclaimed notify the next winner by .{}", itemId, nextHighestBid.get().getBidderUserName());

                auctionEndService.handleNewWinnerSet(Math.toIntExact(itemId),nextHighestBid.get().getBidderUserName(),Math.toIntExact(nextWinningPlace),nextHighestBid.get().getAmount(),winnerUsername);
                //logic to get the next winner from db by giving a wining place
                try {
                    auctionScheduler.scheduleClaimCheck(itemId, nextHighestBid.get().getBidderUserName(), nextWinningPlace);
                } catch (SchedulerException e) {
                    throw new JobExecutionException(e);
                }
            }


        }



    }

}
