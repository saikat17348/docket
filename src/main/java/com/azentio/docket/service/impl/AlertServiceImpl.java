package com.azentio.docket.service.impl;

import com.azentio.docket.entity.Alert;
import com.azentio.docket.entity.Case;
import com.azentio.docket.entity.CaseEvent;
import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.Priority;
import com.azentio.docket.repository.AlertRepository;
import com.azentio.docket.repository.CaseEventRepository;
import com.azentio.docket.repository.CaseRepository;
import com.azentio.docket.requestdto.AlertRequest;
import com.azentio.docket.responsedto.CaseSummaryResponse;
import com.azentio.docket.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final CaseRepository caseRepository;
    private final CaseEventRepository caseEventRepository;

    @Value("${app.sla.p1-hours:4}")
    private long p1Hours;

    @Value("${app.sla.p2-hours:24}")
    private long p2Hours;

    @Value("${app.sla.p3-hours:72}")
    private long p3Hours;

    @Override
    @Transactional
    public CaseSummaryResponse ingestAlert(AlertRequest request, String actor) {

        // 1. Idempotency check
        var existingAlert = alertRepository.findByAlertId(request.getAlertId());

        if (existingAlert.isPresent()) {
            return toResponse(existingAlert.get().getCaseEntity());
        }

        Instant now = Instant.now();

        // 2. Find an existing open case for this customer
        Case caseEntity = caseRepository
                .findOpenCaseByCustomerId(request.getCustomerId())
                .orElse(null);

        CaseStatus fromStatus;
        boolean newCase = (caseEntity == null);

        // 3. Create a new case if no open case exists
        if (newCase) {

            Priority priority = calculatePriority(request.getScore());
            long slaSeconds = getSlaDurationSeconds(priority);

            caseEntity = Case.builder()
                    .customerId(request.getCustomerId())
                    .status(CaseStatus.NEW)
                    .priority(priority)
                    .createdAt(now)
                    .updatedAt(now)
                    .slaDurationSeconds(slaSeconds)
                    .dueAt(now.plusSeconds(slaSeconds))
                    .build();

            fromStatus = null;

        } else {

            fromStatus = caseEntity.getStatus();

            // 4. Raise priority only; never lower it
            Priority incomingPriority = calculatePriority(request.getScore());

            if (incomingPriority.ordinal() < caseEntity.getPriority().ordinal()) {

                caseEntity.setPriority(incomingPriority);

                long slaSeconds = getSlaDurationSeconds(incomingPriority);

                caseEntity.setSlaDurationSeconds(slaSeconds);
                caseEntity.setDueAt(
                        caseEntity.getCreatedAt().plusSeconds(slaSeconds)
                );
            }

            caseEntity.setUpdatedAt(now);
        }

        caseEntity = caseRepository.save(caseEntity);

        // 5. Attach the alert
        Alert alert = Alert.builder()
                .alertId(request.getAlertId())
                .customerId(request.getCustomerId())
                .ruleCode(request.getRuleCode())
                .score(request.getScore())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .raisedAt(request.getRaisedAt())
                .caseEntity(caseEntity)
                .build();

        alertRepository.save(alert);

        // 6. Record the audit event
        CaseEvent event = CaseEvent.builder()
                .caseEntity(caseEntity)
                .actor(actor)
                .action("ALERT_ATTACHED")
                .fromStatus(fromStatus)
                .toStatus(caseEntity.getStatus())
                .note("Alert " + request.getAlertId() + " attached")
                .at(now)
                .build();

        caseEventRepository.save(event);

        return toResponse(caseEntity);
    }

    private Priority calculatePriority(int score) {

        if (score >= 80) {
            return Priority.P1;
        }

        if (score >= 50) {
            return Priority.P2;
        }

        return Priority.P3;
    }

    private long getSlaDurationSeconds(Priority priority) {

        return switch (priority) {
            case P1 -> Duration.ofHours(p1Hours).toSeconds();
            case P2 -> Duration.ofHours(p2Hours).toSeconds();
            case P3 -> Duration.ofHours(p3Hours).toSeconds();
        };
    }

    private CaseSummaryResponse toResponse(Case caseEntity) {

        return CaseSummaryResponse.builder()
                .id(caseEntity.getId())
                .customerId(caseEntity.getCustomerId())
                .status(caseEntity.getStatus())
                .priority(caseEntity.getPriority())
                .assigneeId(
                        caseEntity.getAssignee() == null
                                ? null
                                : caseEntity.getAssignee().getId()
                )
                .createdAt(caseEntity.getCreatedAt())
                .dueAt(caseEntity.getDueAt())
                .slaDurationSeconds(caseEntity.getSlaDurationSeconds())
                .build();
    }
}