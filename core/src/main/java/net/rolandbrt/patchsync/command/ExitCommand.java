package net.rolandbrt.patchsync.command;

import net.rolandbrt.patchsync.App;

public class ExitCommand implements Command {

    @Override
    public void execute(String[] args) {
        App.getInstance().setRunning(false);
    }
}