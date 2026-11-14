package com.azentio.docket.requestdto;

import com.azentio.docket.enums.CaseAction;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseActionRequest {

    private CaseAction action;

    @Size(max = 1000, message = "Note cannot exceed 1000 characters")
    private String note;

    private Long assignee;
}