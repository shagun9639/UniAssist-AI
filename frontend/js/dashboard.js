/**
 * UniAssist AI - Student Dashboard Logic
 */

document.addEventListener("DOMContentLoaded", async () => {
    if (!API.requireAuth()) return;

    const user = API.getCurrentUser();
    if (user) {
        document.getElementById("dash-welcome-name").textContent = `Welcome Back, ${user.fullName}!`;
        document.getElementById("dash-student-id").textContent = user.studentId || "N/A";
        document.getElementById("dash-student-course").textContent = `${user.course || 'B.Tech'} (Sem ${user.semester || 1})`;
        document.getElementById("dash-student-dept").textContent = user.department || "General";
    }

    const dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    document.getElementById("dash-date").textContent = new Date().toLocaleDateString("en-US", dateOptions);

    // Load Metrics and Recent Data
    loadDashboardData();
});

async function loadDashboardData() {
    try {
        // Fetch conversations
        const conversations = await API.getConversations();
        document.getElementById("stat-conversations").textContent = conversations.length;
        renderRecentConversations(conversations);

        // Fetch student tickets
        const tickets = await API.getMyTickets();
        document.getElementById("stat-tickets").textContent = tickets.length;

        // Fetch active announcements
        const announcements = await API.getAnnouncements();
        renderAnnouncements(announcements);

        // Fetch calendar events
        const events = await API.getCalendar();
        renderCalendar(events);

    } catch (err) {
        console.error("Dashboard data load error:", err);
    }
}

function renderRecentConversations(conversations) {
    const container = document.getElementById("recent-conversations-list");
    if (!conversations || conversations.length === 0) {
        container.innerHTML = `
            <div class="text-center py-4 text-muted small">
                <i class="bi bi-chat-square-text display-6 d-block mb-2 text-muted" style="opacity:0.4;"></i>
                You haven't started any conversations yet.<br>
                <a href="chatbot.html" class="btn btn-primary-uni btn-sm mt-2">Start First Chat</a>
            </div>
        `;
        return;
    }

    const recent = conversations.slice(0, 5);
    container.innerHTML = recent.map(c => `
        <a href="chatbot.html?conv=${c.id}" class="list-group-item list-group-item-action border-0 px-0 py-2 d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2 text-truncate me-2">
                <i class="bi bi-chat-left-dots text-primary"></i>
                <span class="fw-semibold text-truncate small">${escapeHtml(c.title)}</span>
            </div>
            <div class="text-muted small text-nowrap" style="font-size:0.75rem;">
                <span class="badge bg-light text-secondary border me-1">${c.messageCount} turns</span>
                ${formatDate(c.updatedAt)}
            </div>
        </a>
    `).join("");
}

function renderAnnouncements(announcements) {
    const container = document.getElementById("dash-announcements-list");
    if (!announcements || announcements.length === 0) {
        container.innerHTML = `<div class="text-muted small py-2">No active announcements right now.</div>`;
        return;
    }

    const items = announcements.slice(0, 3);
    container.innerHTML = items.map(a => `
        <div class="p-2 border-bottom">
            <div class="d-flex align-items-center justify-content-between mb-1">
                <span class="badge bg-danger-subtle text-danger border border-danger-subtle small" style="font-size:0.7rem;">${a.category}</span>
                <span class="text-muted" style="font-size:0.75rem;">${formatDate(a.publishedAt)}</span>
            </div>
            <div class="fw-bold small text-dark">${escapeHtml(a.title)}</div>
            <div class="text-secondary small text-truncate" style="font-size:0.8rem;">${escapeHtml(a.description)}</div>
        </div>
    `).join("");
}

function renderCalendar(events) {
    const container = document.getElementById("dash-calendar-list");
    if (!events || events.length === 0) {
        container.innerHTML = `<div class="text-muted small py-2">No upcoming calendar events.</div>`;
        return;
    }

    const items = events.slice(0, 4);
    container.innerHTML = items.map(e => `
        <div class="d-flex align-items-start gap-2 py-2 border-bottom">
            <div class="bg-light border rounded text-center p-1" style="min-width: 48px;">
                <div class="fw-bold text-primary small" style="font-size: 0.7rem;">${getMonthName(e.startDate)}</div>
                <div class="fw-extrabold text-dark" style="font-size: 1rem; line-height: 1;">${getDayNumber(e.startDate)}</div>
            </div>
            <div>
                <div class="fw-semibold small text-dark">${escapeHtml(e.title)}</div>
                <div class="text-muted small" style="font-size: 0.75rem;">${e.eventType} • ${e.semester || 'All'}</div>
            </div>
        </div>
    `).join("");
}

function formatDate(isoStr) {
    if (!isoStr) return "";
    const date = new Date(isoStr);
    return date.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function getMonthName(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    return d.toLocaleString("en-US", { month: "short" }).toUpperCase();
}

function getDayNumber(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    return d.getDate();
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text || "";
    return div.innerHTML;
}
