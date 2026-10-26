/**
 * UniAssist AI - Administrative Console Logic
 * Coordinates statistics, Chart.js graphs, FAQ CRUD, ticket triage, and announcements.
 */

let intentChartInstance = null;
let ticketChartInstance = null;
let faqModalInstance = null;
let adminTicketModalInstance = null;
let announcementModalInstance = null;
let allAdminFaqs = [];

document.addEventListener("DOMContentLoaded", async () => {
    if (!API.requireAdmin()) return;

    faqModalInstance = new bootstrap.Modal(document.getElementById("faqModal"));
    adminTicketModalInstance = new bootstrap.Modal(document.getElementById("adminTicketModal"));
    announcementModalInstance = new bootstrap.Modal(document.getElementById("announcementModal"));

    await loadAdminDashboardData();
    await loadAdminFaqs();
    await loadAdminTickets("OPEN");
    await loadUnansweredQueries();
    await loadAdminAnnouncements();
    await loadAdminUsers();

    // FAQ Search & Category Filter Listeners
    document.getElementById("admin-faq-search").addEventListener("input", filterFaqs);
    document.getElementById("admin-faq-category").addEventListener("change", filterFaqs);

    // Ticket Status Filter
    document.getElementById("admin-ticket-filter").addEventListener("change", (e) => {
        loadAdminTickets(e.target.value);
    });

    // FAQ Form Submit Handler
    document.getElementById("faq-form").addEventListener("submit", handleSaveFaq);

    // Announcement Form Submit Handler
    document.getElementById("announcement-form").addEventListener("submit", handleSaveAnnouncement);

    // Admin Ticket Response & Status Update Handler
    document.getElementById("btn-admin-submit-ticket-response").addEventListener("click", handleSaveTicketStaffResponse);
});

async function loadAdminDashboardData() {
    try {
        const stats = await API.getAdminDashboard();

        document.getElementById("adm-stat-students").textContent = stats.totalStudents;
        document.getElementById("adm-stat-convs").textContent = stats.totalConversations;
        document.getElementById("adm-stat-msgs").textContent = stats.totalMessages;
        document.getElementById("adm-stat-tickets").textContent = stats.openTickets;
        document.getElementById("adm-stat-faqs").textContent = stats.totalFaqs;
        document.getElementById("adm-stat-unanswered").textContent = stats.unansweredQueries;

        renderIntentChart(stats.topIntents || []);
        renderTicketStatusChart(stats.ticketStatusBreakdown || {});

    } catch (err) {
        console.error("Failed to load admin stats:", err);
    }
}

function renderIntentChart(topIntents) {
    const ctx = document.getElementById("intentChart");
    if (!ctx) return;

    if (intentChartInstance) intentChartInstance.destroy();

    const labels = topIntents.length ? topIntents.map(i => i.intent) : ["attendance_policy", "exam_schedule", "fee_payment", "scholarships", "timetable"];
    const counts = topIntents.length ? topIntents.map(i => i.count) : [12, 9, 7, 5, 4];

    intentChartInstance = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: "Query Count",
                data: counts,
                backgroundColor: "rgba(37, 99, 235, 0.75)",
                borderColor: "rgba(37, 99, 235, 1)",
                borderWidth: 1,
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true, ticks: { precision: 0 } }
            }
        }
    });
}

function renderTicketStatusChart(breakdown) {
    const ctx = document.getElementById("ticketStatusChart");
    if (!ctx) return;

    if (ticketChartInstance) ticketChartInstance.destroy();

    ticketChartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Open", "In Progress", "Resolved", "Closed"],
            datasets: [{
                data: [
                    breakdown["OPEN"] || 1,
                    breakdown["IN_PROGRESS"] || 0,
                    breakdown["RESOLVED"] || 0,
                    breakdown["CLOSED"] || 0
                ],
                backgroundColor: ["#eab308", "#0284c7", "#16a34a", "#94a3b8"],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: "bottom" }
            }
        }
    });
}

// ----------------- FAQ Management -----------------
async function loadAdminFaqs() {
    const tbody = document.getElementById("admin-faqs-table-body");
    try {
        allAdminFaqs = await API.getFaqs();
        renderAdminFaqs(allAdminFaqs);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger small text-center">Failed to load FAQs.</td></tr>`;
    }
}

function renderAdminFaqs(faqs) {
    const tbody = document.getElementById("admin-faqs-table-body");
    if (!faqs || faqs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-muted small text-center py-4">No matching FAQ entries found.</td></tr>`;
        return;
    }

    tbody.innerHTML = faqs.map(f => `
        <tr>
            <td class="text-muted small">#${f.id}</td>
            <td class="fw-semibold small">${escapeHtml(f.question)}</td>
            <td class="text-secondary small text-truncate" style="max-width:320px;">${escapeHtml(f.answer)}</td>
            <td><span class="badge bg-light text-dark border small">${f.category}</span></td>
            <td><span class="badge ${f.active ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-muted'} small">${f.active ? 'Active' : 'Inactive'}</span></td>
            <td>
                <div class="d-flex gap-1">
                    <button class="btn btn-outline-primary btn-sm py-0 px-2" onclick="editFaq(${f.id})" title="Edit FAQ">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm py-0 px-2" onclick="deleteFaq(${f.id})" title="Delete FAQ">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join("");
}

function filterFaqs() {
    const query = document.getElementById("admin-faq-search").value.toLowerCase().trim();
    const cat = document.getElementById("admin-faq-category").value;

    const filtered = allAdminFaqs.filter(f => {
        const matchesQuery = !query || f.question.toLowerCase().includes(query) || f.answer.toLowerCase().includes(query) || (f.keywords && f.keywords.toLowerCase().includes(query));
        const matchesCat = !cat || f.category === cat;
        return matchesQuery && matchesCat;
    });

    renderAdminFaqs(filtered);
}

function prepareNewFaqModal() {
    document.getElementById("faqModalTitle").textContent = "Add New University FAQ";
    document.getElementById("faq-id").value = "";
    document.getElementById("faq-question").value = "";
    document.getElementById("faq-category-input").value = "academics";
    document.getElementById("faq-answer").value = "";
    document.getElementById("faq-keywords").value = "";
    document.getElementById("faq-source").value = "";
    document.getElementById("faq-active").checked = true;
}

function editFaq(faqId) {
    const faq = allAdminFaqs.find(f => f.id === faqId);
    if (!faq) return;

    document.getElementById("faqModalTitle").textContent = "Edit University FAQ #" + faq.id;
    document.getElementById("faq-id").value = faq.id;
    document.getElementById("faq-question").value = faq.question;
    document.getElementById("faq-category-input").value = faq.category;
    document.getElementById("faq-answer").value = faq.answer;
    document.getElementById("faq-keywords").value = faq.keywords || "";
    document.getElementById("faq-source").value = faq.sourceUrl || "";
    document.getElementById("faq-active").checked = faq.active;

    faqModalInstance.show();
}

async function handleSaveFaq(e) {
    e.preventDefault();
    const id = document.getElementById("faq-id").value;
    const question = document.getElementById("faq-question").value.trim();
    const category = document.getElementById("faq-category-input").value;
    const answer = document.getElementById("faq-answer").value.trim();
    const keywords = document.getElementById("faq-keywords").value.trim();
    const sourceUrl = document.getElementById("faq-source").value.trim();
    const active = document.getElementById("faq-active").checked;

    try {
        if (id) {
            await API.updateFaq(parseInt(id, 10), { question, category, answer, keywords, sourceUrl, active });
            API.showToast("FAQ updated successfully.", "success");
        } else {
            await API.createFaq({ question, category, answer, keywords, sourceUrl, active });
            API.showToast("New FAQ created.", "success");
        }
        faqModalInstance.hide();
        await loadAdminFaqs();
        await loadAdminDashboardData();
    } catch (err) {
        API.showToast(err.message || "Failed to save FAQ.", "error");
    }
}

async function deleteFaq(faqId) {
    if (!confirm("Are you sure you want to permanently delete this FAQ?")) return;
    try {
        await API.deleteFaq(faqId);
        API.showToast("FAQ deleted.", "info");
        await loadAdminFaqs();
        await loadAdminDashboardData();
    } catch (err) {
        API.showToast("Failed to delete FAQ.", "error");
    }
}

// ----------------- Ticket Management -----------------
async function loadAdminTickets(status) {
    const tbody = document.getElementById("admin-tickets-table-body");
    try {
        const tickets = await API.getAdminTickets(status);
        if (!tickets || tickets.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-muted small">No tickets matching status: ${status}</td></tr>`;
            return;
        }

        tbody.innerHTML = tickets.map(t => `
            <tr>
                <td class="text-muted small">#${t.id}</td>
                <td class="fw-semibold small">${escapeHtml(t.studentName)}<br><small class="text-muted">${escapeHtml(t.studentEmail)}</small></td>
                <td><span class="badge bg-light text-dark border small">${t.category}</span></td>
                <td class="small fw-semibold text-truncate" style="max-width:240px;">${escapeHtml(t.subject)}</td>
                <td><span class="badge ${getPriorityClass(t.priority)} small">${t.priority}</span></td>
                <td><span class="badge ${getStatusBadgeClass(t.status)} small">${t.status}</span></td>
                <td class="text-muted small">${new Date(t.createdAt).toLocaleDateString()}</td>
                <td>
                    <button class="btn btn-outline-primary btn-sm py-0 px-2" onclick="openAdminTicketModal(${t.id})">
                        Triage / Reply
                    </button>
                </td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-danger small text-center">Failed to load tickets.</td></tr>`;
    }
}

async function openAdminTicketModal(ticketId) {
    try {
        const ticket = await API.getTicketById(ticketId);
        document.getElementById("admin-ticket-id").value = ticket.id;
        document.getElementById("admin-ticket-student").textContent = `${ticket.studentName} (${ticket.studentEmail})`;
        document.getElementById("admin-ticket-status-badge").textContent = ticket.status;
        document.getElementById("admin-ticket-subject").textContent = ticket.subject;
        document.getElementById("admin-ticket-desc").textContent = ticket.description;
        document.getElementById("admin-update-ticket-status").value = ticket.status;
        document.getElementById("admin-update-ticket-assignee").value = ticket.assignedTo || "";
        document.getElementById("admin-ticket-reply").value = "";

        adminTicketModalInstance.show();
    } catch (err) {
        API.showToast("Could not load ticket details.", "error");
    }
}

async function handleSaveTicketStaffResponse() {
    const ticketId = document.getElementById("admin-ticket-id").value;
    const status = document.getElementById("admin-update-ticket-status").value;
    const assignee = document.getElementById("admin-update-ticket-assignee").value.trim();
    const replyText = document.getElementById("admin-ticket-reply").value.trim();

    try {
        await API.updateTicketStatus(parseInt(ticketId, 10), status, assignee);
        if (replyText) {
            await API.addStaffResponse(parseInt(ticketId, 10), replyText);
        }
        API.showToast("Ticket updated successfully.", "success");
        adminTicketModalInstance.hide();
        const currentFilter = document.getElementById("admin-ticket-filter").value;
        await loadAdminTickets(currentFilter);
        await loadAdminDashboardData();
    } catch (err) {
        API.showToast("Failed to update ticket.", "error");
    }
}

// ----------------- Unanswered Queries Review -----------------
async function loadUnansweredQueries() {
    const tbody = document.getElementById("admin-unanswered-table-body");
    try {
        const queries = await API.getUnansweredQueries();
        if (!queries || queries.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted small">No unanswered queries pending review.</td></tr>`;
            return;
        }

        tbody.innerHTML = queries.map(q => `
            <tr>
                <td class="text-muted small">#${q.id}</td>
                <td class="fw-semibold small">${escapeHtml(q.queryText)}</td>
                <td><span class="pill-intent">${q.predictedIntent || 'unrecognized'}</span></td>
                <td><span class="pill-score">${q.modelScore ? Math.round(q.modelScore * 100) + '%' : 'N/A'}</span></td>
                <td><span class="badge ${q.reviewStatus === 'UNREVIEWED' ? 'bg-warning-subtle text-dark border border-warning' : 'bg-success-subtle text-success'} small">${q.reviewStatus}</span></td>
                <td class="text-muted small">${new Date(q.createdAt).toLocaleDateString()}</td>
                <td>
                    <div class="d-flex gap-1">
                        <button class="btn btn-outline-success btn-sm py-0 px-2" onclick="resolveQuery(${q.id}, 'RESOLVED_AS_FAQ')" title="Resolved as FAQ">
                            <i class="bi bi-check-lg"></i>
                        </button>
                        <button class="btn btn-outline-secondary btn-sm py-0 px-2" onclick="resolveQuery(${q.id}, 'DISMISSED')" title="Dismiss query">
                            <i class="bi bi-x-lg"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger small text-center">Failed to load unanswered queries.</td></tr>`;
    }
}

async function resolveQuery(id, resolution) {
    try {
        await API.resolveUnansweredQuery(id, resolution);
        API.showToast("Query marked as " + resolution, "info");
        await loadUnansweredQueries();
        await loadAdminDashboardData();
    } catch (err) {
        API.showToast("Failed to update query status.", "error");
    }
}

// ----------------- Announcements -----------------
async function loadAdminAnnouncements() {
    const tbody = document.getElementById("admin-announcements-table-body");
    try {
        const announcements = await API.getAnnouncements();
        if (!announcements || announcements.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted small">No announcements posted.</td></tr>`;
            return;
        }

        tbody.innerHTML = announcements.map(a => `
            <tr>
                <td class="fw-semibold small">${escapeHtml(a.title)}</td>
                <td><span class="badge bg-light text-dark border small">${a.category}</span></td>
                <td class="text-muted small">${new Date(a.publishedAt).toLocaleDateString()}</td>
                <td class="text-muted small">${a.expiryDate || 'N/A'}</td>
                <td>
                    <button class="btn btn-outline-danger btn-sm py-0 px-2" onclick="deleteAnnouncement(${a.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-danger small text-center">Failed to load notices.</td></tr>`;
    }
}

async function handleSaveAnnouncement(e) {
    e.preventDefault();
    const title = document.getElementById("ann-title").value.trim();
    const category = document.getElementById("ann-category").value;
    const description = document.getElementById("ann-desc").value.trim();

    try {
        await API.createAnnouncement({ title, category, description });
        API.showToast("Announcement published.", "success");
        announcementModalInstance.hide();
        document.getElementById("announcement-form").reset();
        await loadAdminAnnouncements();
    } catch (err) {
        API.showToast("Failed to publish announcement.", "error");
    }
}

async function deleteAnnouncement(id) {
    if (!confirm("Delete this notice?")) return;
    try {
        await API.deleteAnnouncement(id);
        API.showToast("Announcement removed.", "info");
        await loadAdminAnnouncements();
    } catch (err) {
        API.showToast("Failed to delete announcement.", "error");
    }
}

// ----------------- Users Directory -----------------
async function loadAdminUsers() {
    const tbody = document.getElementById("admin-users-table-body");
    try {
        const users = await API.getAdminUsers();
        if (!users || users.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted small">No students registered yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = users.map(u => `
            <tr>
                <td class="fw-semibold small">${escapeHtml(u.studentId || 'N/A')}</td>
                <td class="small">${escapeHtml(u.fullName)}</td>
                <td class="small text-muted">${escapeHtml(u.email)}</td>
                <td class="small">${escapeHtml(u.department || 'N/A')}</td>
                <td class="small">${escapeHtml(u.course || 'N/A')}</td>
                <td class="small text-center">${u.semester || 1}</td>
                <td><span class="badge bg-success-subtle text-success border border-success-subtle small">${u.accountStatus}</span></td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-danger small text-center">Failed to load students.</td></tr>`;
    }
}

function getPriorityClass(p) {
    switch (p) {
        case "URGENT": return "bg-danger text-white";
        case "HIGH": return "bg-warning text-dark";
        case "MEDIUM": return "bg-info text-white";
        default: return "bg-light text-muted border";
    }
}

function getStatusBadgeClass(status) {
    switch (status) {
        case "OPEN": return "bg-warning text-dark";
        case "IN_PROGRESS": return "bg-info text-white";
        case "RESOLVED": return "bg-success text-white";
        case "CLOSED": return "bg-secondary text-white";
        default: return "bg-light text-muted border";
    }
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text || "";
    return div.innerHTML;
}
