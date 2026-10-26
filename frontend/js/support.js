/**
 * UniAssist AI - Support Ticket System Client Logic
 */

let activeTicketId = null;
let newTicketModalInstance = null;

document.addEventListener("DOMContentLoaded", async () => {
    if (!API.requireAuth()) return;

    newTicketModalInstance = new bootstrap.Modal(document.getElementById("newTicketModal"));

    await loadMyTickets();

    // Create ticket form handler
    const createForm = document.getElementById("create-ticket-form");
    if (createForm) {
        createForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const submitBtn = document.getElementById("btn-submit-ticket");

            const category = document.getElementById("ticketCategory").value;
            const priority = document.getElementById("ticketPriority").value;
            const subject = document.getElementById("ticketSubject").value.trim();
            const description = document.getElementById("ticketDescription").value.trim();

            if (!category || !subject || !description) {
                API.showToast("Please fill all required ticket fields.", "warning");
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = "Submitting...";

            try {
                const newTicket = await API.createTicket({ category, priority, subject, description });
                API.showToast("Ticket submitted successfully! Assigned ID #" + newTicket.id, "success");
                createForm.reset();
                newTicketModalInstance.hide();
                await loadMyTickets();
                selectTicket(newTicket.id);
            } catch (err) {
                API.showToast(err.message || "Failed to submit ticket.", "error");
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = "Submit Ticket";
            }
        });
    }

    // Reply form handler
    const replyForm = document.getElementById("ticket-reply-form");
    if (replyForm) {
        replyForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const replyInput = document.getElementById("ticket-reply-text");
            const responseText = replyInput.value.trim();
            const replyBtn = document.getElementById("btn-reply-ticket");

            if (!responseText || !activeTicketId) return;

            replyBtn.disabled = true;
            replyBtn.textContent = "Posting...";

            try {
                await API.addTicketResponse(activeTicketId, responseText);
                API.showToast("Follow-up reply added.", "success");
                replyInput.value = "";
                await selectTicket(activeTicketId);
            } catch (err) {
                API.showToast("Failed to post response.", "error");
            } finally {
                replyBtn.disabled = false;
                replyBtn.textContent = "Post Reply";
            }
        });
    }
});

async function loadMyTickets() {
    const listContainer = document.getElementById("tickets-list-container");
    const countBadge = document.getElementById("ticket-count-badge");

    try {
        const tickets = await API.getMyTickets();
        if (countBadge) countBadge.textContent = `${tickets.length} tickets`;

        if (!tickets || tickets.length === 0) {
            listContainer.innerHTML = `
                <div class="text-center py-5 text-muted small">
                    <i class="bi bi-inbox fs-2 d-block mb-2 text-muted" style="opacity:0.4;"></i>
                    No tickets submitted yet.<br>Click "Create New Ticket" to submit an inquiry.
                </div>
            `;
            return;
        }

        listContainer.innerHTML = tickets.map(t => `
            <a href="javascript:void(0)" onclick="selectTicket(${t.id})" class="list-group-item list-group-item-action border-0 px-3 py-3 border-bottom ticket-item ${activeTicketId === t.id ? 'active' : ''}" data-ticket-id="${t.id}">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <span class="badge ${getStatusBadgeClass(t.status)} small">${t.status}</span>
                    <span class="text-muted" style="font-size: 0.75rem;">${formatDate(t.createdAt)}</span>
                </div>
                <div class="fw-bold small text-truncate text-dark">${escapeHtml(t.subject)}</div>
                <div class="text-muted small" style="font-size:0.75rem;">Category: ${t.category} • Replies: ${t.responses ? t.responses.length : 0}</div>
            </a>
        `).join("");

        // If ticket active, keep selected
        if (activeTicketId) {
            selectTicket(activeTicketId);
        }

    } catch (err) {
        listContainer.innerHTML = `<div class="text-danger small py-3 text-center">Failed to load tickets.</div>`;
    }
}

async function selectTicket(ticketId) {
    activeTicketId = ticketId;

    // Highlight active in list
    document.querySelectorAll(".ticket-item").forEach(item => {
        if (parseInt(item.getAttribute("data-ticket-id"), 10) === ticketId) {
            item.classList.add("bg-light");
        } else {
            item.classList.remove("bg-light");
        }
    });

    const noTicketCard = document.getElementById("no-ticket-selected");
    const contentCard = document.getElementById("ticket-selected-content");

    try {
        const ticket = await API.getTicketById(ticketId);

        noTicketCard.classList.add("d-none");
        contentCard.classList.remove("d-none");

        document.getElementById("detail-id").textContent = `#${ticket.id}`;
        document.getElementById("detail-category").textContent = ticket.category;
        document.getElementById("detail-subject").textContent = ticket.subject;
        document.getElementById("detail-date").textContent = new Date(ticket.createdAt).toLocaleString("en-US", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
        document.getElementById("detail-priority").textContent = ticket.priority;

        const statusEl = document.getElementById("detail-status");
        statusEl.textContent = ticket.status;
        statusEl.className = `badge ${getStatusBadgeClass(ticket.status)} px-3 py-1 rounded-pill`;

        document.getElementById("detail-description").textContent = ticket.description;

        // Render response thread
        const threadContainer = document.getElementById("ticket-responses-thread");
        if (!ticket.responses || ticket.responses.length === 0) {
            threadContainer.innerHTML = `
                <div class="text-center py-3 text-muted small bg-white border rounded">
                    Staff have not responded to this ticket yet. Responses will appear here within 24-48 hours.
                </div>
            `;
        } else {
            threadContainer.innerHTML = ticket.responses.map(r => `
                <div class="p-3 rounded border bg-white shadow-sm">
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="fw-bold small text-primary d-flex align-items-center gap-1">
                            <i class="bi bi-person-circle"></i> ${escapeHtml(r.responderName)}
                        </span>
                        <span class="text-muted" style="font-size:0.75rem;">${new Date(r.createdAt).toLocaleString("en-US", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" })}</span>
                    </div>
                    <div class="small text-secondary">${escapeHtml(r.responseText)}</div>
                </div>
            `).join("");
        }

    } catch (err) {
        API.showToast("Could not retrieve ticket details.", "error");
    }
}

function getStatusBadgeClass(status) {
    switch (status) {
        case "OPEN": return "bg-warning-subtle text-warning border border-warning-subtle";
        case "IN_PROGRESS": return "bg-info-subtle text-info border border-info-subtle";
        case "RESOLVED": return "bg-success-subtle text-success border border-success-subtle";
        case "CLOSED": return "bg-secondary-subtle text-secondary border border-secondary-subtle";
        default: return "bg-light text-dark border";
    }
}

function formatDate(isoStr) {
    if (!isoStr) return "";
    const d = new Date(isoStr);
    return d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function escapeHtml(text) {
    const div = document.createElement("div");
    div.textContent = text || "";
    return div.innerHTML;
}
