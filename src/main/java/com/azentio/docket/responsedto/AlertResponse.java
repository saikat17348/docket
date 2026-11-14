package com.azentio.docket.responsedto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class AlertResponse {

    private Long id;
    private String alertId;
    private String customerId;
    private String ruleCode;
    private Integer score;
    private BigDecimal amount;
    private String currency;
    private Instant raisedAt;
}