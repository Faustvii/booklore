package org.booklore.service.author;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local accumulator for author IDs created via find-or-create during book
 * ingestion/metadata operations. Reentrant: begin() only starts a session if none is
 * active on the current thread, so a batch caller (library scan, bulk metadata refresh)
 * can wrap many nested find-or-create calls and fire a single auto-fetch pass afterwards
 * instead of one per book.
 */
@Component
public class NewAuthorTrackingContext {

    private static final ThreadLocal<List<Long>> TRACKED_IDS = new ThreadLocal<>();

    public void track(Long authorId) {
        List<Long> ids = TRACKED_IDS.get();
        if (ids != null) {
            ids.add(authorId);
        }
    }

    public boolean begin() {
        if (TRACKED_IDS.get() != null) {
            return false;
        }
        TRACKED_IDS.set(new ArrayList<>());
        return true;
    }

    public List<Long> end(boolean owner) {
        if (!owner) {
            return List.of();
        }
        List<Long> ids = TRACKED_IDS.get();
        TRACKED_IDS.remove();
        return ids != null ? ids : List.of();
    }
}
