package com.azentio.docket.responsedto;

import com.azentio.docket.enums.CaseAction;
import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.Priority;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class CaseDetailResponse {

    private Long id;
    private String customerId;
    private CaseStatus status;
    private Priority priority;
    private Long assigneeId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant dueAt;
    private Long slaDurationSeconds;

    private List<AlertResponse> alerts;
    private List<CaseEventResponse> timeline;
    private Set<CaseAction> allowedActions;
}