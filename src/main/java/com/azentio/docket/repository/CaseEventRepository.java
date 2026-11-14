package com.azentio.docket.repository;

import com.azentio.docket.entity.CaseEvent;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface CaseEventRepository extends Repository<CaseEvent, Long> {

    CaseEvent save(CaseEvent event);

    List<CaseEvent> findByCaseEntityIdOrderByAtAsc(Long caseId);
}