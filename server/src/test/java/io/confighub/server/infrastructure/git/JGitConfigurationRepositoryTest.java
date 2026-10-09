package io.confighub.server.infrastructure.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.confighub.server.config.ConfigHubGitConfig;
import io.confighub.server.domain.ResolvedConfiguration;

class JGitConfigurationRepositoryTest {

    @TempDir
    Path temp;

    @Test
    void resolvesConfigurationUsingExpectedPrecedence() throws Exception {
        Path remote = temp.resolve("remote");
        try (Git git = Git.init().setInitialBranch("main").setDirectory(remote.toFile()).call()) {
            write(remote, "global/application.properties", """
                    common.timeout=1s
                    common.region=global
                    """);
            write(remote, "global/pluto.properties", """
                    common.timeout=2s
                    global.only=true
                    """);
            write(remote, "orders-service/application.properties", """
                    common.timeout=3s
                    service.name=orders
                    """);
            write(remote, "orders-service/pluto.properties", """
                    common.timeout=5s
                    payment.url=https://payment.internal
                    """);

            git.add().addFilepattern(".").call();
            git.commit()
                    .setMessage("initial config")
                    .setAuthor("Config Hub Test", "test@example.com")
                    .call();
        }

        Path worktree = temp.resolve("worktree");
        ConfigHubGitConfig config = new TestGitConfig(remote.toUri().toString(), worktree.toString(), "main");
        JGitConfigurationRepository repository = new JGitConfigurationRepository(config);

        ResolvedConfiguration resolved = repository.resolve("orders-service", "pluto");

        assertEquals("orders-service", resolved.application());
        assertEquals("pluto", resolved.environment());
        assertEquals(40, resolved.version().length());
        assertEquals("5s", resolved.properties().get("common.timeout"));
        assertEquals("global", resolved.properties().get("common.region"));
        assertEquals("true", resolved.properties().get("global.only"));
        assertEquals("orders", resolved.properties().get("service.name"));
        assertEquals("https://payment.internal", resolved.properties().get("payment.url"));
        assertTrue(resolved.properties().size() >= 5);
    }

    private static void write(Path root, String relative, String content) throws Exception {
        Path file = root.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private record TestGitConfig(String repositoryUri, String worktree, String branch)
            implements ConfigHubGitConfig {
    }
}
