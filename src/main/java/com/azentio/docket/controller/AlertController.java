package com.azentio.docket.controller;

import com.azentio.docket.requestdto.AlertRequest;
import com.azentio.docket.responsedto.CaseSummaryResponse;
import com.azentio.docket.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CaseSummaryResponse ingestAlert(
            @Valid @RequestBody AlertRequest request,
            Authentication authentication) {

        return alertService.ingestAlert(
                request,
                authentication.getName()
        );
    }
}