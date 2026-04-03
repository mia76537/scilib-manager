package com.scimanager.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scimanager.core.model.UserInterest;

public interface UserInterestRepository extends JpaRepository<UserInterest, String> {

}
