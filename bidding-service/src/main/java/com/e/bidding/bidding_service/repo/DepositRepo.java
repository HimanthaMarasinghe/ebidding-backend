package com.e.bidding.bidding_service.repo;

import com.e.bidding.bidding_service.model.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepositRepo extends JpaRepository<Deposit, Integer> {

    Deposit findFirstByUserNameOrderByBidTimeDesc(String userName);
}
