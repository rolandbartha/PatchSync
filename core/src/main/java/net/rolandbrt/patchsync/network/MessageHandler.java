package net.rolandbrt.patchsync.network;

@FunctionalInterface
public interface MessageHandler {
    void onMessage(String message);
}