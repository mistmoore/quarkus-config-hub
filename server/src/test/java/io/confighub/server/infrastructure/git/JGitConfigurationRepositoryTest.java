package io.confighub.server.infrastructure.git;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(resolved.application()).isEqualTo("orders-service");
        assertThat(resolved.environment()).isEqualTo("pluto");
        assertThat(resolved.version()).hasSize(40);
        assertThat(resolved.properties())
                .containsEntry("common.timeout", "5s")
                .containsEntry("common.region", "global")
                .containsEntry("global.only", "true")
                .containsEntry("service.name", "orders")
                .containsEntry("payment.url", "https://payment.internal");
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
