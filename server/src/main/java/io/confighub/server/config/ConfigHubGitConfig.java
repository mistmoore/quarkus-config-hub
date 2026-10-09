package io.confighub.server.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "config-hub.git")
public interface ConfigHubGitConfig {
    String repositoryUri();

    @WithDefault(".config-hub/repository")
    String worktree();

    @WithDefault("main")
    String branch();
}
