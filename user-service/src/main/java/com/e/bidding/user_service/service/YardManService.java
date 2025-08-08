package com.e.bidding.user_service.service;

import com.e.bidding.user_service.model.YardManager;
import com.e.bidding.user_service.repo.YardManagerRepo;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class YardManService {
    private final YardManagerRepo yardManagerRepo;
    private final ModelMapper modelMapper;

    public YardManService(YardManagerRepo yardManagerRepo, ModelMapper modelMapper) {
        this.yardManagerRepo = yardManagerRepo;
        this.modelMapper = modelMapper;
    }
    /**
     * Instantly updates a Yard Manager's assigned yard to an existing one.
     * The asynchronous validation and transaction rollback for this action should be handled separately.
     * @param manId Yard Manager ID
     * @param yardId Yard ID
     */
    @Transactional
    public void changeTheYard(Integer manId, Integer yardId) {
        yardManagerRepo.updateYardId(yardId, manId);
    }

    /**
     * Updates the pending yard ID (-1) for the specified Yard Manager to the newly created yard ID.
     * If the yard creation fails, pass "null" as the yard ID to clear the pending state.
     * This method should be used in the NewLocationIdConsumer to finalize yard assignment.
     *
     * @param manId Yard Manager ID
     * @param yardId Yard ID
     */
    @Transactional
    public void changeThePendingYard(Integer manId, Integer yardId) {
        YardManager yardManager = yardManagerRepo.findById(manId)
                .orElse(null);

        if (yardManager != null && yardManager.getYard_id().equals(-1)) {
            yardManagerRepo.updateYardId(yardId, manId);
        }
    }
}
