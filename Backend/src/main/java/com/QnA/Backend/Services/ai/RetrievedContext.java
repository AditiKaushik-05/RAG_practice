package com.QnA.Backend.Services.ai;

import com.QnA.Backend.dto.CitationDto;

import java.util.List;

public record RetrievedContext(
        List<CitationDto> citations,
        String contextText) {
}
