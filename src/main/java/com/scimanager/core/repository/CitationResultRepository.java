package com.scimanager.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scimanager.core.model.CitationResult;

@Repository
public interface CitationResultRepository extends JpaRepository<CitationResult, Long> {

	void deleteByItemIdIn(List<Long> itemIds);

}