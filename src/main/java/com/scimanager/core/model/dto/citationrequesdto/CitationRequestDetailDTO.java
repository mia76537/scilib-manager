package com.scimanager.core.model.dto.citationrequesdto;

import java.util.List;

import lombok.Data;

@Data
public class CitationRequestDetailDTO extends CitationRequestSummaryDTO {
	private CitationCriteriaDTO citationCriteria;
	private List<CitationItemDTO> citationItems;
}