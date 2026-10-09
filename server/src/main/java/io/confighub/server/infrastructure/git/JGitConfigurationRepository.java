package io.confighub.server.infrastructure.git;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;

import io.confighub.server.config.ConfigHubGitConfig;
import io.confighub.server.domain.ConfigurationRepository;
import io.confighub.server.domain.ConfigurationRepositoryException;
import io.confighub.server.domain.ResolvedConfiguration;

@ApplicationScoped
public class JGitConfigurationRepository implements ConfigurationRepository {
    private final ConfigHubGitConfig config;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile Git git;

    public JGitConfigurationRepository(ConfigHubGitConfig config) {
        this.config = config;
    }

    @Override
    public ResolvedConfiguration resolve(String application, String environment) {
        lock.lock();
        try {
            Git repository = ensureRepository();
            synchronize(repository);

            Map<String, String> properties = new LinkedHashMap<>();
            Path worktree = repository.getRepository().getWorkTree().toPath();

            load(worktree.resolve("global/application.properties"), properties);
            load(worktree.resolve("global/" + environment + ".properties"), properties);
            load(worktree.resolve(application + "/application.properties"), properties);
            load(worktree.resolve(application + "/" + environment + ".properties"), properties);

            ObjectId head = repository.getRepository().resolve(Constants.HEAD);
            String version = head == null ? "UNKNOWN" : head.name();

            return new ResolvedConfiguration(
                    application,
                    environment,
                    version,
                    Map.copyOf(properties));
        } catch (IOException | GitAPIException e) {
            throw new ConfigurationRepositoryException("Unable to resolve configuration from Git", e);
        } finally {
            lock.unlock();
        }
    }

    private Git ensureRepository() throws GitAPIException, IOException {
        Git current = git;
        if (current != null) {
            return current;
        }

        Path worktree = Path.of(config.worktree()).toAbsolutePath().normalize();
        if (Files.exists(worktree.resolve(".git"))) {
            git = Git.open(worktree.toFile());
            return git;
        }

        Path parent = worktree.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        git = Git.cloneRepository()
                .setURI(config.repositoryUri())
                .setDirectory(worktree.toFile())
                .setBranch("refs/heads/" + config.branch())
                .call();

        return git;
    }

    private void synchronize(Git repository) throws GitAPIException {
        repository.fetch().setRemote("origin").call();
        repository.checkout().setName(config.branch()).call();
        repository.reset()
                .setMode(ResetType.HARD)
                .setRef("origin/" + config.branch())
                .call();
    }

    private static void load(Path file, Map<String, String> target) throws IOException {
        if (!Files.isRegularFile(file)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        }

        properties.stringPropertyNames().stream()
                .sorted()
                .forEach(name -> target.put(name, properties.getProperty(name)));
    }
}
