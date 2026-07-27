package org.booklore.crons;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Service;

@Service
@DependsOnDatabaseInitialization
@Slf4j
public class CronService {

    @PostConstruct
    public void initScheduledTasks() {
        // Disabled in this fork.
    }
}
