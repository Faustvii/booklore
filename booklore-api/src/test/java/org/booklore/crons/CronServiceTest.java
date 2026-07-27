package org.booklore.crons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CronServiceTest {

    @Test
    void initScheduledTasks_doesNotThrow() {
        CronService cronService = new CronService();
        assertDoesNotThrow(cronService::initScheduledTasks);
    }
}
