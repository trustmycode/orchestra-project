package com.orchestra.executor.service;

import com.orchestra.domain.repository.TestRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class StuckRunReaperService {

    private final TestRunRepository testRunRepository;

    @Scheduled(fixedDelay = 60000) // Run every minute
    @Transactional
    public void reapStuckRuns() {
        OffsetDateTime threshold = OffsetDateTime.now().minusSeconds(120); // 2 minutes without heartbeat
        int count = testRunRepository.failStuckRuns(threshold, OffsetDateTime.now());
        if (count > 0) {
            log.info("Reaper: Found and failed {} stuck test runs.", count);
        }
    }
}
