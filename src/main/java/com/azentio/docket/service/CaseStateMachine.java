package com.azentio.docket.service;

import com.azentio.docket.entity.AppUser;
import com.azentio.docket.entity.Case;
import com.azentio.docket.enums.CaseAction;
import com.azentio.docket.enums.CaseStatus;

import java.util.Set;

public interface CaseStateMachine {

    boolean isRoleAllowed(CaseAction action, Case caseEntity, AppUser user);

    CaseStatus getNextStatus(CaseAction action, CaseStatus currentStatus);

    Set<CaseAction> getAllowedActions(Case caseEntity, AppUser user);

}
