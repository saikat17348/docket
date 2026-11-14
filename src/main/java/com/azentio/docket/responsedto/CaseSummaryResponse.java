package com.azentio.docket.responsedto;

import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.Priority;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CaseSummaryResponse {

    private Long id;
    private String customerId;
    private CaseStatus status;
    private Priority priority;
    private Long assigneeId;
    private Instant createdAt;
    private Instant dueAt;
    private Long slaDurationSeconds;
}