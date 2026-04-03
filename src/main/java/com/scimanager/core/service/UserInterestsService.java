package com.scimanager.core.service;

public interface UserInterestsService {
	String getUserInterestProfile(String userId);

	void makeUserInterestProfile(String paperOwnerId);

}
