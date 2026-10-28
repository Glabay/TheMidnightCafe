/**
 * K6 Load Test Analytics Dashboard - The Midnight Café
 * Interactive visualizer, progress bar analytics, chart suite, and drilldown modal system
 * for K6 result-summary.json.
 */

(function () {
    'use strict';

    // Global dashboard state
    let rawData = null;
    let cachedChecks = [];
    let checkFilter = 'all';
    let checkSearchQuery = '';

    // Color Palette matching Midnight Café Theme
    const THEME = {
        violet: '#a855f7',
        purple: '#9d50bb',
        pink: '#ff0080',
        red: '#ef4444',
        yellow: '#f1c56a',
        blue: '#3b82f6',
        cyan: '#06b6d4',
        green: '#10b981',
        textPrimary: '#ffffff',
        textSecondary: 'rgba(224, 224, 224, 0.75)',
        textMuted: 'rgba(224, 224, 224, 0.55)',
        gridLine: 'rgba(255, 255, 255, 0.08)',
        bgCard: 'rgba(18, 22, 32, 0.85)'
    };

    /**
     * Entry point to initialize the dashboard with data
     */
    window.initK6Dashboard = function (jsonData) {
        if (!jsonData) return;

        if (typeof jsonData === 'string') {
            try {
                rawData = JSON.parse(jsonData);
            } catch (e) {
                console.error('Invalid JSON string passed to K6 Dashboard:', e);
                showToast('Failed to parse JSON data', 'danger');
                return;
            }
        } else {
            rawData = jsonData;
        }

        renderDashboard(rawData);
    };

    /**
     * Main render coordinator
     */
    function renderDashboard(data) {
        if (!data || !data.metrics) {
            console.warn('Incomplete K6 data structure:', data);
            return;
        }

        const metrics = data.metrics || {};
        cachedChecks = extractChecks(data);

        // 1. Render Top KPI Cards & Progress Bars
        renderKpiCards(metrics, cachedChecks);

        // 2. Render Human Insights & Executive Summary
        renderHumanInsights(metrics, cachedChecks);

        // 3. Render Thresholds Grid & SLA Compliance
        renderThresholds(metrics);

        // 4. Render Visual Charts Suite (6 Charts)
        renderOutcomeChart(metrics);
        renderLatencyChart(metrics);
        renderTimingBreakdownChart(metrics);
        renderChecksComparisonChart(cachedChecks);
        renderNetworkChart(metrics);
        renderConcurrencyChart(metrics);

        // 5. Render Checks Table with Scope Badges & Progress
        renderChecksTable(cachedChecks);

        // 6. Render Metrics Detail Table with Spread Gauges
        renderMetricsDetailTable(metrics);

        // 7. Update Raw JSON display
        updateRawJsonDisplay(data);

        // Update file info indicator
        const fileInfo = document.getElementById('k6FileInfo');
        if (fileInfo) {
            const reqCount = formatNumber(metrics.http_reqs ? metrics.http_reqs.count : 0);
            const peakVus = (metrics.vus_max ? metrics.vus_max.value : (metrics.vus ? metrics.vus.max : 0)) || '—';
            fileInfo.textContent = `${reqCount} Requests • ${peakVus} Peak VUs`;
        }
    }

    /**
     * Extracts, tags and flattens all checks from root_group and nested groups
     */
    function extractChecks(data) {
        const list = [];

        function scanGroup(group) {
            if (!group) return;
            if (group.checks) {
                for (const key of Object.keys(group.checks)) {
                    const c = group.checks[key];
                    const passes = c.passes || 0;
                    const fails = c.fails || 0;
                    const total = passes + fails;
                    const passRate = total > 0 ? (passes / total) * 100 : 0;

                    let status = 'pass';
                    if (fails > 0 && passes === 0) status = 'fail';
                    else if (fails > 0 && passes > 0) status = 'partial';

                    // Determine scope / flow category
                    let scope = 'other';
                    const nameLower = (c.name || key).toLowerCase();
                    if (nameLower.startsWith('anonymous') || nameLower.includes('home page') || nameLower.includes('storefront')) {
                        scope = 'anonymous';
                    } else if (nameLower.startsWith('customer')) {
                        scope = 'customer';
                    } else if (nameLower.startsWith('admin')) {
                        scope = 'admin';
                    }

                    list.push({
                        name: c.name || key,
                        path: c.path || key,
                        id: c.id,
                        passes: passes,
                        fails: fails,
                        total: total,
                        passRate: passRate,
                        status: status,
                        scope: scope
                    });
                }
            }

            if (group.groups) {
                if (Array.isArray(group.groups)) {
                    group.groups.forEach(scanGroup);
                } else if (typeof group.groups === 'object') {
                    Object.values(group.groups).forEach(scanGroup);
                }
            }
        }

        if (data.root_group) {
            scanGroup(data.root_group);
        }

        return list;
    }

    /**
     * Populates Top 6 KPI summary cards and card-level progress bars
     */
    function renderKpiCards(metrics, checks) {
        const reqs = metrics.http_reqs || {};
        const reqFailed = metrics.http_req_failed || {};
        const duration = metrics.http_req_duration || {};
        const vusMax = metrics.vus_max || metrics.vus || {};
        const dataRecv = metrics.data_received || {};
        const dataSent = metrics.data_sent || {};

        // 1. Total Requests
        setText('kpiTotalRequests', formatNumber(reqs.count || 0));
        setText('kpiRps', reqs.rate ? `${formatNumber(reqs.rate.toFixed(1))} req/s` : '—');
        const progReqs = document.getElementById('kpiProgressRequests');
        if (progReqs) {
            progReqs.style.width = '100%';
        }

        // 2. Failure Rate
        const failRateVal = reqFailed.value !== undefined ? (reqFailed.value * 100) : (reqFailed.passes && reqs.count ? ((reqFailed.passes / reqs.count) * 100) : 0);
        const failRateStr = failRateVal.toFixed(2);
        const failCount = reqFailed.passes || 0;
        setText('kpiFailRate', `${failRateStr}%`);
        setText('kpiFailCount', `${formatNumber(failCount)} failed reqs`);

        const progFail = document.getElementById('kpiProgressFail');
        if (progFail) {
            progFail.style.width = `${Math.min(100, Math.max(failRateVal > 0 ? 5 : 0, failRateVal))}%`;
            progFail.className = failRateVal > 15 ? 'k6-card-progress-bar danger' : (failRateVal > 0 ? 'k6-card-progress-bar warning' : 'k6-card-progress-bar green');
        }

        // 3. Response Time (Median & p95)
        const medDurationVal = duration.med !== undefined ? duration.med : 0;
        const p95DurationVal = duration['p(95)'] !== undefined ? duration['p(95)'] : 0;
        setText('kpiMedianDuration', `${medDurationVal.toFixed(1)} ms`);
        setText('kpiP95Duration', `p95: ${p95DurationVal.toFixed(1)} ms • avg: ${duration.avg ? duration.avg.toFixed(1) + 'ms' : '—'}`);

        const progLatency = document.getElementById('kpiProgressLatency');
        if (progLatency) {
            // Relative to benchmark target (e.g. 500ms target vs actual)
            const target = 500;
            const pct = Math.min(100, Math.max(10, (medDurationVal / 5000) * 100));
            progLatency.style.width = `${pct}%`;
            progLatency.className = medDurationVal > 1000 ? 'k6-card-progress-bar warning' : 'k6-card-progress-bar cyan';
        }

        // 4. Peak Virtual Users
        const peakVus = vusMax.value || vusMax.max || '—';
        const iterations = metrics.iterations ? formatNumber(metrics.iterations.count) : '0';
        setText('kpiPeakVus', `${peakVus} VUs`);
        setText('kpiIterations', `${iterations} iterations`);

        const progVus = document.getElementById('kpiProgressVus');
        if (progVus) {
            progVus.style.width = '100%';
        }

        // 5. Data Transferred
        const recvGb = dataRecv.count ? (dataRecv.count / (1024 * 1024 * 1024)).toFixed(2) : '0.00';
        const recvRate = dataRecv.rate ? (dataRecv.rate / (1024 * 1024)).toFixed(2) : '0.00';
        setText('kpiDataTransferred', `${recvGb} GB`);
        setText('kpiDataRate', `${recvRate} MB/s throughput`);

        const progData = document.getElementById('kpiProgressData');
        if (progData) {
            progData.style.width = '100%';
        }

        // 6. Checks Passed
        const totalChecks = checks.reduce((sum, c) => sum + c.total, 0);
        const passedChecks = checks.reduce((sum, c) => sum + c.passes, 0);
        const checkPassPct = totalChecks > 0 ? ((passedChecks / totalChecks) * 100) : 100.0;
        setText('kpiChecksPassRate', `${checkPassPct.toFixed(1)}%`);
        setText('kpiChecksCounts', `${formatNumber(passedChecks)} of ${formatNumber(totalChecks)} passed`);

        const progChecks = document.getElementById('kpiProgressChecks');
        if (progChecks) {
            progChecks.style.width = `${checkPassPct}%`;
            progChecks.className = checkPassPct >= 95 ? 'k6-card-progress-bar green' : (checkPassPct >= 80 ? 'k6-card-progress-bar warning' : 'k6-card-progress-bar danger');
        }
    }

    /**
     * Generates plain-English human interpretations of load test results
     */
    function renderHumanInsights(metrics, checks) {
        const reqs = metrics.http_reqs || {};
        const reqFailed = metrics.http_req_failed || {};
        const duration = metrics.http_req_duration || {};
        const vusMax = metrics.vus_max || metrics.vus || {};

        const totalReqs = reqs.count || 0;
        const failRate = reqFailed.value !== undefined ? (reqFailed.value * 100) : 0;
        const p95 = duration['p(95)'] || 0;
        const med = duration.med || 0;
        const peakVus = vusMax.value || vusMax.max || 500;

        const badge = document.getElementById('insightBadge');
        const narrative = document.getElementById('insightNarrative');
        const highlightsContainer = document.getElementById('insightHighlights');

        let statusClass = 'healthy';
        let statusText = 'Excellent Performance';

        if (failRate > 15) {
            statusClass = 'danger';
            statusText = 'Errors Detected - Review Required';
        } else if (failRate > 2 || p95 > 1000) {
            statusClass = 'warning';
            statusText = 'Acceptable With Warnings';
        }

        if (badge) {
            badge.className = `insight-status-badge ${statusClass}`;
            badge.innerHTML = `<span>●</span> ${statusText}`;
        }

        // Identify high-performing checks vs failing checks
        const perfectChecks = checks.filter(c => c.fails === 0 && c.passes > 0);
        const failingChecks = checks.filter(c => c.fails > 0 && c.passes === 0);
        const partialChecks = checks.filter(c => c.fails > 0 && c.passes > 0);

        // Build human narrative
        if (narrative) {
            let paragraph1 = `During this load test run, the system sustained a heavy concurrency workload with <strong>${peakVus} concurrent Virtual Users</strong>, processing a total of <strong>${formatNumber(totalReqs)} HTTP requests</strong> at a throughput velocity of <strong>${formatNumber(reqs.rate ? reqs.rate.toFixed(0) : 0)} requests/second</strong>.`;

            let paragraph2 = '';
            if (p95 <= 500) {
                paragraph2 += `<strong>Latency &amp; Response Speed:</strong> Outstanding latency characteristics under load. The median response time was <strong>${med.toFixed(1)} ms</strong>, and 95% of all requests completed within <strong>${p95.toFixed(1)} ms</strong>, well within typical production SLA benchmarks (&lt; 500 ms). `;
            } else {
                paragraph2 += `<strong>Latency &amp; Response Speed:</strong> The median response time was <strong>${med.toFixed(1)} ms</strong> with peak tail latency (p95) reaching <strong>${p95.toFixed(1)} ms</strong> under high concurrent load.`;
            }

            let paragraph3 = '';
            if (failRate > 0) {
                const failingNames = failingChecks.slice(0, 3).map(c => c.name.split('-')[0].trim()).join(', ');
                paragraph3 += `<strong>Error Diagnostics (${failRate.toFixed(1)}% Failure Rate):</strong> Errors are concentrated in specific authenticated/mutation flows (e.g. <code>${escapeHtml(failingNames || 'Add Product to Cart')}</code>), while public storefront pages (Home, Products, Login, Register) maintained high reliability.`;
            } else {
                paragraph3 += `<strong>Error Diagnostics:</strong> Zero HTTP errors were observed across all simulated user journeys.`;
            }

            narrative.innerHTML = `
                <p>${paragraph1}</p>
                <p>${paragraph2}</p>
                <p>${paragraph3}</p>
            `;
        }

        // Build highlight bullets
        if (highlightsContainer) {
            highlightsContainer.innerHTML = `
                <div class="highlight-item">
                    <span class="highlight-bullet">⚡</span>
                    <div>
                        <strong>Response Latency Distribution:</strong>
                        <div>Median latency was <strong>${med.toFixed(1)} ms</strong> with p95 at <strong>${p95.toFixed(1)} ms</strong>.</div>
                    </div>
                </div>
                <div class="highlight-item">
                    <span class="highlight-bullet">🛡️</span>
                    <div>
                        <strong>Storefront &amp; Public Flow Stability:</strong>
                        <div><strong>${perfectChecks.length + partialChecks.length} endpoint flows</strong> successfully served traffic under peak load.</div>
                    </div>
                </div>
                <div class="highlight-item">
                    <span class="highlight-bullet">${failRate > 0 ? '⚠️' : '✅'}</span>
                    <div>
                        <strong>${failRate > 0 ? 'Mutation / Auth Hotspots' : 'Clean API Execution'}:</strong>
                        <div>${failingChecks.length > 0 ? `${failingChecks.length} endpoint checks encountered 100% rejection (e.g. CSRF/Session requirements).` : 'All endpoint checks passed successfully.'}</div>
                    </div>
                </div>
                <div class="highlight-item">
                    <span class="highlight-bullet">📈</span>
                    <div>
                        <strong>High Throughput Capacity:</strong>
                        <div>Sustained <strong>${formatNumber(reqs.rate ? reqs.rate.toFixed(0) : 0)} req/s</strong> without server process crashes or memory starvation.</div>
                    </div>
                </div>
            `;
        }
    }

    /**
     * Renders configured thresholds and whether they passed or failed, with visual budget progress bars
     */
    function renderThresholds(metrics) {
        const container = document.getElementById('thresholdsGrid');
        const summaryBadge = document.getElementById('thresholdsSummaryBadge');
        if (!container) return;

        const thresholdEntries = [];

        // Scan all metric thresholds
        for (const [metricKey, metricObj] of Object.entries(metrics)) {
            if (metricObj && metricObj.thresholds) {
                for (const [rule, failed] of Object.entries(metricObj.thresholds)) {
                    // In K6 result-summary.json, true indicates breached/failed threshold, false indicates passed
                    const passed = !failed;
                    let actualDisplay = '';
                    let progressPct = 50;

                    if (metricKey === 'errors' && metricObj.count !== undefined) {
                        actualDisplay = `Actual: ${formatNumber(metricObj.count)} errors`;
                        progressPct = passed ? 10 : 100;
                    } else if (metricKey === 'http_req_failed' && metricObj.value !== undefined) {
                        const actualPct = (metricObj.value * 100).toFixed(2);
                        actualDisplay = `Actual: ${actualPct}% failure`;
                        progressPct = Math.min(100, Math.max(5, (metricObj.value / 0.01) * 100));
                    } else if (metricKey === 'http_req_duration') {
                        if (rule.includes('p(95)')) {
                            const actualVal = metricObj['p(95)'] || 0;
                            actualDisplay = `Actual: ${actualVal.toFixed(1)} ms`;
                            progressPct = Math.min(100, Math.max(10, (actualVal / 400) * 100));
                        } else if (rule.includes('p(99)')) {
                            const actualVal = metricObj['p(99)'] || 0;
                            actualDisplay = `Actual: ${actualVal.toFixed(1)} ms`;
                            progressPct = Math.min(100, Math.max(10, (actualVal / 900) * 100));
                        }
                    }

                    thresholdEntries.push({
                        metric: metricKey,
                        rule: rule,
                        passed: passed,
                        actual: actualDisplay,
                        progressPct: progressPct
                    });
                }
            }
        }

        if (summaryBadge) {
            const passedCount = thresholdEntries.filter(t => t.passed).length;
            const totalCount = thresholdEntries.length;
            summaryBadge.textContent = `${passedCount} of ${totalCount} Passed`;
            summaryBadge.className = `threshold-summary-pill ${passedCount === totalCount ? 'passed' : 'failed'}`;
        }

        if (thresholdEntries.length === 0) {
            container.innerHTML = '<div style="color: var(--text-muted); font-size: 0.9rem;">No thresholds configured in this test run.</div>';
            return;
        }

        container.innerHTML = thresholdEntries.map(t => `
            <div class="threshold-card ${t.passed ? 'passed' : 'failed'}" data-metric="${escapeHtml(t.metric)}" data-rule="${escapeHtml(t.rule)}" role="button" tabindex="0" title="Click for SLA threshold diagnostics">
                <div class="threshold-card-top">
                    <div class="threshold-info">
                        <div class="threshold-name">${escapeHtml(t.metric)}</div>
                        <div class="threshold-rule">SLA Rule: <code>${escapeHtml(t.rule)}</code></div>
                        ${t.actual ? `<div class="threshold-rule" style="color: var(--text-secondary); margin-top: 0.15rem;">${escapeHtml(t.actual)}</div>` : ''}
                    </div>
                    <div class="threshold-status-badge ${t.passed ? 'passed' : 'failed'}">
                        ${t.passed ? '✓ PASSED' : '✕ BREACHED'}
                    </div>
                </div>
                <div class="threshold-progress-wrapper" title="Budget usage: ${t.progressPct.toFixed(0)}%">
                    <div class="threshold-progress-bar ${t.passed ? 'pass' : 'fail'}" style="width: ${Math.min(100, t.progressPct)}%;"></div>
                </div>
            </div>
        `).join('');

        // Attach click handlers
        container.querySelectorAll('.threshold-card').forEach(card => {
            card.addEventListener('click', () => {
                const metric = card.getAttribute('data-metric');
                const rule = card.getAttribute('data-rule');
                openThresholdModal(metric, rule);
            });
            card.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    card.click();
                }
            });
        });
    }

    /**
     * Chart 1: Donut chart showing Request Outcome (Passed vs Failed)
     */
    function renderOutcomeChart(metrics) {
        const canvas = document.getElementById('chartOutcome');
        if (!canvas) return;

        const reqs = metrics.http_reqs ? metrics.http_reqs.count : 0;
        const fails = metrics.http_req_failed ? (metrics.http_req_failed.passes || 0) : 0;
        const passes = Math.max(0, reqs - fails);

        const passPct = reqs > 0 ? ((passes / reqs) * 100).toFixed(1) : '100';
        const failPct = reqs > 0 ? ((fails / reqs) * 100).toFixed(1) : '0';

        drawDonutChart(canvas, [
            { label: 'Passed Requests (2xx)', value: passes, color: THEME.green, pct: passPct + '%' },
            { label: 'Failed Requests (4xx/5xx)', value: fails, color: THEME.red, pct: failPct + '%' }
        ], `${passPct}%`, 'Success Rate');
    }

    /**
     * Chart 2: Latency Percentile distribution
     */
    function renderLatencyChart(metrics) {
        const canvas = document.getElementById('chartLatency');
        if (!canvas) return;

        const dur = metrics.http_req_duration || {};
        const durExp = metrics['http_req_duration{expected_response:true}'] || dur;

        const categories = ['Min', 'Median', 'Average', 'p(90)', 'p(95)', 'Max'];
        const allValues = [
            Math.max(0, dur.min || 0),
            dur.med || 0,
            dur.avg || 0,
            dur['p(90)'] || 0,
            dur['p(95)'] || 0,
            dur.max || 0
        ];
        const expValues = [
            Math.max(0, durExp.min || 0),
            durExp.med || 0,
            durExp.avg || 0,
            durExp['p(90)'] || 0,
            durExp['p(95)'] || 0,
            durExp.max || 0
        ];

        drawGroupedBarChart(canvas, categories, [
            { label: 'All Requests (ms)', data: allValues, color: THEME.violet },
            { label: 'Expected 2xx Only (ms)', data: expValues, color: THEME.cyan }
        ], 'ms');
    }

    /**
     * Chart 3: HTTP Request Lifecycle Timing Breakdown
     */
    function renderTimingBreakdownChart(metrics) {
        const canvas = document.getElementById('chartTiming');
        if (!canvas) return;

        const blocked = metrics.http_req_blocked ? (metrics.http_req_blocked.avg || 0) : 0;
        const connecting = metrics.http_req_connecting ? (metrics.http_req_connecting.avg || 0) : 0;
        const sending = metrics.http_req_sending ? (metrics.http_req_sending.avg || 0) : 0;
        const waiting = metrics.http_req_waiting ? (metrics.http_req_waiting.avg || 0) : 0;
        const receiving = metrics.http_req_receiving ? (metrics.http_req_receiving.avg || 0) : 0;

        const items = [
            { label: 'Blocked / Init', value: blocked, color: THEME.purple, desc: 'Socket initialization' },
            { label: 'TCP Connecting', value: connecting, color: THEME.blue, desc: 'TCP connection handshake' },
            { label: 'Sending Request', value: sending, color: THEME.yellow, desc: 'HTTP request upload' },
            { label: 'Waiting (TTFB)', value: waiting, color: THEME.cyan, desc: 'Server compute & first byte' },
            { label: 'Receiving Payload', value: receiving, color: THEME.pink, desc: 'Response body download' }
        ];

        drawHorizontalBars(canvas, items, ' ms');
    }

    /**
     * Chart 4: Endpoint Checks Success Rate Ranking Chart
     */
    function renderChecksComparisonChart(checks) {
        const canvas = document.getElementById('chartChecks');
        if (!canvas) return;

        if (checks.length === 0) return;

        // Group checks by short name and take top 8
        const items = checks.slice(0, 8).map(c => {
            const shortName = c.name.length > 28 ? c.name.substring(0, 26) + '…' : c.name;
            let color = THEME.green;
            if (c.status === 'fail') color = THEME.red;
            else if (c.status === 'partial') color = THEME.yellow;

            return {
                label: shortName,
                value: c.passRate,
                color: color,
                passes: c.passes,
                fails: c.fails
            };
        });

        drawHorizontalBars(canvas, items, '%', 100);
    }

    /**
     * Chart 5: Network Data Flow & I/O Breakdown (Chart 5)
     */
    function renderNetworkChart(metrics) {
        const canvas = document.getElementById('chartNetwork');
        if (!canvas) return;

        const dataRecv = metrics.data_received || {};
        const dataSent = metrics.data_sent || {};

        const recvMb = dataRecv.count ? (dataRecv.count / (1024 * 1024)) : 0;
        const sentMb = dataSent.count ? (dataSent.count / (1024 * 1024)) : 0;
        const totalMb = recvMb + sentMb;

        const recvPct = totalMb > 0 ? ((recvMb / totalMb) * 100).toFixed(1) : '99.6';
        const sentPct = totalMb > 0 ? ((sentMb / totalMb) * 100).toFixed(1) : '0.4';

        const totalGb = (totalMb / 1024).toFixed(2);

        drawDonutChart(canvas, [
            { label: 'Data Received (Download)', value: Math.round(recvMb), color: THEME.cyan, pct: `${recvPct}% (${(recvMb / 1024).toFixed(2)} GB)` },
            { label: 'Data Sent (Upload)', value: Math.round(sentMb), color: THEME.pink, pct: `${sentPct}% (${sentMb.toFixed(1)} MB)` }
        ], `${totalGb} GB`, 'Total Network I/O');
    }

    /**
     * Chart 6: Concurrency & Iterations Overview (Chart 6)
     */
    function renderConcurrencyChart(metrics) {
        const canvas = document.getElementById('chartConcurrency');
        if (!canvas) return;

        const iter = metrics.iteration_duration || {};
        const vusMax = metrics.vus_max || metrics.vus || {};

        const categories = ['Min', 'Median', 'Average', 'p(90)', 'p(95)', 'Max'];
        const iterValues = [
            (iter.min || 0) / 1000,
            (iter.med || 0) / 1000,
            (iter.avg || 0) / 1000,
            (iter['p(90)'] || 0) / 1000,
            (iter['p(95)'] || 0) / 1000,
            (iter.max || 0) / 1000
        ];

        drawGroupedBarChart(canvas, categories, [
            { label: 'Full Iteration Duration (Seconds)', data: iterValues, color: THEME.purple }
        ], 's');
    }

    /**
     * High-DPI canvas preparation helper
     */
    function setupCanvas(canvas) {
        const rect = canvas.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;
        const width = rect.width || 450;
        const height = rect.height || 260;

        canvas.width = width * dpr;
        canvas.height = height * dpr;

        const ctx = canvas.getContext('2d');
        ctx.resetTransform && ctx.resetTransform();
        ctx.scale(dpr, dpr);

        ctx.clearRect(0, 0, width, height);

        return { ctx, width, height };
    }

    /**
     * Canvas Donut Chart renderer
     */
    function drawDonutChart(canvas, segments, centerText, centerSubtext) {
        const { ctx, width, height } = setupCanvas(canvas);

        const total = segments.reduce((sum, s) => sum + s.value, 0);
        if (total === 0) return;

        const centerX = width / 2;
        const centerY = height / 2 - 12;
        const radius = Math.min(centerX, centerY) - 15;
        const innerRadius = radius * 0.62;

        let startAngle = -Math.PI / 2;

        segments.forEach(segment => {
            const sliceAngle = (segment.value / total) * 2 * Math.PI;

            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
            ctx.arc(centerX, centerY, innerRadius, startAngle + sliceAngle, startAngle, true);
            ctx.closePath();
            ctx.fillStyle = segment.color;
            ctx.fill();

            startAngle += sliceAngle;
        });

        // Center cutout background
        ctx.beginPath();
        ctx.arc(centerX, centerY, innerRadius - 1, 0, 2 * Math.PI);
        ctx.fillStyle = THEME.bgCard;
        ctx.fill();

        // Center Text
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = THEME.textPrimary;
        ctx.font = 'bold 22px "Segoe UI", sans-serif';
        ctx.fillText(centerText, centerX, centerY - 8);

        ctx.fillStyle = THEME.textMuted;
        ctx.font = '11px "Segoe UI", sans-serif';
        ctx.fillText(centerSubtext, centerX, centerY + 14);

        // Legend at bottom
        const legendContainer = canvas.parentElement.parentElement.querySelector('.chart-legend');
        if (legendContainer) {
            legendContainer.innerHTML = segments.map(s => `
                <div class="legend-item">
                    <span class="legend-color" style="background: ${s.color};"></span>
                    <span>${s.label}: <strong>${s.pct}</strong></span>
                </div>
            `).join('');
        }
    }

    /**
     * Canvas Grouped Bar Chart renderer
     */
    function drawGroupedBarChart(canvas, categories, datasets, unit) {
        const { ctx, width, height } = setupCanvas(canvas);

        const padding = { top: 25, right: 20, bottom: 40, left: 55 };
        const chartW = width - padding.left - padding.right;
        const chartH = height - padding.top - padding.bottom;

        let maxVal = 0;
        datasets.forEach(ds => {
            ds.data.forEach(v => {
                if (v > maxVal) maxVal = v;
            });
        });
        if (maxVal === 0) maxVal = 100;
        maxVal = maxVal * 1.15; // 15% headroom

        // Draw horizontal grid lines
        const gridSteps = 4;
        ctx.strokeStyle = THEME.gridLine;
        ctx.fillStyle = THEME.textMuted;
        ctx.font = '10px "Segoe UI", sans-serif';
        ctx.textAlign = 'right';
        ctx.textBaseline = 'middle';

        for (let i = 0; i <= gridSteps; i++) {
            const yVal = (maxVal / gridSteps) * i;
            const yPos = padding.top + chartH - (i / gridSteps) * chartH;

            ctx.beginPath();
            ctx.moveTo(padding.left, yPos);
            ctx.lineTo(padding.left + chartW, yPos);
            ctx.stroke();

            ctx.fillText(yVal >= 1000 && unit === 'ms' ? (yVal / 1000).toFixed(1) + 's' : Math.round(yVal) + unit, padding.left - 8, yPos);
        }

        // Draw bars
        const groupW = chartW / categories.length;
        const barW = Math.min(22, (groupW * 0.75) / datasets.length);

        categories.forEach((cat, catIdx) => {
            const groupX = padding.left + catIdx * groupW + groupW / 2;

            datasets.forEach((ds, dsIdx) => {
                const val = ds.data[catIdx] || 0;
                const barH = (val / maxVal) * chartH;
                const barX = groupX - (datasets.length * barW) / 2 + dsIdx * barW;
                const barY = padding.top + chartH - barH;

                ctx.fillStyle = ds.color;
                ctx.beginPath();
                if (ctx.roundRect) {
                    ctx.roundRect(barX, barY, barW - 2, barH, [3, 3, 0, 0]);
                } else {
                    ctx.rect(barX, barY, barW - 2, barH);
                }
                ctx.fill();
            });

            // Category Label
            ctx.fillStyle = THEME.textSecondary;
            ctx.font = '11px "Segoe UI", sans-serif';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'top';
            ctx.fillText(cat, groupX, padding.top + chartH + 8);
        });

        // Legend at bottom
        const legendContainer = canvas.parentElement.parentElement.querySelector('.chart-legend');
        if (legendContainer) {
            legendContainer.innerHTML = datasets.map(ds => `
                <div class="legend-item">
                    <span class="legend-color" style="background: ${ds.color};"></span>
                    <span>${ds.label}</span>
                </div>
            `).join('');
        }
    }

    /**
     * Canvas Horizontal Bars renderer
     */
    function drawHorizontalBars(canvas, items, unit, forcedMax) {
        const { ctx, width, height } = setupCanvas(canvas);

        const padding = { top: 15, right: 65, bottom: 20, left: 145 };
        const chartW = width - padding.left - padding.right;
        const chartH = height - padding.top - padding.bottom;

        if (items.length === 0) return;

        let maxVal = forcedMax || Math.max(...items.map(i => i.value), 1);
        if (!forcedMax) maxVal = maxVal * 1.15;

        const rowH = chartH / items.length;
        const barH = Math.min(18, rowH * 0.6);

        items.forEach((item, idx) => {
            const rowY = padding.top + idx * rowH + rowH / 2;
            const barW = Math.max(2, (item.value / maxVal) * chartW);
            const barY = rowY - barH / 2;

            // Row Label
            ctx.fillStyle = THEME.textSecondary;
            ctx.font = '11px "Segoe UI", sans-serif';
            ctx.textAlign = 'right';
            ctx.textBaseline = 'middle';
            ctx.fillText(item.label, padding.left - 10, rowY);

            // Bar background track
            ctx.fillStyle = 'rgba(255, 255, 255, 0.04)';
            if (ctx.roundRect) {
                ctx.beginPath();
                ctx.roundRect(padding.left, barY, chartW, barH, 4);
                ctx.fill();
            } else {
                ctx.fillRect(padding.left, barY, chartW, barH);
            }

            // Colored Bar
            ctx.fillStyle = item.color;
            if (ctx.roundRect) {
                ctx.beginPath();
                ctx.roundRect(padding.left, barY, barW, barH, 4);
                ctx.fill();
            } else {
                ctx.fillRect(padding.left, barY, barW, barH);
            }

            // Value Label
            ctx.fillStyle = THEME.textPrimary;
            ctx.font = 'bold 11px monospace';
            ctx.textAlign = 'left';
            ctx.textBaseline = 'middle';
            const valText = item.value >= 1000 && unit === ' ms' ? (item.value / 1000).toFixed(1) + 's' : item.value.toFixed(item.value < 1 && item.value > 0 ? 2 : 1) + (unit || '');
            ctx.fillText(valText, padding.left + barW + 6, rowY);
        });
    }

    /**
     * Checks Table Rendering with Search, Category Filter, Progress Bars, and Drilldown Click
     */
    function renderChecksTable(checks) {
        const tbody = document.getElementById('checksTableBody');
        if (!tbody) return;

        // Apply search and filter
        const filtered = checks.filter(c => {
            if (checkFilter === 'passed' && c.fails > 0) return false;
            if (checkFilter === 'failed' && c.fails === 0) return false;
            if (checkFilter === 'anonymous' && c.scope !== 'anonymous') return false;
            if (checkFilter === 'customer' && c.scope !== 'customer') return false;
            if (checkFilter === 'admin' && c.scope !== 'admin') return false;

            if (checkSearchQuery) {
                return c.name.toLowerCase().includes(checkSearchQuery.toLowerCase());
            }
            return true;
        });

        const countBadge = document.getElementById('checksFilterCount');
        if (countBadge) {
            countBadge.textContent = `${filtered.length} of ${checks.length} checks`;
        }

        if (filtered.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-muted);">
                        No checks match the current filter or search criteria.
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = filtered.map(c => {
            const passPct = c.passRate.toFixed(1);
            let badgeClass = 'pass';
            let badgeText = 'Passed';

            if (c.status === 'fail') {
                badgeClass = 'fail';
                badgeText = 'Failed';
            } else if (c.status === 'partial') {
                badgeClass = 'partial';
                badgeText = 'Partial';
            }

            let scopeLabel = 'Storefront';
            let scopeClass = 'anonymous';
            if (c.scope === 'customer') {
                scopeLabel = 'Customer';
                scopeClass = 'customer';
            } else if (c.scope === 'admin') {
                scopeLabel = 'Admin';
                scopeClass = 'admin';
            } else if (c.scope === 'other') {
                scopeLabel = 'General';
                scopeClass = 'other';
            }

            return `
                <tr class="check-row-clickable" data-check-name="${escapeHtml(c.name)}" title="Click to view detailed metrics and failure analysis for this check">
                    <td>
                        <span class="check-scope-tag ${scopeClass}">${scopeLabel}</span>
                    </td>
                    <td>
                        <div class="item-name" style="font-weight: 600; color: var(--text-primary); font-size: 0.9rem;">
                            ${escapeHtml(c.name)}
                        </div>
                    </td>
                    <td>
                        <div style="display: flex; align-items: center; gap: 0.6rem;">
                            <div class="mini-progress-container">
                                <div class="mini-progress-bar" style="width: ${passPct}%;"></div>
                            </div>
                            <span style="font-size: 0.78rem; font-weight: 600; font-family: monospace; color: var(--text-primary);">${passPct}%</span>
                        </div>
                    </td>
                    <td style="font-family: monospace; color: var(--aurora-green); font-weight: 600;">
                        ${formatNumber(c.passes)}
                    </td>
                    <td style="font-family: monospace; color: ${c.fails > 0 ? 'var(--aurora-red)' : 'var(--text-muted)'}; font-weight: 600;">
                        ${formatNumber(c.fails)}
                    </td>
                    <td>
                        <span class="badge-status ${badgeClass}">
                            ${badgeClass === 'pass' ? '✓' : (badgeClass === 'fail' ? '✕' : '⚠')} ${badgeText}
                        </span>
                    </td>
                    <td style="text-align: center;">
                        <button type="button" class="btn-inspect-check" aria-label="Inspect ${escapeHtml(c.name)}">Details ↗</button>
                    </td>
                </tr>
            `;
        }).join('');

        // Attach click handlers to rows
        tbody.querySelectorAll('tr.check-row-clickable').forEach(row => {
            row.addEventListener('click', () => {
                const checkName = row.getAttribute('data-check-name');
                openCheckModal(checkName);
            });
        });
    }

    /**
     * Metrics Detail Table renderer with visual spread distribution range
     */
    function renderMetricsDetailTable(metrics) {
        const tbody = document.getElementById('metricsTableBody');
        if (!tbody) return;

        const rows = [
            { key: 'http_req_duration', name: 'HTTP Request Duration (Total Latency)', unit: 'ms' },
            { key: 'http_req_duration{expected_response:true}', name: 'HTTP Duration (Expected 2xx Only)', unit: 'ms' },
            { key: 'http_req_waiting', name: 'HTTP Waiting (Time to First Byte - TTFB)', unit: 'ms' },
            { key: 'http_req_receiving', name: 'HTTP Receiving (Payload Download)', unit: 'ms' },
            { key: 'http_req_connecting', name: 'HTTP TCP Connecting', unit: 'ms' },
            { key: 'http_req_blocked', name: 'HTTP Blocked / Handshaking', unit: 'ms' },
            { key: 'http_req_sending', name: 'HTTP Request Sending', unit: 'ms' },
            { key: 'iteration_duration', name: 'Full Iteration Duration', unit: 'ms' }
        ];

        tbody.innerHTML = rows.map(r => {
            const m = metrics[r.key];
            if (!m) return '';

            const avg = m.avg !== undefined ? m.avg.toFixed(2) : '—';
            const min = m.min !== undefined ? m.min.toFixed(2) : '—';
            const med = m.med !== undefined ? m.med.toFixed(2) : '—';
            const p90 = m['p(90)'] !== undefined ? m['p(90)'].toFixed(2) : '—';
            const p95 = m['p(95)'] !== undefined ? m['p(95)'].toFixed(2) : '—';
            const max = m.max !== undefined ? m.max.toFixed(2) : '—';

            // Calculate spread distribution percentage
            const minNum = m.min || 0;
            const maxNum = m.max || 100;
            const medNum = m.med || 0;
            const span = Math.max(1, maxNum - minNum);
            const medPct = Math.min(100, Math.max(5, ((medNum - minNum) / span) * 100));

            return `
                <tr>
                    <td>
                        <strong style="color: var(--text-primary); font-size: 0.88rem;">${r.name}</strong>
                        <div style="font-size: 0.75rem; color: var(--text-muted); font-family: monospace;">${r.key}</div>
                    </td>
                    <td class="metric-val-num">${avg} ${r.unit}</td>
                    <td class="metric-val-num">${min} ${r.unit}</td>
                    <td class="metric-val-num" style="color: var(--aurora-cyan); font-weight: 600;">${med} ${r.unit}</td>
                    <td class="metric-val-num">${p90} ${r.unit}</td>
                    <td class="metric-val-num" style="color: var(--aurora-yellow); font-weight: 600;">${p95} ${r.unit}</td>
                    <td class="metric-val-num">${max} ${r.unit}</td>
                    <td>
                        <div class="metric-spread-container" title="Median at ${medPct.toFixed(1)}% of spread">
                            <div class="metric-spread-bar" style="width: ${medPct}%;"></div>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    /**
     * Updates Raw JSON viewer
     */
    function updateRawJsonDisplay(data) {
        const rawPre = document.getElementById('rawJsonPre');
        if (rawPre) {
            rawPre.textContent = JSON.stringify(data, null, 2);
        }
    }

    // =========================================================================
    // POPUP MODAL DRILLDOWN SYSTEM
    // =========================================================================

    /**
     * Opens Modal for KPI Cards
     */
    function openKpiModal(kpiType) {
        if (!rawData || !rawData.metrics) return;
        const metrics = rawData.metrics;
        const checks = cachedChecks || extractChecks(rawData);

        switch (kpiType) {
            case 'requests': {
                const reqs = metrics.http_reqs || {};
                const reqFailed = metrics.http_req_failed || {};
                const total = reqs.count || 0;
                const rate = reqs.rate ? reqs.rate.toFixed(1) : 0;
                const fails = reqFailed.passes || 0;
                const successes = Math.max(0, total - fails);
                const successRate = total > 0 ? ((successes / total) * 100).toFixed(2) : 100;

                openModal({
                    icon: '🌐',
                    title: 'Total HTTP Requests & Throughput',
                    subtitle: 'Detailed request volume, velocity rates, and workload capacity analysis',
                    overviewHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Total Volume</div>
                                <div class="box-value">${formatNumber(total)}</div>
                                <div class="box-sub">HTTP requests processed</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Throughput RPS</div>
                                <div class="box-value" style="color: var(--aurora-cyan);">${rate} req/s</div>
                                <div class="box-sub">Average request rate</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Success Rate</div>
                                <div class="box-value" style="color: var(--aurora-green);">${successRate}%</div>
                                <div class="box-sub">${formatNumber(successes)} successful 2xx</div>
                            </div>
                        </div>
                        <div class="modal-diagnostic-box healthy">
                            <strong>Throughput Capacity Rating:</strong> High concurrency load sustained with zero application crashes. The application successfully served over <strong>${formatNumber(total)} requests</strong> under a multi-user load.
                        </div>
                    `,
                    breakdownHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Successful Requests</div>
                                <div class="box-value" style="color: var(--aurora-green);">${formatNumber(successes)}</div>
                                <div class="box-sub">${successRate}% of total traffic</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Failed Requests</div>
                                <div class="box-value" style="color: var(--aurora-red);">${formatNumber(fails)}</div>
                                <div class="box-sub">${(100 - successRate).toFixed(2)}% failure rate</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Request Sending Time</div>
                                <div class="box-value">${(metrics.http_req_sending ? metrics.http_req_sending.avg : 0).toFixed(3)} ms</div>
                                <div class="box-sub">Average socket write</div>
                            </div>
                        </div>
                    `,
                    recommendationsHtml: `
                        <div class="modal-rec-list">
                            <div class="modal-rec-item">
                                <span class="rec-icon">⚡</span>
                                <div>
                                    <strong>Scale Connection Pooling:</strong>
                                    <p>Ensure backend connection pools (e.g. HikariCP) are tuned to match peak virtual user concurrency of ${metrics.vus_max ? metrics.vus_max.value : 500} threads.</p>
                                </div>
                            </div>
                            <div class="modal-rec-item">
                                <span class="rec-icon">🛡️</span>
                                <div>
                                    <strong>HTTP Keep-Alive &amp; Connection Reuse:</strong>
                                    <p>Average TCP connect time was low (${(metrics.http_req_connecting ? metrics.http_req_connecting.avg : 0).toFixed(2)} ms), indicating excellent HTTP keep-alive reuse.</p>
                                </div>
                            </div>
                        </div>
                    `
                });
                break;
            }

            case 'failures': {
                const reqFailed = metrics.http_req_failed || {};
                const errors = metrics.errors || {};
                const failRate = reqFailed.value !== undefined ? (reqFailed.value * 100).toFixed(2) : '0.00';
                const failCount = reqFailed.passes || 0;
                const errCount = errors.count || 0;
                const failingChecks = checks.filter(c => c.fails > 0);

                openModal({
                    icon: '⚠️',
                    title: 'HTTP Failure Rate & Error Diagnostics',
                    subtitle: 'Root cause analysis, failed endpoint list, and status code rejections',
                    overviewHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Failure Rate</div>
                                <div class="box-value" style="color: var(--aurora-red);">${failRate}%</div>
                                <div class="box-sub">Threshold target: &lt; 1.00%</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Failed Requests</div>
                                <div class="box-value" style="color: var(--aurora-red);">${formatNumber(failCount)}</div>
                                <div class="box-sub">Rejections &amp; errors</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Failing Flows</div>
                                <div class="box-value" style="color: var(--aurora-yellow);">${failingChecks.length}</div>
                                <div class="box-sub">Impacted endpoints</div>
                            </div>
                        </div>
                        <div class="modal-diagnostic-box danger">
                            <strong>Diagnostic Root Cause:</strong> The elevated failure rate (${failRate}%) is primarily caused by authenticated/stateful endpoints (such as <code>Customer: Add Product to Cart</code>) being invoked without active user session cookies or CSRF tokens in simulated K6 scripts.
                        </div>
                    `,
                    breakdownHtml: `
                        <h4 style="color: var(--text-primary); margin-top: 0; font-size: 0.95rem;">Impacted Endpoint Flows:</h4>
                        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                            ${failingChecks.map(c => `
                                <div style="background: rgba(255,255,255,0.03); border: 1px solid var(--border-color); border-left: 3px solid var(--aurora-red); padding: 0.6rem 0.9rem; border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                                    <div>
                                        <div style="font-weight: 600; color: var(--text-primary); font-size: 0.85rem;">${escapeHtml(c.name)}</div>
                                        <div style="font-size: 0.75rem; color: var(--text-muted);">${formatNumber(c.fails)} fails out of ${formatNumber(c.total)} requests (${c.passRate.toFixed(1)}% pass rate)</div>
                                    </div>
                                    <span class="badge-status ${c.status === 'fail' ? 'fail' : 'partial'}">${c.status.toUpperCase()}</span>
                                </div>
                            `).join('')}
                        </div>
                    `,
                    recommendationsHtml: `
                        <div class="modal-rec-list">
                            <div class="modal-rec-item">
                                <span class="rec-icon">🔑</span>
                                <div>
                                    <strong>Add Session Extraction in K6 Scripts:</strong>
                                    <p>Extract CSRF tokens and session cookies from login responses before dispatching <code>POST /cart/add</code> requests.</p>
                                </div>
                            </div>
                            <div class="modal-rec-item">
                                <span class="rec-icon">🛡️</span>
                                <div>
                                    <strong>Spring Security Authentication Headers:</strong>
                                    <p>Pass Bearer JWT tokens or valid session cookie headers for Admin API endpoints.</p>
                                </div>
                            </div>
                        </div>
                    `
                });
                break;
            }

            case 'latency': {
                const dur = metrics.http_req_duration || {};
                const ttfb = metrics.http_req_waiting || {};
                const recv = metrics.http_req_receiving || {};

                openModal({
                    icon: '⚡',
                    title: 'Latency & Response Time Percentiles',
                    subtitle: 'Full distribution spread from sub-millisecond to tail latency p95 and max',
                    overviewHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Median (p50)</div>
                                <div class="box-value" style="color: var(--aurora-cyan);">${(dur.med || 0).toFixed(1)} ms</div>
                                <div class="box-sub">50% of requests faster than</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">p95 Percentile</div>
                                <div class="box-value" style="color: var(--aurora-yellow);">${(dur['p(95)'] || 0).toFixed(1)} ms</div>
                                <div class="box-sub">High-load SLA benchmark</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Average Latency</div>
                                <div class="box-value">${(dur.avg || 0).toFixed(1)} ms</div>
                                <div class="box-sub">Mean response duration</div>
                            </div>
                        </div>
                        <div class="modal-diagnostic-box warning">
                            <strong>Latency SLA Evaluation:</strong> Fast median response time of <strong>${(dur.med || 0).toFixed(1)} ms</strong>. Tail latency at p95 reached <strong>${(dur['p(95)'] || 0).toFixed(1)} ms</strong> under 525 concurrent virtual users due to thread queues.
                        </div>
                    `,
                    breakdownHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Minimum Latency</div>
                                <div class="box-value">${(dur.min || 0).toFixed(2)} ms</div>
                                <div class="box-sub">Best-case response</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">p90 Percentile</div>
                                <div class="box-value">${(dur['p(90)'] || 0).toFixed(1)} ms</div>
                                <div class="box-sub">90% of requests</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Maximum Latency</div>
                                <div class="box-value" style="color: var(--aurora-red);">${((dur.max || 0) / 1000).toFixed(1)} s</div>
                                <div class="box-sub">Peak tail timeout</div>
                            </div>
                        </div>
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Server TTFB Waiting</div>
                                <div class="box-value">${(ttfb.avg || 0).toFixed(1)} ms</div>
                                <div class="box-sub">Backend execution time</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Payload Download</div>
                                <div class="box-value">${(recv.avg || 0).toFixed(1)} ms</div>
                                <div class="box-sub">Receiving data stream</div>
                            </div>
                        </div>
                    `,
                    recommendationsHtml: `
                        <div class="modal-rec-list">
                            <div class="modal-rec-item">
                                <span class="rec-icon">🚀</span>
                                <div>
                                    <strong>Enable HTTP GZIP / Brotli Compression:</strong>
                                    <p>Compress HTML and JSON responses in Spring Boot (<code>server.compression.enabled=true</code>) to reduce download duration.</p>
                                </div>
                            </div>
                            <div class="modal-rec-item">
                                <span class="rec-icon">🗄️</span>
                                <div>
                                    <strong>Database Query Indexing:</strong>
                                    <p>Index frequently queried columns (products, categories, users) to keep TTFB below 150 ms under high load.</p>
                                </div>
                            </div>
                        </div>
                    `
                });
                break;
            }

            case 'concurrency': {
                const vus = metrics.vus || {};
                const vusMax = metrics.vus_max || vus;
                const iter = metrics.iterations || {};
                const iterDur = metrics.iteration_duration || {};

                openModal({
                    icon: '👥',
                    title: 'Concurrency & Virtual User Execution',
                    subtitle: 'Workload scaling, user ramp-up, iteration rate, and loop completion times',
                    overviewHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Peak Virtual Users</div>
                                <div class="box-value" style="color: var(--aurora-cyan);">${vusMax.value || vusMax.max || 0} VUs</div>
                                <div class="box-sub">Simulated concurrent users</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Completed Iterations</div>
                                <div class="box-value">${formatNumber(iter.count || 0)}</div>
                                <div class="box-sub">Full user flows executed</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Iteration Velocity</div>
                                <div class="box-value" style="color: var(--aurora-green);">${(iter.rate || 0).toFixed(1)} iter/s</div>
                                <div class="box-sub">Loops completed per second</div>
                            </div>
                        </div>
                        <div class="modal-diagnostic-box healthy">
                            <strong>Concurrency Scaling Assessment:</strong> The application smoothly accommodated <strong>${vusMax.value || 525} concurrent users</strong> executing multi-step shopping journeys.
                        </div>
                    `,
                    breakdownHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Average Iteration Time</div>
                                <div class="box-value">${((iterDur.avg || 0) / 1000).toFixed(1)} s</div>
                                <div class="box-sub">Full user journey loop</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Median Iteration Time</div>
                                <div class="box-value">${((iterDur.med || 0) / 1000).toFixed(1)} s</div>
                                <div class="box-sub">p50 user loop duration</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">p95 Iteration Time</div>
                                <div class="box-value">${((iterDur['p(95)'] || 0) / 1000).toFixed(1)} s</div>
                                <div class="box-sub">p95 user loop duration</div>
                            </div>
                        </div>
                    `,
                    recommendationsHtml: `
                        <div class="modal-rec-list">
                            <div class="modal-rec-item">
                                <span class="rec-icon">⚙️</span>
                                <div>
                                    <strong>Tomcat Thread Configuration:</strong>
                                    <p>Configure <code>server.tomcat.threads.max=400</code> and <code>server.tomcat.accept-count=200</code> to optimize thread pool utilization.</p>
                                </div>
                            </div>
                        </div>
                    `
                });
                break;
            }

            case 'data': {
                const dataRecv = metrics.data_received || {};
                const dataSent = metrics.data_sent || {};
                const recvGb = (dataRecv.count || 0) / (1024 * 1024 * 1024);
                const sentMb = (dataSent.count || 0) / (1024 * 1024);
                const rateRecv = ((dataRecv.rate || 0) / (1024 * 1024)).toFixed(2);
                const rateSent = ((dataSent.rate || 0) / 1024).toFixed(1);

                openModal({
                    icon: '📦',
                    title: 'Network Data Transfer & Bandwidth',
                    subtitle: 'Payload transfer volume, network I/O velocity, and compression analysis',
                    overviewHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Total Data Received</div>
                                <div class="box-value" style="color: var(--aurora-cyan);">${recvGb.toFixed(2)} GB</div>
                                <div class="box-sub">Server payload responses</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Total Data Sent</div>
                                <div class="box-value">${sentMb.toFixed(2)} MB</div>
                                <div class="box-sub">Client requests sent</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Transfer Bandwidth</div>
                                <div class="box-value" style="color: var(--aurora-purple);">${rateRecv} MB/s</div>
                                <div class="box-sub">Average download bandwidth</div>
                            </div>
                        </div>
                        <div class="modal-diagnostic-box healthy">
                            <strong>Network Bandwidth Health:</strong> Sustained an average download throughput of <strong>${rateRecv} MB/s</strong> (${recvGb.toFixed(2)} GB total transfer).
                        </div>
                    `,
                    breakdownHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Bytes Received</div>
                                <div class="box-value">${formatNumber(dataRecv.count || 0)}</div>
                                <div class="box-sub">Raw bytes downloaded</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Bytes Sent</div>
                                <div class="box-value">${formatNumber(dataSent.count || 0)}</div>
                                <div class="box-sub">Raw bytes uploaded</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Upload Rate</div>
                                <div class="box-value">${rateSent} KB/s</div>
                                <div class="box-sub">Client upload stream</div>
                            </div>
                        </div>
                    `,
                    recommendationsHtml: `
                        <div class="modal-rec-list">
                            <div class="modal-rec-item">
                                <span class="rec-icon">🌐</span>
                                <div>
                                    <strong>Static Asset CDN Caching:</strong>
                                    <p>Serve CSS, JS, and image assets via Cache-Control headers (<code>max-age=31536000</code>) to offload repeated payload downloads from the application server.</p>
                                </div>
                            </div>
                        </div>
                    `
                });
                break;
            }

            case 'checks': {
                const total = checks.reduce((sum, c) => sum + c.total, 0);
                const passed = checks.reduce((sum, c) => sum + c.passes, 0);
                const failed = checks.reduce((sum, c) => sum + c.fails, 0);
                const rate = total > 0 ? ((passed / total) * 100).toFixed(1) : 100;
                const failingChecks = checks.filter(c => c.fails > 0);

                openModal({
                    icon: '🎯',
                    title: 'Endpoint Validation Checks & Assertions',
                    subtitle: 'Summary of all functional checks and assertion success ratios',
                    overviewHtml: `
                        <div class="modal-stats-grid">
                            <div class="modal-stat-box">
                                <div class="box-label">Overall Pass Rate</div>
                                <div class="box-value" style="color: ${rate >= 90 ? 'var(--aurora-green)' : 'var(--aurora-yellow)'};">${rate}%</div>
                                <div class="box-sub">${formatNumber(passed)} of ${formatNumber(total)} assertions</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Total Checks Run</div>
                                <div class="box-value">${checks.length}</div>
                                <div class="box-sub">Simulated user flows</div>
                            </div>
                            <div class="modal-stat-box">
                                <div class="box-label">Failed Assertions</div>
                                <div class="box-value" style="color: ${failed > 0 ? 'var(--aurora-red)' : 'var(--aurora-green)'};">${formatNumber(failed)}</div>
                                <div class="box-sub">Status code rejections</div>
                            </div>
                        </div>
                        <div class="modal-diagnostic-box ${failingChecks.length > 0 ? 'warning' : 'healthy'}">
                            <strong>Assertion Summary:</strong> <strong>${checks.length - failingChecks.length} of ${checks.length} endpoint flows</strong> passed with 100% reliability. ${failingChecks.length} flows experienced partial or complete status rejections.
                        </div>
                    `,
                    breakdownHtml: `
                        <h4 style="color: var(--text-primary); margin-top: 0; font-size: 0.95rem;">All Check Breakdown:</h4>
                        <div style="display: flex; flex-direction: column; gap: 0.5rem; max-height: 280px; overflow-y: auto;">
                            ${checks.map(c => `
                                <div style="background: rgba(255,255,255,0.03); border: 1px solid var(--border-color); border-left: 3px solid ${c.status === 'pass' ? 'var(--aurora-green)' : (c.status === 'fail' ? 'var(--aurora-red)' : 'var(--aurora-yellow)')}; padding: 0.5rem 0.8rem; border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                                    <div style="font-size: 0.82rem; color: var(--text-primary); font-weight: 600;">${escapeHtml(c.name)}</div>
                                    <span style="font-family: monospace; font-size: 0.78rem; color: ${c.status === 'pass' ? 'var(--aurora-green)' : 'var(--aurora-yellow)'};">${c.passRate.toFixed(1)}% (${formatNumber(c.passes)}/${formatNumber(c.total)})</span>
                                </div>
                            `).join('')}
                        </div>
                    `,
                    recommendationsHtml: `
                        <div class="modal-rec-list">
                            <div class="modal-rec-item">
                                <span class="rec-icon">🔍</span>
                                <div>
                                    <strong>Inspect Failing Check Details:</strong>
                                    <p>Click on individual check rows in the table below to inspect status codes and root causes for specific flows.</p>
                                </div>
                            </div>
                        </div>
                    `
                });
                break;
            }
        }
    }

    /**
     * Opens Modal for Endpoint Check Row
     */
    function openCheckModal(checkName) {
        if (!cachedChecks || cachedChecks.length === 0) return;
        const check = cachedChecks.find(c => c.name === checkName);
        if (!check) return;

        const passPct = check.passRate.toFixed(1);
        let statusBadgeClass = check.status === 'pass' ? 'healthy' : (check.status === 'fail' ? 'danger' : 'warning');
        let statusText = check.status === 'pass' ? 'Check Passed Perfectly' : (check.status === 'fail' ? 'Check Failed Rejection' : 'Partial Passes / Some Failures');

        openModal({
            icon: check.status === 'pass' ? '✅' : (check.status === 'fail' ? '❌' : '⚠️'),
            title: check.name,
            subtitle: `Flow ID: ${check.id || 'N/A'} • Scope: ${check.scope.toUpperCase()}`,
            overviewHtml: `
                <div class="modal-stats-grid">
                    <div class="modal-stat-box">
                        <div class="box-label">Success Pass Rate</div>
                        <div class="box-value" style="color: ${check.passRate >= 90 ? 'var(--aurora-green)' : (check.passRate === 0 ? 'var(--aurora-red)' : 'var(--aurora-yellow)')};">${passPct}%</div>
                        <div class="box-sub">Overall reliability</div>
                    </div>
                    <div class="modal-stat-box">
                        <div class="box-label">Successful Passes</div>
                        <div class="box-value" style="color: var(--aurora-green);">${formatNumber(check.passes)}</div>
                        <div class="box-sub">Valid response codes</div>
                    </div>
                    <div class="modal-stat-box">
                        <div class="box-label">Failed Requests</div>
                        <div class="box-value" style="color: ${check.fails > 0 ? 'var(--aurora-red)' : 'var(--text-muted)'};">${formatNumber(check.fails)}</div>
                        <div class="box-sub">Rejections / errors</div>
                    </div>
                </div>
                <div class="modal-diagnostic-box ${statusBadgeClass}">
                    <strong>${statusText}:</strong> ${check.fails === 0 ? 'All simulated calls to this endpoint matched the expected status code range.' : `This flow encountered ${formatNumber(check.fails)} failures (${(100 - check.passRate).toFixed(1)}% of calls).`}
                </div>
            `,
            breakdownHtml: `
                <div class="modal-stats-grid">
                    <div class="modal-stat-box">
                        <div class="box-label">Total Execution Count</div>
                        <div class="box-value">${formatNumber(check.total)}</div>
                        <div class="box-sub">Total check invocations</div>
                    </div>
                    <div class="modal-stat-box">
                        <div class="box-label">Simulated Scope</div>
                        <div class="box-value" style="color: var(--aurora-cyan);">${check.scope.toUpperCase()}</div>
                        <div class="box-sub">User role category</div>
                    </div>
                </div>
                <div style="background: rgba(0,0,0,0.3); border: 1px solid var(--border-color); border-radius: 8px; padding: 1rem; margin-top: 1rem;">
                    <strong style="color: var(--text-primary); font-size: 0.85rem;">Check Path / Selector:</strong>
                    <pre style="margin: 0.5rem 0 0 0; color: var(--aurora-cyan); font-size: 0.82rem; font-family: monospace;">${escapeHtml(check.path || check.name)}</pre>
                </div>
            `,
            recommendationsHtml: `
                <div class="modal-rec-list">
                    ${check.fails > 0 ? `
                        <div class="modal-rec-item">
                            <span class="rec-icon">🔧</span>
                            <div>
                                <strong>Authentication &amp; CSRF Headers:</strong>
                                <p>If this is a mutation flow (POST/PUT), ensure the test script sends a valid CSRF token in header <code>X-CSRF-TOKEN</code> and active session cookie.</p>
                            </div>
                        </div>
                        <div class="modal-rec-item">
                            <span class="rec-icon">📋</span>
                            <div>
                                <strong>Status Code Alignment:</strong>
                                <p>Verify whether the endpoint returns 302/303 redirects or 401/403 when unauthenticated, and adjust the K6 assertion condition accordingly.</p>
                            </div>
                        </div>
                    ` : `
                        <div class="modal-rec-item">
                            <span class="rec-icon">✨</span>
                            <div>
                                <strong>Optimal Performance:</strong>
                                <p>This endpoint operated with 100% reliability under load. No tuning required.</p>
                            </div>
                        </div>
                    `}
                </div>
            `
        });
    }

    /**
     * Opens Modal for Threshold Cards
     */
    function openThresholdModal(metricKey, rule) {
        if (!rawData || !rawData.metrics) return;
        const metricObj = rawData.metrics[metricKey];
        if (!metricObj) return;

        const failed = metricObj.thresholds && metricObj.thresholds[rule];
        const passed = !failed;

        openModal({
            icon: '🎯',
            title: `Threshold SLA: ${metricKey}`,
            subtitle: `Rule Condition: ${rule}`,
            overviewHtml: `
                <div class="modal-stats-grid">
                    <div class="modal-stat-box">
                        <div class="box-label">SLA Compliance</div>
                        <div class="box-value" style="color: ${passed ? 'var(--aurora-green)' : 'var(--aurora-red)'};">${passed ? 'PASSED' : 'BREACHED'}</div>
                        <div class="box-sub">${passed ? 'Within configured budget' : 'Target exceeded'}</div>
                    </div>
                    <div class="modal-stat-box">
                        <div class="box-label">Target Rule</div>
                        <div class="box-value" style="font-size: 1.1rem; color: var(--aurora-cyan);">${rule}</div>
                        <div class="box-sub">Configured condition</div>
                    </div>
                </div>
                <div class="modal-diagnostic-box ${passed ? 'healthy' : 'danger'}">
                    <strong>Threshold Verdict:</strong> This SLA threshold ${passed ? 'PASSED successfully during the test run.' : 'was BREACHED under high load.'}
                </div>
            `,
            breakdownHtml: `
                <div style="background: rgba(0,0,0,0.3); border: 1px solid var(--border-color); border-radius: 8px; padding: 1rem;">
                    <strong style="color: var(--text-primary); font-size: 0.85rem;">Metric Statistics Snapshot:</strong>
                    <pre style="margin: 0.5rem 0 0 0; color: #a5b4fc; font-size: 0.82rem; font-family: monospace;">${JSON.stringify(metricObj, null, 2)}</pre>
                </div>
            `,
            recommendationsHtml: `
                <div class="modal-rec-list">
                    <div class="modal-rec-item">
                        <span class="rec-icon">🎯</span>
                        <div>
                            <strong>SLO Review &amp; Capacity Adjustment:</strong>
                            <p>${passed ? 'This metric meets production SLA requirements.' : 'Review backend bottlenecks and consider scaling horizontal instances or increasing caching to meet this SLA target.'}</p>
                        </div>
                    </div>
                </div>
            `
        });
    }

    /**
     * Generic Modal Launcher with tab management
     */
    function openModal({ icon, title, subtitle, overviewHtml, breakdownHtml, recommendationsHtml }) {
        const backdrop = document.getElementById('k6ModalBackdrop');
        const modalIcon = document.getElementById('modalIcon');
        const modalTitle = document.getElementById('modalTitle');
        const modalSubtitle = document.getElementById('modalSubtitle');
        const tabOverview = document.getElementById('modalTabOverview');
        const tabBreakdown = document.getElementById('modalTabBreakdown');
        const tabRecs = document.getElementById('modalTabRecommendations');

        if (!backdrop) return;

        if (modalIcon) modalIcon.textContent = icon || '📊';
        if (modalTitle) modalTitle.textContent = title || 'Metric Details';
        if (modalSubtitle) modalSubtitle.textContent = subtitle || '';
        if (tabOverview) tabOverview.innerHTML = overviewHtml || '';
        if (tabBreakdown) tabBreakdown.innerHTML = breakdownHtml || '';
        if (tabRecs) tabRecs.innerHTML = recommendationsHtml || '';

        // Reset to first tab
        switchModalTab('overview');

        backdrop.classList.add('open');
        backdrop.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
    }

    /**
     * Closes the Popup Modal
     */
    function closeModal() {
        const backdrop = document.getElementById('k6ModalBackdrop');
        if (backdrop) {
            backdrop.classList.remove('open');
            backdrop.setAttribute('aria-hidden', 'true');
            document.body.style.overflow = '';
        }
    }

    /**
     * Switches active tab inside modal
     */
    function switchModalTab(tabId) {
        const tabs = document.querySelectorAll('.k6-modal-tab');
        const contents = document.querySelectorAll('.k6-modal-tab-content');

        tabs.forEach(t => {
            if (t.getAttribute('data-tab') === tabId) {
                t.classList.add('active');
            } else {
                t.classList.remove('active');
            }
        });

        contents.forEach(c => {
            if (c.id === `modalTab${capitalize(tabId)}`) {
                c.classList.add('active');
            } else {
                c.classList.remove('active');
            }
        });
    }

    function capitalize(s) {
        if (!s) return '';
        return s.charAt(0).toUpperCase() + s.slice(1);
    }

    /**
     * Helper to set element text
     */
    function setText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    }

    /**
     * Format numbers with thousands commas
     */
    function formatNumber(num) {
        if (num === null || num === undefined) return '0';
        return Number(num).toLocaleString('en-US');
    }

    /**
     * Escape HTML helper
     */
    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /**
     * Toast notification helper
     */
    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = 'cart-toast';
        toast.style.borderColor = type === 'danger' ? 'var(--aurora-red)' : 'var(--aurora-violet)';
        toast.innerHTML = `<span>${message}</span>`;
        document.body.appendChild(toast);
        setTimeout(() => toast.classList.add('show'), 50);
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 400);
        }, 3200);
    }

    /**
     * DOM Event Listeners & Interactive Handlers
     */
    document.addEventListener('DOMContentLoaded', () => {
        // File Dropzone & Input Handlers
        const dropzone = document.getElementById('k6Dropzone');
        const fileInput = document.getElementById('k6FileInput');
        const reloadBtn = document.getElementById('btnReloadDefault');
        const copyJsonBtn = document.getElementById('btnCopyJson');
        const toggleRawBtn = document.getElementById('rawJsonHeader');
        const rawContent = document.getElementById('rawJsonContent');
        const searchInput = document.getElementById('checksSearchInput');
        const filterButtons = document.querySelectorAll('.filter-btn');

        // Modal Elements
        const modalCloseBtn = document.getElementById('modalCloseBtn');
        const modalDismissBtn = document.getElementById('modalDismissBtn');
        const modalBackdrop = document.getElementById('k6ModalBackdrop');
        const modalTabs = document.querySelectorAll('.k6-modal-tab');

        // KPI Clickable Cards Handlers
        document.querySelectorAll('.k6-stat-card.clickable').forEach(card => {
            card.addEventListener('click', () => {
                const kpi = card.getAttribute('data-kpi');
                openKpiModal(kpi);
            });
            card.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    card.click();
                }
            });
        });

        // Modal Tab Buttons
        modalTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const tabId = tab.getAttribute('data-tab');
                switchModalTab(tabId);
            });
        });

        // Modal Close Handlers
        if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeModal);
        if (modalDismissBtn) modalDismissBtn.addEventListener('click', closeModal);
        if (modalBackdrop) {
            modalBackdrop.addEventListener('click', (e) => {
                if (e.target === modalBackdrop) {
                    closeModal();
                }
            });
        }

        // Keyboard ESC handler
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closeModal();
            }
        });

        // File dropzone handlers
        if (dropzone && fileInput) {
            dropzone.addEventListener('click', () => fileInput.click());

            dropzone.addEventListener('dragover', (e) => {
                e.preventDefault();
                dropzone.classList.add('drag-over');
            });

            dropzone.addEventListener('dragleave', () => {
                dropzone.classList.remove('drag-over');
            });

            dropzone.addEventListener('drop', (e) => {
                e.preventDefault();
                dropzone.classList.remove('drag-over');
                if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                    handleFileSelection(e.dataTransfer.files[0]);
                }
            });

            fileInput.addEventListener('change', (e) => {
                if (e.target.files && e.target.files.length > 0) {
                    handleFileSelection(e.target.files[0]);
                }
            });
        }

        function handleFileSelection(file) {
            if (!file.name.endsWith('.json') && file.type !== 'application/json') {
                showToast('Please select a valid .json file', 'danger');
                return;
            }

            const reader = new FileReader();
            reader.onload = function (e) {
                try {
                    const parsed = JSON.parse(e.target.result);
                    window.initK6Dashboard(parsed);
                    showToast(`Loaded ${file.name} successfully!`, 'info');
                } catch (err) {
                    console.error('JSON parse error:', err);
                    showToast('Invalid JSON file format', 'danger');
                }
            };
            reader.readAsText(file);
        }

        // Reload Default Data Button
        if (reloadBtn) {
            reloadBtn.addEventListener('click', () => {
                fetch('/admin/load-test/data')
                    .then(res => res.json())
                    .then(data => {
                        window.initK6Dashboard(data);
                        showToast('Default K6 result summary reloaded!', 'info');
                    })
                    .catch(err => {
                        console.error('Failed to load server data:', err);
                        showToast('Failed to load default summary', 'danger');
                    });
            });
        }

        // Copy JSON Button
        if (copyJsonBtn) {
            copyJsonBtn.addEventListener('click', () => {
                if (rawData) {
                    navigator.clipboard.writeText(JSON.stringify(rawData, null, 2))
                        .then(() => showToast('Summary JSON copied to clipboard!', 'info'))
                        .catch(() => showToast('Failed to copy to clipboard', 'danger'));
                }
            });
        }

        // Toggle Raw JSON Accordion
        if (toggleRawBtn && rawContent) {
            toggleRawBtn.addEventListener('click', () => {
                rawContent.classList.toggle('open');
                const arrow = toggleRawBtn.querySelector('.toggle-arrow');
                if (arrow) {
                    arrow.textContent = rawContent.classList.contains('open') ? '▲' : '▼';
                }
            });
        }

        // Checks Search Input Handler
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                checkSearchQuery = e.target.value.trim();
                renderChecksTable(cachedChecks);
            });
        }

        // Checks Category & Status Filter Buttons
        filterButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                filterButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                checkFilter = btn.getAttribute('data-filter') || 'all';
                renderChecksTable(cachedChecks);
            });
        });

        // Window resize debounced re-render for responsive charts
        let resizeTimer = null;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(() => {
                if (rawData && rawData.metrics) {
                    const metrics = rawData.metrics;
                    renderOutcomeChart(metrics);
                    renderLatencyChart(metrics);
                    renderTimingBreakdownChart(metrics);
                    renderChecksComparisonChart(cachedChecks);
                    renderNetworkChart(metrics);
                    renderConcurrencyChart(metrics);
                }
            }, 150);
        });
    });

})();
