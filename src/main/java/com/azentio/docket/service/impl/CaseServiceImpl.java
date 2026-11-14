package com.azentio.docket.service.impl;

import com.azentio.docket.entity.AppUser;
import com.azentio.docket.entity.Case;
import com.azentio.docket.entity.CaseEvent;
import com.azentio.docket.enums.CaseAction;
import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.Priority;
import com.azentio.docket.repository.AlertRepository;
import com.azentio.docket.repository.AppUserRepository;
import com.azentio.docket.repository.CaseEventRepository;
import com.azentio.docket.repository.CaseRepository;
import com.azentio.docket.requestdto.CaseActionRequest;
import com.azentio.docket.responsedto.*;
import com.azentio.docket.service.CaseService;
import com.azentio.docket.service.CaseStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {

    private final CaseRepository caseRepository;
    private final AlertRepository alertRepository;
    private final CaseEventRepository caseEventRepository;
    private final AppUserRepository appUserRepository;
    private final CaseStateMachine stateMachine;

    @Override
    @Transactional
    public CaseDetailResponse performAction(
            Long caseId,
            CaseActionRequest request,
            AppUser actor) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        CaseAction action = request.getAction();

        if (action == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Action is required");
        }

        // Validate role and ownership
        if (!stateMachine.isRoleAllowed(action, caseEntity, actor)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not allowed to perform this action");
        }

        // Validate transition
        CaseStatus fromStatus = caseEntity.getStatus();

        CaseStatus toStatus = stateMachine.getNextStatus(action, fromStatus);

        if (toStatus == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Illegal case status transition");
        }

        // Validate required note
        if (requiresNote(action)
                && (request.getNote() == null
                || request.getNote().trim().length() < 20)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Note must contain at least 20 characters");
        }

        // Handle assignment
        if (action == CaseAction.ASSIGN) {

            if (request.getAssignee() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Assignee is required");
            }

            AppUser assignee = appUserRepository.findById(request.getAssignee())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Assignee not found"));

            if (assignee.getRole() != com.azentio.docket.enums.UserRole.ANALYST) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Assignee must be an analyst");
            }

            caseEntity.setAssignee(assignee);
        }

        caseEntity.setStatus(toStatus);
        caseEntity.setUpdatedAt(Instant.now());

        if (toStatus == CaseStatus.CLOSED_FALSE_POSITIVE
                || toStatus == CaseStatus.CLOSED_CONFIRMED_FRAUD) {
            caseEntity.setAssignee(caseEntity.getAssignee());
        }

        caseRepository.saveAndFlush(caseEntity);

        // Write append-only audit event
        CaseEvent event = CaseEvent.builder()
                .caseEntity(caseEntity)
                .actor(actor.getUsername())
                .action(action.name())
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .note(request.getNote())
                .at(Instant.now())
                .build();

        caseEventRepository.save(event);

        return buildCaseDetail(caseEntity, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public CaseDetailResponse getCaseDetails(Long caseId, AppUser actor) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Case not found"));

        return buildCaseDetail(caseEntity, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseSummaryResponse> getCases(
            CaseStatus status,
            Priority priority,
            Long assigneeId,
            Pageable pageable) {

        return caseRepository.findCasesForQueue(
                status, priority, assigneeId, pageable
        ).map(this::toSummaryResponse);
    }

    private CaseDetailResponse buildCaseDetail(Case caseEntity, AppUser actor) {

        List<AlertResponse> alerts =
                alertRepository.findByCaseEntityIdOrderByRaisedAtAsc(caseEntity.getId())
                        .stream()
                        .map(alert -> AlertResponse.builder()
                                .id(alert.getId())
                                .alertId(alert.getAlertId())
                                .customerId(alert.getCustomerId())
                                .ruleCode(alert.getRuleCode())
                                .score(alert.getScore())
                                .amount(alert.getAmount())
                                .currency(alert.getCurrency())
                                .raisedAt(alert.getRaisedAt())
                                .build())
                        .toList();

        List<CaseEventResponse> timeline =
                caseEventRepository.findByCaseEntityIdOrderByAtAsc(caseEntity.getId())
                        .stream()
                        .map(event -> CaseEventResponse.builder()
                                .id(event.getId())
                                .actor(event.getActor())
                                .action(event.getAction())
                                .fromStatus(event.getFromStatus())
                                .toStatus(event.getToStatus())
                                .note(event.getNote())
                                .at(event.getAt())
                                .build())
                        .toList();

        return CaseDetailResponse.builder()
                .id(caseEntity.getId())
                .customerId(caseEntity.getCustomerId())
                .status(caseEntity.getStatus())
                .priority(caseEntity.getPriority())
                .assigneeId(caseEntity.getAssignee() == null
                        ? null : caseEntity.getAssignee().getId())
                .createdAt(caseEntity.getCreatedAt())
                .updatedAt(caseEntity.getUpdatedAt())
                .dueAt(caseEntity.getDueAt())
                .slaDurationSeconds(caseEntity.getSlaDurationSeconds())
                .alerts(alerts)
                .timeline(timeline)
                .allowedActions(stateMachine.getAllowedActions(caseEntity, actor))
                .build();
    }

    private CaseSummaryResponse toSummaryResponse(Case caseEntity) {
        return CaseSummaryResponse.builder()
                .id(caseEntity.getId())
                .customerId(caseEntity.getCustomerId())
                .status(caseEntity.getStatus())
                .priority(caseEntity.getPriority())
                .assigneeId(caseEntity.getAssignee() == null
                        ? null : caseEntity.getAssignee().getId())
                .createdAt(caseEntity.getCreatedAt())
                .dueAt(caseEntity.getDueAt())
                .slaDurationSeconds(caseEntity.getSlaDurationSeconds())
                .build();
    }

    private boolean requiresNote(CaseAction action) {
        return action == CaseAction.ESCALATE
                || action == CaseAction.CLOSE_FALSE_POSITIVE
                || action == CaseAction.CLOSE_FRAUD;
    }
}