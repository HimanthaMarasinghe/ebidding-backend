package com.e.bidding.bidding_service.service;

import com.e.bidding.bidding_service.dto.AutoBidDTO;
import com.e.bidding.bidding_service.dto.BidDTO;
import com.e.bidding.bidding_service.dto.BidHistoryItemDTO;
import com.e.bidding.bidding_service.dto.MyAutoBidDTO;
import com.e.bidding.bidding_service.kafka.OutbidAlertProducer;
import com.e.bidding.bidding_service.model.AutoBid;
import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.repo.AutoBidRepo;
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.dtos.ActiveItemBidValidationDTO;
import com.e.bidding.dtos.OutBidNotificationDTO;
import com.e.bidding.dtos.ResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class BidService {
    private static final Logger logger = LoggerFactory.getLogger(BidService.class);


    private final BidRepo bidRepo;
    private final AutoBidRepo autoBidRepo;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final OutbidAlertProducer outbidAlertProducer;


    public BidService(BidRepo bidRepo, AutoBidRepo autoBidRepo, ModelMapper modelMapper, ObjectMapper objectMapper, StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate, OutbidAlertProducer outbidAlertProducer) {
        this.bidRepo = bidRepo;
        this.autoBidRepo = autoBidRepo;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.outbidAlertProducer=outbidAlertProducer;
    }

    /**
     * Places a bid
     * @param bidDTO new bid that should be placed
     * @param calledFromAutoBid If true, the method will not enforce the increment value.
     *                          Since the bidders do not know whether there is a autoBid or not, a bidder can place
     *                          a laser amount as the autoBid. In such case `setAutoBid` method will call this method
     *                          with `calledFromAutoBid` as true.
     * @return ResponseDTO with the bid id as the data.
     */
    public ResponseDTO<Long> addBid(BidDTO bidDTO, boolean calledFromAutoBid) {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            bidDTO.setBidTime(now);

            Optional<ActiveItemBidValidationDTO> opActiveItemDto = getActiveItemData(bidDTO.getItemId());
            if (opActiveItemDto.isEmpty())
                return new ResponseDTO<>(false,null, "Active item not found for ID : " + bidDTO.getItemId());

            ActiveItemBidValidationDTO activeItemDto = opActiveItemDto.get();

            // Todo: User's maximum bidding amount Validations goes here. Create a new private method for it and call it here

            bidDTO.setAutoBid(calledFromAutoBid);
            if(!calledFromAutoBid) {
                //If called from autoBid bidDTO already have the correct username. (New autoBidder or old one)
                //If called as a normal bid, username should be got from authentication object
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userName = (String) authentication.getPrincipal();
                bidDTO.setBidderUserName(userName);
            }

            //get the current highest bids details before saving the new bid
            Optional<Bid> prevBid = getCurrentHighestBid(bidDTO.getItemId());

            //Validation
            if (prevBid.isPresent()) {
                Bid bid = prevBid.get();

                //normal bid
                if (!calledFromAutoBid && bidDTO.getAmount() != activeItemDto.getIncrement() + bid.getAmount())
                    return new ResponseDTO<>(false, null, "Invalid Bid Amount");

                //autoBid
                // amount should be, a multiple of increment + startingBid
                // (bidAmount - startingBid) % increment != 0
                if ((bidDTO.getAmount() - activeItemDto.getStartingBid()) % activeItemDto.getIncrement() != 0)
                    return new ResponseDTO<>(false, null, "Invalid BId Amount");

                //winner bidding again
                //Winner can not place a normal bid again
                //But if this bid is calledFromAutoBid, because of another user's autoBid, it is valid. (the only case where a user can have two adjacent bids for one item)
                if(!calledFromAutoBid && bid.getBidderUserName().equals(bidDTO.getBidderUserName()))
                    return new ResponseDTO<>(false, null, "Current winner can not bid again");
            } else if(activeItemDto.getStartingBid() != bidDTO.getAmount()) {
                return new ResponseDTO<>(false, null, "First bid should be exactly equal to starting bid amount");
            }


            Bid newBid = bidRepo.save(modelMapper.map(bidDTO, Bid.class));
            BidHistoryItemDTO newBidHistoryItemDTO = modelMapper.map(newBid, BidHistoryItemDTO.class);

            //update the current highest with the new bid immediately updating the DB
            setCurrentHighestBidInRedis(newBid);

            messagingTemplate.convertAndSend("/topic/bid:" + bidDTO.getItemId(), newBidHistoryItemDTO);
            newBidHistoryItemDTO.setPlacedByMe(true);
            // 🛑🛑🛑 Warn: Not suitable for Production. This topic need to be authenticated. (When the api gateway is connected all the websocket connection will be coming through it with authentication.)
            messagingTemplate.convertAndSend("/topic/bidder:" + bidDTO.getBidderUserName(), newBidHistoryItemDTO);

            Optional<AutoBidDTO> autoBid = getAutoBid(bidDTO.getItemId());
            long nextBidAmount = bidDTO.getAmount() + activeItemDto.getIncrement();
            //Case of Valid autoBid Presents
            if (autoBid.isPresent() && autoBid.get().getAmount() >= nextBidAmount && !autoBid.get().getBidderUserName().equals(bidDTO.getBidderUserName())) {
                    AutoBidDTO autoBidDTO = autoBid.get();
                    BidDTO imidiateNewBidDTO =  new BidDTO(null, autoBidDTO.getBidderUserName(), bidDTO.getItemId(), nextBidAmount, now, true);
                    try {
                        Bid imidiateNewBid = bidRepo.save(modelMapper.map(imidiateNewBidDTO, Bid.class));
                        BidHistoryItemDTO newAutoBidHistoryItemDto = modelMapper.map(imidiateNewBid, BidHistoryItemDTO.class);
                        //update the current highest with the new bid immediately updating the DB
                        setCurrentHighestBidInRedis(imidiateNewBid);

                        messagingTemplate.convertAndSend("/topic/bid:" + autoBidDTO.getItemId(), newAutoBidHistoryItemDto);
                        newAutoBidHistoryItemDto.setPlacedByMe(true);
                        messagingTemplate.convertAndSend("/topic/bidder:" + autoBidDTO.getBidderUserName(), newAutoBidHistoryItemDto);

                        //Todo: send notification to the autoBidder that his bid was incremented

                    } catch (Exception e) {
                        logger.error("AutoBid failed on new bid for item : {}", bidDTO.getItemId());
                    }
            }

            // calling the notification producer for sending the outbid alert to previous bidder
            prevBid.ifPresent(bid ->{
                String prevBidder= bid.getBidderUserName();
                long prevAmount = bid.getAmount();
                Integer itemId = bid.getItemId();
                long newAmount = newBid.getAmount();
                OutBidNotificationDTO outBidNotification = new OutBidNotificationDTO(prevBidder,itemId,prevAmount,newAmount);
                //sending outbid details to user service
                outbidAlertProducer.SendMessage(outBidNotification);

            });

            return new ResponseDTO<Long>(true, newBid.getBidId(), "Bid saved successfully. Bid id: " + newBid.getBidId());

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    private void setCurrentHighestBidInRedis(Bid newBid) {
        try {
            String redisKey = "currentHighestBid:" + newBid.getItemId();
            String bidJson = objectMapper.writeValueAsString(newBid);
            redisTemplate.opsForValue().set(redisKey, bidJson, java.time.Duration.ofMinutes(10)); //10 minute cache
            logger.info("Saving current highest for caching");
        } catch (Exception e) {
            logger.error("Error updating Redis with new bid: " + e.getMessage());
        }
    }

    public List<BidHistoryItemDTO> getBiddingHistory(int itemId) throws Error{

        List<Bid> bids = bidRepo.getAllBidsForItem(itemId);
        List<BidHistoryItemDTO> bidHistoryItemDTOS = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (String) authentication.getPrincipal();

        // TODO: There’s a small chance that two bids may arrive within a few milliseconds,
        //       before the first request finishes updating the current winning bid.
        //       In this case, for bids with the same amount, the one with the earlier timestamp
        //       should be accepted and the other omitted when returning the bid history.
        //       To keep the database strictly consistent, it’s better to implement validation at the database level.


        for (Bid bid : bids) {
            BidHistoryItemDTO bidHistoryItemDTO = modelMapper.map(bid, BidHistoryItemDTO.class);
            bidHistoryItemDTO.setPlacedByMe(bid.getBidderUserName() != null && bid.getBidderUserName().equals(userName));
            bidHistoryItemDTOS.add(bidHistoryItemDTO);
        }

        bidHistoryItemDTOS.sort(Comparator
                .comparing(BidHistoryItemDTO::getBidTime)
                .thenComparingLong(BidHistoryItemDTO::getAmount)
                .reversed()
        );

        return bidHistoryItemDTOS;
    }

    private Optional<Bid> getCurrentHighestBid(Integer itemId){
        // access the current highest from redis
        String key = "currentHighestBid:" + itemId;
        BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
        String cachedBidJson = ops.getAndExpire(java.time.Duration.ofMinutes(10));
        if (cachedBidJson != null) {
            Bid cachedBid = null;
            try {
                cachedBid = objectMapper.readValue(cachedBidJson, Bid.class);
                if(cachedBid.getAmount() == 0L) return  Optional.empty();
                return Optional.of(cachedBid);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        //if current highest is not in redis cache, fetching it from db
        Optional <Bid> currentHighestBids = bidRepo.findTopByItemIdOrderByAmountDesc(itemId);

        //save the current highest bid in redis. If no bids yet, save `0`, so next time it checks, no need to check the DB again.
        try {
            Bid curHigBid = currentHighestBids.orElseGet(() -> new Bid(null, null, itemId, 0L, null, false));
            String json = objectMapper.writeValueAsString(curHigBid);
            redisTemplate.opsForValue().set(key, json, java.time.Duration.ofMinutes(10));
            logger.info("Saved current highest for caching");
        } catch (Exception e) {
            logger.error("Error saving currentHighest bid in redis");
        }

        return currentHighestBids;
    }

    @Transactional
    public ResponseDTO<AutoBidDTO> setAutoBid(AutoBidDTO autoBidDto) {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

            //Validation
            Optional<ActiveItemBidValidationDTO> opActiveItemDto = getActiveItemData(autoBidDto.getItemId(), now);
            if (opActiveItemDto.isEmpty())
                return new ResponseDTO<>(false,null, "Item not found for ID : " + autoBidDto.getItemId());

            ActiveItemBidValidationDTO activeItemDto = opActiveItemDto.get();

            //Set username from jwt key
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = (String) authentication.getPrincipal();
            autoBidDto.setBidderUserName(userName);

            Optional<Bid> currentHighestBid = getCurrentHighestBid(autoBidDto.getItemId());
            //If there is a winning bid validate the autoBid against it.
            if(currentHighestBid.isPresent()){
                Bid curHighBid = currentHighestBid.get();
                //Valid autoBid amount is depend on whether the autoBidder is same as the current winner or not.
                long adjustIncrement = curHighBid.getBidderUserName().equals(userName) ? 0 : activeItemDto.getIncrement();
                //Should be, new autoBid >= current wining bid + adjustIncrement + 2*increment
                if(curHighBid.getAmount() + adjustIncrement + (2L * activeItemDto.getIncrement()) > autoBidDto.getAmount())
                    return new ResponseDTO<>(false, null, "Amount should be greater than the current highest bid + 2*increment.");
                //(new autoBid - winning bid - adjustIncrement) % 2*increment should be 0
                if((autoBidDto.getAmount() - curHighBid.getAmount() - adjustIncrement) % (2L * activeItemDto.getIncrement()) != 0)
                    return new ResponseDTO<>(false, null, "(new autoBid - winning bid) % 2*increment should be 0");
            }

            //Else (There are no bids yet) new autoBid amount should be greater than the starting bid + 2*increment and (autoBid - starting bid) % 2*increment should be 0
            else if(
                    autoBidDto.getAmount() < (activeItemDto.getStartingBid() + 2L*activeItemDto.getIncrement()) ||
                    (autoBidDto.getAmount() - activeItemDto.getStartingBid()) % (2L*activeItemDto.getIncrement()) != 0
            )
                return new ResponseDTO<>(false, null, "AutoBid amount should be greater than the starting bid + 2*increment and (autoBid - starting bid) % 2*increment should be 0");

            // Todo: User's maximum bidding amount Validations goes here. Create a new private method for it and call it here


            //Get current max autoBid BEFORE SAVING THE NEW ONE
            Optional<AutoBidDTO> currentAutoBid = getAutoBid(autoBidDto.getItemId());

            if(currentAutoBid.isPresent()){
                AutoBidDTO cb = currentAutoBid.get();
                //If you are the current autoBidder for this item, you are not allowed to set an autoBid with the same amount or less
                if(cb.getBidderUserName().equals(userName) && cb.getAmount() >= autoBidDto.getAmount())
                    return new ResponseDTO<>(false, null, "You are the current autoBidder for this item and you are not allowed to set a autoBid with the same amount or less");
            }

            //Save in DB anyway for accountability.
            AutoBid autoBid = autoBidRepo.save(modelMapper.map(autoBidDto, AutoBid.class));

            //Prepare data to save In Redis
            String Key = "autoBid:" + autoBidDto.getItemId();
            String json = objectMapper.writeValueAsString(autoBid); //Assign new autoBid

            //If there is a current winning bid, immediate bid should be (Winning bid + increment). Else, it should be starting bid.
            long immediateBidAmount = currentHighestBid.isPresent() ? currentHighestBid.get().getAmount() + activeItemDto.getIncrement() : activeItemDto.getStartingBid();
            String immediateBidderUserName = autoBidDto.getBidderUserName();

            if(currentAutoBid.isPresent()) {
                AutoBidDTO cb = currentAutoBid.get();
                //Note: current autoBid and new autoBid can not be equal at this point. Because
                //      1. current auto bidder can not place less or equal. (validated above)
                //      2. current auto bidder is the current winner. Auto Bid amounts that can placed by winner and others are defer.
                if (cb.getAmount() > autoBidDto.getAmount()){
                    //New autoBid is lesser than the current AutoBid.
                    immediateBidAmount = autoBidDto.getAmount();
                    json = objectMapper.writeValueAsString(cb); //Replace `json` since the current autoBid is greater than the new one.
                }
                else {
                    //New autoBid is greater than the current AutoBid.
                    immediateBidAmount = cb.getAmount();
                    immediateBidderUserName = cb.getBidderUserName();
                }
            }
            redisTemplate.opsForValue().set(Key, json, java.time.Duration.ofMinutes(10)); // Here 'json' has the greatest value between current amount and new amount.

            //Place immediate bid. If current autoBidder raising his autoBid amount, skip this step.
            if(!(currentAutoBid.isPresent() && currentAutoBid.get().getBidderUserName().equals(userName))){
                BidDTO immediateBid = new BidDTO(null, immediateBidderUserName, autoBid.getItemId(), immediateBidAmount, now, true);
                addBid(immediateBid, true);
            }


            return new ResponseDTO<>(true, modelMapper.map(autoBid, AutoBidDTO.class), "Auto Bid Placed successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Optional<AutoBidDTO> getAutoBid(Integer itemId) {
        try {
            // Try redis
            String key = "autoBid:" + itemId;
            BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
            String cachedAutoBidJson = ops.get();
            if (cachedAutoBidJson != null) {
                AutoBidDTO cachedAutoBid = objectMapper.readValue(cachedAutoBidJson, AutoBidDTO.class);
                if(cachedAutoBid.getAmount() == 0L) return Optional.empty();
                return Optional.of(cachedAutoBid);
            }
            // From DB
            Optional<AutoBidDTO> autoBidDto = autoBidRepo.findTopByItemIdOrderByAmountDesc(itemId)
                    .map(bid -> modelMapper.map(bid, AutoBidDTO.class));

            //Save in redis
            String json;
            if (autoBidDto.isPresent())
                json = objectMapper.writeValueAsString(autoBidDto.get());
            else
                //Set 0 if there is no autoBids, so next bid does not have to check DB aging
                json = objectMapper.writeValueAsString(new AutoBidDTO(null, null, itemId, 0L, null));

            redisTemplate.opsForValue().set(key, json, java.time.Duration.ofMinutes(10));

            return autoBidDto;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get active item for the given id at a given time.
     * @param itemId id of the Active item
     * @param now time check the item's state against
     * @return Optional of `ActiveItemBidValidationDTO`. If the item does not exist or not active :  Optional.empty()
     */
    private Optional<ActiveItemBidValidationDTO> getActiveItemData(Integer itemId, LocalDateTime now) {
        ActiveItemBidValidationDTO activeItemDto = null;
        try {
            String key = "activeItem:" + itemId;
            BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
            String cachedItemJson = ops.getAndExpire(java.time.Duration.ofMinutes(10));
            activeItemDto = objectMapper.readValue(cachedItemJson, ActiveItemBidValidationDTO.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        if (activeItemDto == null) {
            // Todo : Create a REST API request and get details from item service and set it in redis
            return Optional.empty(); //For now
        }
        
        // Active State validation.
        if (now.isAfter(activeItemDto.getStartingTime()) && now.isBefore(activeItemDto.getEndingTime()))
            return Optional.of(activeItemDto);

        return Optional.empty();
    }

    /**
     * Get active item for the given id at now.
     * @param itemId id of the Active item
     * @return Optional of `ActiveItemBidValidationDTO`. If the item does not exist or not active :  Optional.empty()
     */
    private Optional<ActiveItemBidValidationDTO> getActiveItemData(Integer itemId){
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return getActiveItemData(itemId, now);
    }

    public MyAutoBidDTO getMyAutoBid(Integer itemId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (String) authentication.getPrincipal();

        Optional<AutoBid> autoBid = autoBidRepo.findTopByItemIdAndBidderUserNameOrderByAmountDesc(itemId, userName);
        return autoBid.map(bid -> modelMapper.map(bid, MyAutoBidDTO.class)).orElse(null);
    }
}
