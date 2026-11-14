package com.azentio.docket.service;

import com.azentio.docket.requestdto.AlertRequest;
import com.azentio.docket.responsedto.CaseSummaryResponse;

public interface AlertService {

    CaseSummaryResponse ingestAlert(AlertRequest request, String actor);
}