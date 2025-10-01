package net.rolandbrt.patchsync.pluginexample;

import net.rolandbrt.patchsync.api.Event;
import net.rolandbrt.patchsync.api.SyncPlugin;
import net.rolandbrt.patchsync.event.DeployUpdateEvent;
import net.rolandbrt.patchsync.event.RollbackUpdateEvent;

public class Main extends SyncPlugin {
    @Override
    public String getName() {
        return "ExamplePlugin";
    }

    @Override
    public void onLoad() {
        log("called onLoad()");
    }

    @Override
    public void onUnload() {
        log("called onUnload()");
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof DeployUpdateEvent e) {
            log("called onEvent() for DeployEvent with " + e.getArtifacts().size() +
                    " artifacts and reason: " + e.getSnapshot().getReason());
        } else if (event instanceof RollbackUpdateEvent e) {
            log("called onEvent() for RollbackEvent id: " + e.getSnapshotId() + " with " +
                    e.getArtifacts().size() + " artifacts and reason: " + e.getReason());
        }
    }
}
