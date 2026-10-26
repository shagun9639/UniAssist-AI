/**
 * UniAssist AI – ML Model Analytics & Evaluation JavaScript
 * Grounded in empirical test metrics, classification reports, and model comparisons.
 */

document.addEventListener("DOMContentLoaded", async () => {
    setupAuthNavbar();
    await loadModelAnalytics();
});

function setupAuthNavbar() {
    const authContainer = document.getElementById("navbar-auth-section");
    if (!authContainer) return;

    const user = api.getAuthUser();
    if (user) {
        authContainer.innerHTML = `
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-primary dropdown-toggle d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                    <span class="avatar-badge-nav">${user.name ? user.name.charAt(0).toUpperCase() : 'U'}</span>
                    <span class="d-none d-sm-inline">${user.name || user.sub}</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                    <li><h6 class="dropdown-header">${user.role === 'ROLE_ADMIN' ? 'Administrator' : 'Student'}</h6></li>
                    ${user.role === 'ROLE_ADMIN' ? '<li><a class="dropdown-item" href="admin.html"><i class="bi bi-speedometer2 me-2"></i>Admin Console</a></li>' : ''}
                    <li><a class="dropdown-item" href="dashboard.html"><i class="bi bi-grid-fill me-2"></i>Dashboard</a></li>
                    <li><a class="dropdown-item" href="profile.html"><i class="bi bi-person-fill me-2"></i>My Profile</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="#" id="analytics-logout-btn"><i class="bi bi-box-arrow-right me-2"></i>Sign Out</a></li>
                </ul>
            </div>
        `;
        document.getElementById("analytics-logout-btn")?.addEventListener("click", (e) => {
            e.preventDefault();
            api.logout();
            window.location.href = "login.html";
        });
    } else {
        authContainer.innerHTML = `
            <div class="d-flex gap-2">
                <a href="login.html" class="btn btn-sm btn-outline-primary">Sign In</a>
                <a href="register.html" class="btn btn-sm btn-primary">Register</a>
            </div>
        `;
    }
}

// Fallback empirical metrics grounded from training/train_model.py run on 82 held-out samples
const FALLBACK_METRICS = {
    model_name: "Calibrated Linear SVC (Model B)",
    test_accuracy: 0.7073,
    macro_precision: 0.7058,
    macro_recall: 0.7050,
    macro_f1: 0.6825,
    weighted_f1: 0.6864,
    total_samples: 408,
    classes: [
        "academic_calendar", "admission_eligibility", "admissions", "assignments",
        "attendance_policy", "campus_amenities", "course_information", "exam_results",
        "exam_schedule", "faculty_contacts", "fee_payment", "goodbye", "greeting",
        "hostel_information", "library_facilities", "scholarships", "technical_support",
        "timetable", "transport_facilities", "university_holidays"
    ],
    classification_report: {
        "academic_calendar": { precision: 1.0, recall: 0.25, "f1-score": 0.4000, support: 4 },
        "admission_eligibility": { precision: 0.6000, recall: 0.75, "f1-score": 0.6667, support: 4 },
        "admissions": { precision: 0.8000, recall: 1.0, "f1-score": 0.8889, support: 4 },
        "assignments": { precision: 0.4286, recall: 0.75, "f1-score": 0.5455, support: 4 },
        "attendance_policy": { precision: 1.0, recall: 1.0, "f1-score": 1.0000, support: 4 },
        "campus_amenities": { precision: 0.4000, recall: 0.50, "f1-score": 0.4444, support: 4 },
        "course_information": { precision: 0.6667, recall: 0.50, "f1-score": 0.5714, support: 4 },
        "exam_results": { precision: 0.0, recall: 0.0, "f1-score": 0.0, support: 4 },
        "exam_schedule": { precision: 0.7500, recall: 0.75, "f1-score": 0.7500, support: 4 },
        "faculty_contacts": { precision: 0.7500, recall: 0.75, "f1-score": 0.7500, support: 4 },
        "fee_payment": { precision: 0.7500, recall: 0.75, "f1-score": 0.7500, support: 4 },
        "goodbye": { precision: 0.8000, recall: 0.80, "f1-score": 0.8000, support: 5 },
        "greeting": { precision: 1.0, recall: 0.80, "f1-score": 0.8889, support: 5 },
        "hostel_information": { precision: 0.6667, recall: 0.50, "f1-score": 0.5714, support: 4 },
        "library_facilities": { precision: 1.0, recall: 1.0, "f1-score": 1.0000, support: 4 },
        "scholarships": { precision: 0.6667, recall: 0.50, "f1-score": 0.5714, support: 4 },
        "technical_support": { precision: 0.6000, recall: 0.75, "f1-score": 0.6667, support: 4 },
        "timetable": { precision: 1.0, recall: 0.75, "f1-score": 0.8571, support: 4 },
        "transport_facilities": { precision: 0.5714, recall: 1.0, "f1-score": 0.7273, support: 4 },
        "university_holidays": { precision: 0.6667, recall: 1.0, "f1-score": 0.8000, support: 4 },
        "macro avg": { precision: 0.7058, recall: 0.7050, "f1-score": 0.6825, support: 82 },
        "weighted avg": { precision: 0.7106, recall: 0.7073, "f1-score": 0.6864, support: 82 }
    }
};

async function loadModelAnalytics() {
    let metricsData = null;

    try {
        metricsData = await api.getModelMetrics();
    } catch (e) {
        console.warn("Could not fetch remote model metrics via API; falling back to offline empirical metrics:", e);
        metricsData = FALLBACK_METRICS;
    }

    if (!metricsData || !metricsData.classification_report) {
        metricsData = FALLBACK_METRICS;
    }

    // Populate top stat cards
    const accEl = document.getElementById("ml-stat-accuracy");
    const f1El = document.getElementById("ml-stat-f1");
    const clsEl = document.getElementById("ml-stat-classes");
    const smpEl = document.getElementById("ml-stat-samples");

    if (accEl) {
        const acc = metricsData.test_accuracy || 0.7073;
        accEl.textContent = `${(acc * 100).toFixed(2)}%`;
    }
    if (f1El) {
        const f1 = metricsData.macro_f1 || 0.6825;
        f1El.textContent = Number(f1).toFixed(4);
    }
    if (clsEl) {
        const classCount = metricsData.classes ? metricsData.classes.length : 20;
        clsEl.textContent = `${classCount} Classes`;
    }
    if (smpEl) {
        smpEl.textContent = "408 Examples";
    }

    // Populate per-intent classification table
    renderClassificationTable(metricsData.classification_report);
}

function renderClassificationTable(report) {
    const tbody = document.getElementById("per-intent-table-body");
    if (!tbody || !report) return;

    let rowsHtml = "";

    const formatPercent = (val) => {
        if (val === undefined || val === null || isNaN(val)) return "0.0%";
        return `${(val * 100).toFixed(1)}%`;
    };

    const getScoreBadge = (score) => {
        if (score >= 0.85) return `<span class="badge bg-success">${(score * 100).toFixed(1)}%</span>`;
        if (score >= 0.65) return `<span class="badge bg-primary text-white">${(score * 100).toFixed(1)}%</span>`;
        if (score >= 0.40) return `<span class="badge bg-warning text-dark">${(score * 100).toFixed(1)}%</span>`;
        return `<span class="badge bg-danger">${(score * 100).toFixed(1)}%</span>`;
    };

    // Sort class names alphabetically
    const intentKeys = Object.keys(report).filter(k => k !== "accuracy" && k !== "macro avg" && k !== "weighted avg");
    intentKeys.sort();

    intentKeys.forEach(intent => {
        const item = report[intent] || {};
        const p = item.precision ?? 0;
        const r = item.recall ?? 0;
        const f1 = item["f1-score"] ?? 0;
        const s = item.support ?? 0;

        rowsHtml += `
            <tr>
                <td class="text-start">
                    <code class="text-primary fw-semibold">${escapeHtml(intent)}</code>
                </td>
                <td>${formatPercent(p)}</td>
                <td>${formatPercent(r)}</td>
                <td>${getScoreBadge(f1)}</td>
                <td class="text-secondary">${s}</td>
            </tr>
        `;
    });

    // Summary Rows
    const macro = report["macro avg"] || {};
    const weighted = report["weighted avg"] || {};

    rowsHtml += `
        <tr class="table-light fw-bold border-top">
            <td class="text-start"><i class="bi bi-calculator me-1 text-primary"></i>Macro Average</td>
            <td>${formatPercent(macro.precision)}</td>
            <td>${formatPercent(macro.recall)}</td>
            <td>${getScoreBadge(macro["f1-score"] || 0.6825)}</td>
            <td class="text-secondary">${macro.support || 82}</td>
        </tr>
        <tr class="table-primary fw-bold">
            <td class="text-start"><i class="bi bi-star-fill me-1 text-warning"></i>Weighted Average</td>
            <td>${formatPercent(weighted.precision)}</td>
            <td>${formatPercent(weighted.recall)}</td>
            <td>${getScoreBadge(weighted["f1-score"] || 0.6864)}</td>
            <td class="text-secondary">${weighted.support || 82}</td>
        </tr>
    `;

    tbody.innerHTML = rowsHtml;
}

function escapeHtml(text) {
    if (!text) return "";
    return text.replace(/[&<>"']/g, m => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    })[m]);
}
