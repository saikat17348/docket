package com.azentio.docket.service.impl;

import com.azentio.docket.entity.AppUser;
import com.azentio.docket.entity.Case;
import com.azentio.docket.enums.CaseAction;
import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.UserRole;
import com.azentio.docket.service.CaseStateMachine;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class CaseStateMachineImpl implements CaseStateMachine {

    private static final Map<CaseAction, Set<CaseStatus>> TRANSITIONS =
            new EnumMap<>(CaseAction.class);

    private static final Map<CaseAction, CaseStatus> NEXT_STATUSES =
            new EnumMap<>(CaseAction.class);

    private static final Set<CaseAction> ANALYST_ACTIONS =
            EnumSet.of(
                    CaseAction.START_REVIEW,
                    CaseAction.REQUEST_INFO,
                    CaseAction.RESUME,
                    CaseAction.ESCALATE,
                    CaseAction.CLOSE_FALSE_POSITIVE
            );

    static {
        TRANSITIONS.put(CaseAction.ASSIGN,
                EnumSet.of(CaseStatus.NEW, CaseStatus.ASSIGNED, CaseStatus.ESCALATED));

        TRANSITIONS.put(CaseAction.START_REVIEW,
                EnumSet.of(CaseStatus.ASSIGNED));

        TRANSITIONS.put(CaseAction.REQUEST_INFO,
                EnumSet.of(CaseStatus.IN_REVIEW));

        TRANSITIONS.put(CaseAction.RESUME,
                EnumSet.of(CaseStatus.PENDING_INFO));

        TRANSITIONS.put(CaseAction.ESCALATE,
                EnumSet.of(CaseStatus.IN_REVIEW));

        TRANSITIONS.put(CaseAction.CLOSE_FALSE_POSITIVE,
                EnumSet.of(CaseStatus.IN_REVIEW, CaseStatus.ESCALATED));

        TRANSITIONS.put(CaseAction.CLOSE_FRAUD,
                EnumSet.of(CaseStatus.IN_REVIEW, CaseStatus.ESCALATED));

        NEXT_STATUSES.put(CaseAction.ASSIGN, CaseStatus.ASSIGNED);
        NEXT_STATUSES.put(CaseAction.START_REVIEW, CaseStatus.IN_REVIEW);
        NEXT_STATUSES.put(CaseAction.REQUEST_INFO, CaseStatus.PENDING_INFO);
        NEXT_STATUSES.put(CaseAction.RESUME, CaseStatus.IN_REVIEW);
        NEXT_STATUSES.put(CaseAction.ESCALATE, CaseStatus.ESCALATED);
        NEXT_STATUSES.put(CaseAction.CLOSE_FALSE_POSITIVE,
                CaseStatus.CLOSED_FALSE_POSITIVE);
        NEXT_STATUSES.put(CaseAction.CLOSE_FRAUD,
                CaseStatus.CLOSED_CONFIRMED_FRAUD);
    }

    @Override
    public boolean isRoleAllowed(CaseAction action, Case caseEntity, AppUser user) {

        if (user.getRole() == UserRole.SUPERVISOR) {
            return true;
        }

        if (user.getRole() != UserRole.ANALYST) {
            return false;
        }

        return ANALYST_ACTIONS.contains(action)
                && caseEntity.getAssignee() != null
                && caseEntity.getAssignee().getId().equals(user.getId());
    }

    @Override
    public CaseStatus getNextStatus(CaseAction action, CaseStatus currentStatus) {

        Set<CaseStatus> validStatuses = TRANSITIONS.get(action);

        if (validStatuses == null || !validStatuses.contains(currentStatus)) {
            return null;
        }

        return NEXT_STATUSES.get(action);
    }

    @Override
    public Set<CaseAction> getAllowedActions(Case caseEntity, AppUser user) {

        Set<CaseAction> allowedActions = EnumSet.noneOf(CaseAction.class);

        for (CaseAction action : CaseAction.values()) {

            if (isRoleAllowed(action, caseEntity, user)
                    && getNextStatus(action, caseEntity.getStatus()) != null) {

                allowedActions.add(action);
            }
        }

        return allowedActions;
    }
}