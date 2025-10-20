package com.e.bidding.bidding_service.service;

import com.e.bidding.bidding_service.kafka.WinningMessageProducer;
import com.e.bidding.bidding_service.model.AuctionWinner;
import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.repo.AuctionWinnerRepo;
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.dtos.WinningMessageDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuctionEndService {
    private final AuctionWinnerRepo auctionWinnerRepo;
    private final BidService bidService;
    private final WinningMessageProducer winningMessageProducer;


    public AuctionEndService(AuctionWinnerRepo auctionWinnerRepo, BidService bidService, WinningMessageProducer winningMessageProducer) {
        this.auctionWinnerRepo = auctionWinnerRepo;
        this.bidService = bidService;
        this.winningMessageProducer = winningMessageProducer;
    }

    public String handleAuctionEnd (long itemId){
        Integer itemID=Math.toIntExact(itemId);
        Optional<Bid> winnerBid=bidService.getHighestBidder(itemID);

        if(winnerBid.isPresent()){
            String winner=winnerBid.get().getBidderUserName();
            Long bidAmount=winnerBid.get().getAmount();
            AuctionWinner auctionWinner=new AuctionWinner();
            auctionWinner.setItemId(itemID);
            auctionWinner.setWinnerUserName(winner);
            auctionWinner.setClaimed(false);
            auctionWinner.setWinningPlace(1);
            auctionWinner.setBidAmount(bidAmount);
            auctionWinnerRepo.save(auctionWinner);

            //notifying user logic here
            WinningMessageDTO winningMessageEvent=new WinningMessageDTO(itemID,winner,bidAmount,1);
            winningMessageProducer.SendMessage(winningMessageEvent);
            return winner;
        }
        return null;
    }



    public boolean checkClaimed (Integer itemId,String userName){
        AuctionWinner auctionWinner=auctionWinnerRepo.findFirstByItemIdAndWinnerUserName(itemId,userName);
        return auctionWinner.isClaimed();
    }

    public void handleNewWinnerSet(Integer itemId,String userName,Integer winningPlace,long bid,String prevWinner){
        discardPrevWinner(prevWinner,itemId); //discard prev winner
        AuctionWinner auctionWinner=new AuctionWinner();
        auctionWinner.setItemId(itemId);
        auctionWinner.setWinnerUserName(userName);
        auctionWinner.setClaimed(false);
        auctionWinner.setWinningPlace(winningPlace);
        auctionWinner.setBidAmount(bid);
        auctionWinnerRepo.save(auctionWinner);

        WinningMessageDTO winningMessageEvent=new WinningMessageDTO(itemId,userName,bid,winningPlace);
        winningMessageProducer.SendMessage(winningMessageEvent);

    }

    private void discardPrevWinner(String prevWinner,Integer itemId){
        AuctionWinner itemtoDiscard=auctionWinnerRepo.findFirstByItemIdAndWinnerUserName(itemId,prevWinner);
        if(itemtoDiscard !=null ){
            itemtoDiscard.setDiscarded(true);
            auctionWinnerRepo.save(itemtoDiscard);
        }
    }
}
