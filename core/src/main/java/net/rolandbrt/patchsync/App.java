package net.rolandbrt.patchsync;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.configuration.AppConfig;
import net.rolandbrt.patchsync.plugin.PluginManager;
import net.rolandbrt.patchsync.command.*;
import net.rolandbrt.patchsync.configuration.ConfigLoader;
import net.rolandbrt.patchsync.core.SyncManager;

import java.util.Scanner;

@Slf4j
@Getter
public class App {
    public static void main(String[] args) {
        new App().init();
    }

    private static App INSTANCE;

    public static App getInstance() {
        synchronized (App.class) {
            return INSTANCE;
        }
    }

    private final CommandRegistry registry = new CommandRegistry();
    private final PluginManager pluginManager = new PluginManager();
    private final SyncManager syncManager = new SyncManager();

    @Setter
    private boolean running;

    public App() {
        INSTANCE = this;
    }

    public void init() {
        log.info("Starting PatchSync...");

        AppConfig config = ConfigLoader.loadOrInit();
        try {
            syncManager.init(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        registry.register("help", new HelpCommand());
        registry.register("check", new CheckUpdatesCommand());
        registry.register("rollback", new RollbackCommand());
        registry.register("exit", new ExitCommand());

        pluginManager.load();

        log.info("Sync started. Type 'help' for commands.");
        running = true;
        loop();
    }

    public void loop() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (!line.trim().isEmpty()) {
                registry.execute(line);
            }
        }
        stop();
    }

    public void stop() {
        pluginManager.unloadAll();
        registry.unregisterAll();
        syncManager.close();
    }
}