package com.azentio.docket.requestdto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertRequest {

    @NotBlank(message = "Alert ID is required")
    private String alertId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Rule code is required")
    private String ruleCode;

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be between 0 and 100")
    @Max(value = 100, message = "Score must be between 0 and 100")
    private Integer score;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 17, fraction = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter uppercase code")
    private String currency;

    @NotNull(message = "Raised at is required")
    private Instant raisedAt;
}