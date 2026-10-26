/**
 * UniAssist AI - Academic Hub Logic
 */

let allCourses = [];

document.addEventListener("DOMContentLoaded", async () => {
    // Check hash in URL (e.g. #calendar, #announcements)
    if (window.location.hash) {
        const hash = window.location.hash.replace("#", "");
        const triggerEl = document.querySelector(`button[data-bs-target="#tab-${hash}"]`);
        if (triggerEl) {
            const tab = new bootstrap.Tab(triggerEl);
            tab.show();
        }
    }

    // Load initial data
    await loadCourses();
    await loadCalendar();
    await loadAnnouncements();

    // Filter input for courses
    const filterInput = document.getElementById("course-filter-input");
    if (filterInput) {
        filterInput.addEventListener("input", (e) => {
            const query = e.target.value.toLowerCase().trim();
            renderCourses(allCourses.filter(c =>
                c.courseName.toLowerCase().includes(query) ||
                c.courseCode.toLowerCase().includes(query) ||
                c.department.toLowerCase().includes(query)
            ));
        });
    }
});

async function loadCourses() {
    const container = document.getElementById("courses-grid-container");
    try {
        allCourses = await API.getCourses();
        renderCourses(allCourses);
    } catch (err) {
        container.innerHTML = `<div class="text-danger small col-12 text-center">Failed to load courses.</div>`;
    }
}

function renderCourses(courses) {
    const container = document.getElementById("courses-grid-container");
    if (!courses || courses.length === 0) {
        container.innerHTML = `<div class="text-muted small col-12 text-center py-4">No degree programs matching your query.</div>`;
        return;
    }

    container.innerHTML = courses.map(c => `
        <div class="col-md-6 col-lg-4">
            <div class="uni-card h-100 d-flex flex-column justify-content-between">
                <div>
                    <div class="d-flex align-items-center justify-content-between mb-2">
                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle small">${c.courseCode}</span>
                        <span class="text-muted small">${c.credits} Credits</span>
                    </div>
                    <h6 class="fw-bold mb-1">${escapeHtml(c.courseName)}</h6>
                    <div class="text-muted small mb-2"><i class="bi bi-geo-alt me-1"></i>${escapeHtml(c.department)}</div>
                    <p class="text-secondary small mb-3">${escapeHtml(c.description || 'Comprehensive curriculum approved by the Academic Council.')}</p>
                </div>
                <div class="pt-2 border-top d-flex justify-content-between align-items-center">
                    <span class="small text-muted">${c.durationSemesters} Semesters</span>
                    <a href="chatbot.html?ask=What is the syllabus and curriculum for ${encodeURIComponent(c.courseName)}?" class="btn btn-outline-uni btn-sm">
                        <i class="bi bi-chat-dots me-1"></i> Ask AI
                    </a>
                </div>
            </div>
        </div>
    `).join("");
}

async function loadCalendar() {
    const tbody = document.getElementById("calendar-table-body");
    try {
        const events = await API.getCalendar();
        if (!events || events.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No academic events registered.</td></tr>`;
            return;
        }

        tbody.innerHTML = events.map(e => `
            <tr>
                <td class="fw-semibold">${escapeHtml(e.title)}</td>
                <td><span class="badge bg-light text-dark border small">${e.eventType}</span></td>
                <td>${e.semester || 'All'}</td>
                <td>${formatDate(e.startDate)}</td>
                <td>${formatDate(e.endDate)}</td>
                <td><span class="badge bg-success-subtle text-success small border border-success-subtle">Active</span></td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-3 text-danger">Failed to load calendar.</td></tr>`;
    }
}

async function loadAnnouncements() {
    const container = document.getElementById("full-announcements-list");
    try {
        const announcements = await API.getAnnouncements();
        if (!announcements || announcements.length === 0) {
            container.innerHTML = `<div class="text-muted small py-4 text-center">No active announcements.</div>`;
            return;
        }

        container.innerHTML = announcements.map(a => `
            <div class="p-3 border-bottom">
                <div class="d-flex align-items-center justify-content-between mb-1">
                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle small">${a.category}</span>
                    <span class="text-muted small">${new Date(a.publishedAt).toLocaleDateString("en-US", { year: 'numeric', month: 'short', day: 'numeric' })}</span>
                </div>
                <h6 class="fw-bold text-dark mb-1">${escapeHtml(a.title)}</h6>
                <p class="text-secondary small mb-0">${escapeHtml(a.description)}</p>
            </div>
        `).join("");
    } catch (err) {
        container.innerHTML = `<div class="text-danger small py-3 text-center">Failed to load announcements.</div>`;
    }
}

function formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    return d.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text || "";
    return div.innerHTML;
}
