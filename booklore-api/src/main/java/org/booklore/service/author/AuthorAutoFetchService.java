package org.booklore.service.author;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booklore.service.AuthorMetadataService;
import org.booklore.service.appsettings.AppSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorAutoFetchService {

    private final AppSettingService appSettingService;
    private final AuthorMetadataService authorMetadataService;

    public void triggerIfEnabled(List<Long> newAuthorIds) {
        if (newAuthorIds == null || newAuthorIds.isEmpty()) {
            return;
        }
        if (!appSettingService.getAppSettings().isAutoFetchAuthorMetadata()) {
            return;
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // Defer until the surrounding transaction commits, so the async matcher
            // (running on a different thread/pool) is guaranteed to see the new author rows.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fetch(newAuthorIds);
                }
            });
        } else {
            fetch(newAuthorIds);
        }
    }

    private void fetch(List<Long> authorIds) {
        log.info("Auto-fetching metadata for {} newly created author(s)", authorIds.size());
        authorMetadataService.autoMatchAuthors(authorIds).subscribe(
                summary -> log.debug("Auto-fetched metadata for author '{}'", summary.getName()),
                error -> log.warn("Auto-fetch author metadata stream failed: {}", error.getMessage())
        );
    }
}
