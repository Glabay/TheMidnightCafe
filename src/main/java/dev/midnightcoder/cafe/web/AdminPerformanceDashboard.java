package dev.midnightcoder.cafe.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Controller for viewing and analyzing K6 load testing summary results.
 *
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping({"/admin/load-test", "/admin/k6", "/admin/performance"})
public class AdminPerformanceDashboard {

    private static final Logger log = LoggerFactory.getLogger(AdminPerformanceDashboard.class);

    private static final Path[] DEFAULT_PATHS = {
        Path.of("k6", "results", "result-summary.json"),
        Path.of("k6/results/result-summary.json"),
        Path.of("result-summary.json")
    };

    @GetMapping
    public String dashboard(Model model) {
        Optional<String> jsonSummary = loadDefaultSummary();
        if (jsonSummary.isPresent()) {
            model.addAttribute("summaryJson", jsonSummary.get());
            model.addAttribute("hasData", true);
        } else {
            model.addAttribute("summaryJson", "{}");
            model.addAttribute("hasData", false);
        }
        return "admin/load-test";
    }

    @GetMapping(value = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> getSummaryData() {
        Optional<String> jsonSummary = loadDefaultSummary();
        return jsonSummary.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok("{}"));
    }

    @PostMapping("/upload")
    public String uploadSummary(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("errorMessage", "Uploaded file is empty. Please select a valid K6 result-summary.json file.");
            return dashboard(model);
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            model.addAttribute("summaryJson", content);
            model.addAttribute("hasData", true);
            model.addAttribute("uploadSuccess", true);
        } catch (IOException e) {
            log.error("Failed to read uploaded summary file", e);
            model.addAttribute("errorMessage", "Failed to process uploaded file: " + e.getMessage());
            return dashboard(model);
        }

        return "admin/load-test";
    }

    private Optional<String> loadDefaultSummary() {
        for (Path path : DEFAULT_PATHS) {
            if (Files.exists(path) && Files.isRegularFile(path)) {
                try {
                    return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    log.warn("Failed reading default summary from {}: {}", path, e.getMessage());
                }
            }
        }
        return Optional.empty();
    }
}
