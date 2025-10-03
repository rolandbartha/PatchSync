package net.rolandbrt.patchsync.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rolandbrt.patchsync.configuration.AppConfig;
import net.rolandbrt.patchsync.configuration.ArtifactRepoConfig;
import net.rolandbrt.patchsync.configuration.RepositoryArtifactConfig;
import net.rolandbrt.patchsync.data.Artifact;
import net.rolandbrt.patchsync.network.UpdateMessage;
import net.rolandbrt.patchsync.util.JsonUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class RepositoryManager {
    private final AppConfig config;
    private static final File reposDir = new File("repos");

    public List<Artifact> checkAllRepos() {
        log.info("Checking all repos for updates...");
        List<Artifact> artifacts = new ArrayList<>();

        config.getRepositories().forEach((name, repoCfg) -> {
            File repoDir = new File(reposDir, name);
            try {
                updateRepo(name, repoCfg, repoDir);
                artifacts.addAll(loadArtifacts(repoDir, name, repoCfg));
            } catch (Exception e) {
                log.error("Failed to update repo {} -> {}", repoCfg.getRepo(), e.getMessage());
            }
        });
        return artifacts;
    }

    public List<Artifact> fetchArtifactsFromMessage(UpdateMessage message) throws Exception {
        File repoDir = new File(reposDir, message.getRepoName());
        ArtifactRepoConfig repoCfg = config.getRepositories().get(message.getRepoName());
        if (repoCfg == null) return Collections.emptyList();
        updateRepo(message.getRepoName(), repoCfg, repoDir);
        return loadArtifacts(repoDir, message.getRepoName(), repoCfg);
    }

    private void updateRepo(String name, ArtifactRepoConfig repoCfg, File repoDir) throws Exception {
        String repoUrl = repoCfg.getRepo();
        String branch = repoCfg.getBranch() != null ? repoCfg.getBranch() : "main";
        RepositoryCredentials credentials = repoCfg.getCredentials();

        if (!repoDir.exists()) {
            log.info("Cloning {}({}) (branch: {}) -> {}", name, repoUrl, branch, repoDir);
            CloneCommand clone = Git.cloneRepository()
                    .setURI("https://" + repoUrl + ".git")
                    .setDirectory(repoDir)
                    .setBranch(branch);
            if (credentials != null) {
                clone.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(
                                credentials.getUsername(),
                                credentials.getToken()
                        )
                );
            }
            clone.call();
        } else {
            try (Git git = Git.open(repoDir)) {
                log.info("Pulling latest for {}({}) (branch: {})", name, repoUrl, branch);
                PullCommand pull = git.pull().setRemoteBranchName(branch);
                if (credentials != null) {
                    pull.setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(
                                    credentials.getUsername(),
                                    credentials.getToken()
                            )
                    );
                }
                pull.call();
            }
        }
    }

    private List<Artifact> loadArtifacts(File repoDir, String repoName, ArtifactRepoConfig repoCfg) throws Exception {
        File cfgFile = new File(repoDir, "repo-config.json");
        if (!cfgFile.exists()) {
            log.warn("No repo-config.json in {}", repoDir);
            return Collections.emptyList();
        }
        RepositoryArtifactConfig cfg = JsonUtils.fromJson(cfgFile, RepositoryArtifactConfig.class);

        List<Artifact> artifacts = new ArrayList<>();
        for (RepositoryArtifactConfig.ArtifactDef def : cfg.getArtifacts()) {
            File file = new File(repoDir, def.getPath());
            artifacts.add(Artifact.builder()
                    .name(def.getName())
                    .file(file)
                    .repo(repoName)
                    .branch(repoCfg.getBranch() != null ? repoCfg.getBranch() : "main")
                    .build());
        }
        return artifacts;
    }
}