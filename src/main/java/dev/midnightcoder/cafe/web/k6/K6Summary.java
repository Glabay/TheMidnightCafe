package dev.midnightcoder.cafe.web.k6;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-10-27
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record K6Summary(
    String version,
    K6Metadata metadata,
    K6Config config,
    K6Results results
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record K6Metadata(
        String generatedAt,
        String k6Version
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record K6Config(
        String execution,
        String script,
        double duration
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record K6Results(
        List<K6Metric> metrics,
        K6Checks checks
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record K6Metric(
        String name,
        String type,
        String contains,
        Map<String, Number> values
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record K6Checks(
        List<K6Metric> metrics,
        List<K6CheckResult> results
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record K6CheckResult(
        String name,
        int passes,
        int fails
    ) {}
}
