package de.jwillert.quarkus.axonframework.extension.runtime.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import org.axonframework.config.TagsConfiguration;

import java.util.Map;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "axon")
public interface TagsConfigurationProperties {

    /**
     * Tags represented by key-value pairs.
     */
    Map<String, String> tags();

    default TagsConfiguration toTagsConfiguration() {
        return new TagsConfiguration(tags());
    }
}
