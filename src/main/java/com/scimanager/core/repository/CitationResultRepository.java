package com.scimanager.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scimanager.core.model.CitationResult;

@Repository
public interface CitationResultRepository extends JpaRepository<CitationResult, Long> {
	// 这里不需要写 saveAll，JpaRepository 已经自带了该方法
}