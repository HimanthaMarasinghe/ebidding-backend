package com.e.bidding.springSecurity.repo;

import com.e.bidding.springSecurity.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<Users, Integer > {

    Users findByUsername(String username);

    @Query("SELECT u.role FROM Users u WHERE u.username = :username")
    String findRoleByUsername(@Param("username") String username);
}
