package com.azentio.docket.service;

import com.azentio.docket.entity.AppUser;
import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.Priority;
import com.azentio.docket.requestdto.CaseActionRequest;
import com.azentio.docket.responsedto.CaseDetailResponse;
import com.azentio.docket.responsedto.CaseSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CaseService {

    CaseDetailResponse performAction(
            Long caseId,
            CaseActionRequest request,
            AppUser actor
    );

    CaseDetailResponse getCaseDetails(Long caseId, AppUser actor);

    Page<CaseSummaryResponse> getCases(
            CaseStatus status,
            Priority priority,
            Long assigneeId,
            Pageable pageable
    );
}