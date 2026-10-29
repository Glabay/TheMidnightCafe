package dev.midnightcoder.cafe.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPerformanceDashboardTest {

    private AdminPerformanceDashboard dashboard;

    @BeforeEach
    void setUp() {
        dashboard = new AdminPerformanceDashboard();
    }

    @Test
    void dashboard_returnsLoadTestViewWithModelAttributes() {
        Model model = new ConcurrentModel();
        String view = dashboard.dashboard(model);

        assertEquals("admin/load-test", view);
        assertNotNull(model.getAttribute("summaryJson"));
        assertNotNull(model.getAttribute("hasData"));
    }

    @Test
    void getSummaryData_returnsJsonResponse() {
        ResponseEntity<String> response = dashboard.getSummaryData();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void uploadSummary_withValidFile_updatesModelAndReturnsView() {
        String testJson = "{\"root_group\":{\"checks\":{}},\"metrics\":{}}";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "result-summary.json",
            "application/json",
            testJson.getBytes(StandardCharsets.UTF_8)
        );

        Model model = new ConcurrentModel();
        String view = dashboard.uploadSummary(file, model);

        assertEquals("admin/load-test", view);
        assertEquals(testJson, model.getAttribute("summaryJson"));
        assertEquals(true, model.getAttribute("hasData"));
        assertEquals(true, model.getAttribute("uploadSuccess"));
    }

    @Test
    void uploadSummary_withEmptyFile_returnsErrorMessage() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.json",
            "application/json",
            new byte[0]
        );

        Model model = new ConcurrentModel();
        String view = dashboard.uploadSummary(emptyFile, model);

        assertEquals("admin/load-test", view);
        assertNotNull(model.getAttribute("errorMessage"));
    }
}
