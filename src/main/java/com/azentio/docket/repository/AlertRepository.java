package com.azentio.docket.repository;

import com.azentio.docket.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findByAlertId(String alertId);

    List<Alert> findByCaseEntityIdOrderByRaisedAtAsc(Long caseId);
}