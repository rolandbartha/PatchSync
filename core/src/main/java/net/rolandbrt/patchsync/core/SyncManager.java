package net.rolandbrt.patchsync.core;

import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.App;
import net.rolandbrt.patchsync.AppConfig;
import net.rolandbrt.patchsync.deploy.DeploymentManager;
import net.rolandbrt.patchsync.event.DeployUpdateEvent;
import net.rolandbrt.patchsync.event.RollbackUpdateEvent;
import net.rolandbrt.patchsync.network.UpdateMessage;
import net.rolandbrt.patchsync.data.Artifact;
import net.rolandbrt.patchsync.repository.RepositoryManager;
import net.rolandbrt.patchsync.data.Snapshot;
import net.rolandbrt.patchsync.snapshot.SnapshotManager;
import net.rolandbrt.patchsync.util.JsonUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SyncManager {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Getter
    private RepositoryManager repositoryManager;
    @Getter
    private SnapshotManager snapshotManager;
    @Getter
    private DeploymentManager deploymentManager;

    private String token;
    private HttpServer server;

    public void init(AppConfig config) throws Exception {
        repositoryManager = new RepositoryManager(config);
        snapshotManager = new SnapshotManager(config.getSnapshot());
        deploymentManager = new DeploymentManager(config.getTargets());
        server = HttpServer.create(new InetSocketAddress(config.getGithubConfig().getPort()), 0);
        token = config.getGithubConfig().getToken();
        server.createContext("/" + config.getGithubConfig().getEndpoint(), exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String authToken = exchange.getRequestHeaders().getFirst("X-Auth-Token");
                if (!token.equals(authToken)) {
                    exchange.sendResponseHeaders(403, 0);
                    exchange.close();
                    return;
                }
                String payload = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                executor.submit(() -> {
                    log.info("Received GitHub update notification {}", payload);
                    try {
                        UpdateMessage message = JsonUtils.fromJson(payload, UpdateMessage.class);
                        List<Artifact> artifacts = repositoryManager.fetchArtifactsFromMessage(message);
                        log.info("Found {} artifacts for repo {}", artifacts.size(), message.getRepoName());
                        deployUpdate(artifacts, "Auto-update from GitHub");
                    } catch (Exception e) {
                        log.error("Failed to process update notification", e);
                    }
                });
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        });
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        log.info("Endpoint listening on port 8080");
    }

    public void close() {
        executor.shutdown();
        if (server != null) {
            log.info("Stopping endpoint...");
            server.stop(1);
        }
        if (deploymentManager != null) {
            deploymentManager.close();
        }
    }

    public void checkAll() {
        List<Artifact> artifacts = repositoryManager.checkAllRepos();
        if (!artifacts.isEmpty()) {
            deployUpdate(artifacts, "Manual check");
        } else {
            log.info("No updates found.");
        }
    }

    public void deployUpdate(List<Artifact> artifacts, String reason) {
        if (artifacts.isEmpty()) {
            log.warn("No artifacts to deploy");
            return;
        }
        Snapshot snapshot = snapshotManager.createSnapshot(artifacts, reason);

        DeployUpdateEvent event = new DeployUpdateEvent(List.copyOf(artifacts), snapshot);
        App.getInstance().getPluginManager().fireEvent(event);

        deploymentManager.deploy(artifacts);

        log.info("Artifacts updated and deployed: {}", artifacts.stream()
                .map(Artifact::getName).toList());
    }

    public void rollback(String snapshotId, String reason) {
        List<Artifact> restored = snapshotManager.restore(snapshotId);
        if (restored.isEmpty()) {
            log.warn("Nothing to rollback in snapshot {}", snapshotId);
            return;
        }
        RollbackUpdateEvent event = new RollbackUpdateEvent(List.copyOf(restored), snapshotId, reason);
        App.getInstance().getPluginManager().fireEvent(event);

        deploymentManager.deploy(restored);

        log.info("Rollback completed for snapshot {}", snapshotId);
    }
}