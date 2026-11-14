package com.azentio.docket.repository;

import com.azentio.docket.entity.Case;
import com.azentio.docket.enums.CaseStatus;
import com.azentio.docket.enums.Priority;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CaseRepository extends JpaRepository<Case, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM Case c
        WHERE c.customerId = :customerId
          AND c.status NOT IN (
              com.azentio.docket.enums.CaseStatus.CLOSED_FALSE_POSITIVE,
              com.azentio.docket.enums.CaseStatus.CLOSED_CONFIRMED_FRAUD
          )
    """)
    Optional<Case> findOpenCaseByCustomerId(
            @Param("customerId") String customerId
    );

    @Query("""
        SELECT c FROM Case c
        WHERE (:status IS NULL OR c.status = :status)
          AND (:priority IS NULL OR c.priority = :priority)
          AND (:assigneeId IS NULL OR c.assignee.id = :assigneeId)
        ORDER BY c.dueAt ASC
    """)
    Page<Case> findCasesForQueue(
            @Param("status") CaseStatus status,
            @Param("priority") Priority priority,
            @Param("assigneeId") Long assigneeId,
            Pageable pageable
    );
}