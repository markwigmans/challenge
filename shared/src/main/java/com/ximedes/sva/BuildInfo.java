package com.ximedes.sva;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Show the GIT build information
 */
@Slf4j
@PropertySource(value = "classpath:build.properties", ignoreResourceNotFound = true)
@Configuration
public class BuildInfo {
    @Getter
    @Value("${version:}")
    private String version;

    @Getter
    @Value("${revision:}")
    private String revision;

    @Getter
    @Value("${name:}")
    private String name;

    @Value("${timestamp:}")
    private String timestamp;

    @PostConstruct
    void init() {
        log.info("App: '{}', version: '{}', git: '{}', timestamp: '{}'", name, version, revision, getTimestamp());
    }

    String getTimestamp() {
        if (NumberUtils.isNumber(timestamp)) {
            return new Date(NumberUtils.createLong(timestamp)).toString();
        } else {
            return timestamp;
        }
    }
}
