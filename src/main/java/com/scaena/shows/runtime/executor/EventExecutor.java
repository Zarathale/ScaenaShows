package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.runtime.RunningShow;

/**
 * Executes one type of show event against a running show.
 * Each implementation handles a specific EventType.
 */
public interface EventExecutor {
    void execute(ShowEvent event, RunningShow show);
}
