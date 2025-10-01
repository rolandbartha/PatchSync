package net.rolandbrt.patchsync.command;

import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.App;

@Slf4j
public class HelpCommand implements Command {

    @Override
    public void execute(String[] args) {
        log.info("Available commands: {}",
                App.getInstance().getRegistry().getCommands().keySet());
    }
}