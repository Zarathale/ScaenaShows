package com.scaena.shows.runtime.executor;

import com.scaena.shows.model.event.ShowEvent;
import com.scaena.shows.model.event.UtilityEvents.*;
import com.scaena.shows.registry.CueRegistry;
import com.scaena.shows.runtime.RunningShow;
import org.bukkit.Bukkit;

import java.util.logging.Logger;

/**
 * Handles REST, COMMAND, and CUE (child-cue reference) events.
 * CUE references are inlined into the running show's event stream by ShowScheduler;
 * this executor handles any remaining CUE event invocations.
 */
public final class UtilityEventExecutor implements EventExecutor {

    private final Logger log;

    public UtilityEventExecutor(Logger log) {
        this.log = log;
    }

    @Override
    public void execute(ShowEvent event, RunningShow show) {
        switch (event.type()) {
            case REST    -> {} // no-op at execution time; timeline pacing is in ShowScheduler
            case COMMAND -> handleCommand((CommandEvent) event, show);
            case CUE     -> {} // CUE refs are inlined by ShowScheduler before dispatch
            case PAUSE   -> {} // OPS-029: no-op in production; Phase 2 step scheduler handles stop logic
            default -> {}
        }
    }

    private void handleCommand(CommandEvent e, RunningShow show) {
        String cmd = e.command.startsWith("/") ? e.command.substring(1) : e.command;
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
