package com.azentio.docket.controller;

import com.azentio.docket.entity.AppUser;
import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.Priority;
import com.azentio.docket.repository.AppUserRepository;
import com.azentio.docket.requestdto.CaseActionRequest;
import com.azentio.docket.responsedto.CaseDetailResponse;
import com.azentio.docket.responsedto.CaseSummaryResponse;
import com.azentio.docket.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final AppUserRepository appUserRepository;

    @GetMapping("/{id}")
    public CaseDetailResponse getCase(
            @PathVariable Long id,
            Authentication authentication) {

        AppUser actor = getCurrentUser(authentication);

        return caseService.getCaseDetails(id, actor);
    }

    @GetMapping
    public Page<CaseSummaryResponse> getCases(
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long assignee,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid page or size");
        }

        return caseService.getCases(
                status,
                priority,
                assignee,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "dueAt"))
        );
    }

    @PostMapping("/{id}/actions")
    @ResponseStatus(HttpStatus.OK)
    public CaseDetailResponse   performAction(
            @PathVariable Long id,
            @Valid @RequestBody CaseActionRequest request,
            Authentication authentication) {

        AppUser actor = getCurrentUser(authentication);

        return caseService.performAction(id, request, actor);
    }

    private AppUser getCurrentUser(Authentication authentication) {

        return appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found"));
    }
}