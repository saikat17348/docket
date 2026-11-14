package com.azentio.docket.responsedto;

import com.azentio.docket.enums.CaseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CaseEventResponse {

    private Long id;
    private String actor;
    private String action;
    private CaseStatus fromStatus;
    private CaseStatus toStatus;
    private String note;
    private Instant at;
}