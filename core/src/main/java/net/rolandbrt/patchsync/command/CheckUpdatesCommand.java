package net.rolandbrt.patchsync.command;

import net.rolandbrt.patchsync.App;

public class CheckUpdatesCommand implements Command {

    @Override
    public void execute(String[] args) {
        App.getInstance().getSyncManager().checkAll();
    }
}