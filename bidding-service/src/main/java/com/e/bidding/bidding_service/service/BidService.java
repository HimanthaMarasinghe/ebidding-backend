package com.e.bidding.bidding_service.service;

import com.e.bidding.bidding_service.dto.BidDTO;
import com.e.bidding.bidding_service.dto.BidHistoryItemDTO;
import com.e.bidding.bidding_service.kafka.OutbidAlertProducer;
import com.e.bidding.bidding_service.model.Bid;
import com.e.bidding.bidding_service.repo.BidRepo;
import com.e.bidding.dtos.ActiveItemBidValidationDTO;
import com.e.bidding.dtos.OutBidNotificationDTO;
import com.e.bidding.dtos.ResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BidService {
    private static final Logger logger = LoggerFactory.getLogger(BidService.class);


    private final BidRepo bidRepo;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final JWTService jwtService;
    private final OutbidAlertProducer outbidAlertProducer;


    public BidService(BidRepo bidRepo, ModelMapper modelMapper, ObjectMapper objectMapper, StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate, JWTService jwtService,OutbidAlertProducer outbidAlertProducer) {
        this.bidRepo = bidRepo;
        this.modelMapper = modelMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.jwtService = jwtService;
        this.outbidAlertProducer=outbidAlertProducer;
    }

    public ResponseDTO<Integer> addBid(BidDTO bidDTO) {
        try {
            bidDTO.setBidTime(LocalDateTime.now(ZoneOffset.UTC));
            // Items status and user's maximum bidding amount Validations goes here
            ActiveItemBidValidationDTO activeItemDto = null;
            try {
                String key = "activeItem:" + bidDTO.getItemId();
                BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
                String cachedItemJson = ops.getAndExpire(java.time.Duration.ofMinutes(10));
                activeItemDto = objectMapper.readValue(cachedItemJson, ActiveItemBidValidationDTO.class);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            if (activeItemDto == null) {
               // Create a REST API request and get details from item service and set it in redis
                return new ResponseDTO<Integer>(false,null, "Cache miss. Item: " + bidDTO.getItemId());
            }
            if (bidDTO.getBidTime().isAfter(activeItemDto.getStartingTime()) && bidDTO.getBidTime().isBefore(activeItemDto.getEndingTime())) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userName = (String) authentication.getPrincipal();
                bidDTO.setBidderUserName(userName);
                //get the current highest bids details before saving the new bid
                Optional <Bid> prevBid = getCurrentHightstBid(bidDTO.getItemId());

                Bid newBid = bidRepo.save(modelMapper.map(bidDTO, Bid.class));
                BidHistoryItemDTO newBidHistoryItemDTO = modelMapper.map(newBid, BidHistoryItemDTO.class);

                messagingTemplate.convertAndSend("/topic/bid:" + bidDTO.getItemId(), newBidHistoryItemDTO);
                newBidHistoryItemDTO.setPlacedByMe(true);
                // 🛑🛑🛑 Warn: Not suitable for Production. This topic need to be authenticated. (When the api gateway is connected all the websocket connection will be coming through it with authentication.)
                messagingTemplate.convertAndSend("/topic/bidder:" + userName, newBidHistoryItemDTO);

                // calling the notification producer for sending the outbid alert to previous bidder
                prevBid.ifPresent(bid ->{
                    String prevBidder= bid.getBidderUserName();
                    Double prevAmount = bid.getAmount();
                    Integer itemId = bid.getItemId();
                    Double newAmount = newBid.getAmount();
                    OutBidNotificationDTO outBidNotification = new OutBidNotificationDTO(prevBidder,itemId,prevAmount,newAmount);
                    //sending outbid details to user service
                    outbidAlertProducer.SendMessage(outBidNotification);

                });

                //update the current highest with the new bid
                try {
                    String redisKey = "currentHighestBid:" + newBid.getItemId();
                    String bidJson = objectMapper.writeValueAsString(newBid);
                    redisTemplate.opsForValue().set(redisKey, bidJson, java.time.Duration.ofMinutes(10)); //10 minute cache
                    logger.info("Saving current highest for caching");
                } catch (Exception e) {
                    logger.error("Error updating Redis with new bid: " + e.getMessage());
                }

                return new ResponseDTO<Integer>(true, newBid.getBidId(), "Bid saved successfully. Bid id: " + newBid.getBidId());
            } else {
                return new ResponseDTO<Integer>(false,null, "Validation failed. Item: " + bidDTO.getItemId());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    public List<BidHistoryItemDTO> getBiddingHistory(int itemId) throws Error{

        List<Bid> bids = bidRepo.getAllBidsForItem(itemId);
        List<BidHistoryItemDTO> bidHistoryItemDTOS = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = (String) authentication.getPrincipal();

        for (Bid bid : bids) {
            BidHistoryItemDTO bidHistoryItemDTO = modelMapper.map(bid, BidHistoryItemDTO.class);
            bidHistoryItemDTO.setPlacedByMe(bid.getBidderUserName() != null && bid.getBidderUserName().equals(userName));
            bidHistoryItemDTOS.add(bidHistoryItemDTO);
        }

        return bidHistoryItemDTOS;
    }

    public Optional<Bid> getCurrentHightstBid(Integer itemId){
        // access the current highest from redis
        String key = "currentHighestBid:" + itemId;
        BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
        String cachedBidJson = ops.get();
        if (cachedBidJson != null) {
            Bid cachedBid = null;
            try {
                cachedBid = objectMapper.readValue(cachedBidJson, Bid.class);
                logger.info("Saving current highest for caching");
                return Optional.of(cachedBid);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        //if current highest is not in redis cache, fetching it from db
        Optional <Bid> currentHighestBids=bidRepo.findTopByItemIdOrderByAmountDesc(itemId);
        return currentHighestBids;
    }


}
