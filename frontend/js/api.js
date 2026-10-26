/**
 * UniAssist AI - Centralized Client API Module
 * Manages JWT tokens, backend communication, error handling, and toast notifications.
 */

const API_BASE_URL = window.location.origin.includes(":8080") 
    ? window.location.origin + "/api" 
    : "http://localhost:8080/api";

const AI_SERVICE_BASE_URL = "http://localhost:8000";

const API = {
    // Authentication Token Management
    getToken() {
        return localStorage.getItem("uniassist_token");
    },

    setSession(authData) {
        localStorage.setItem("uniassist_token", authData.token);
        localStorage.setItem("uniassist_user", JSON.stringify({
            userId: authData.userId,
            fullName: authData.fullName,
            email: authData.email,
            studentId: authData.studentId,
            role: authData.role,
            department: authData.department,
            course: authData.course,
            semester: authData.semester
        }));
    },

    getCurrentUser() {
        const userStr = localStorage.getItem("uniassist_user");
        return userStr ? JSON.parse(userStr) : null;
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    isAdmin() {
        const user = this.getCurrentUser();
        return user && user.role === "ROLE_ADMIN";
    },

    logout() {
        localStorage.removeItem("uniassist_token");
        localStorage.removeItem("uniassist_user");
        window.location.href = "login.html";
    },

    requireAuth() {
        if (!this.isAuthenticated()) {
            window.location.href = "login.html";
            return false;
        }
        return true;
    },

    requireAdmin() {
        if (!this.isAuthenticated() || !this.isAdmin()) {
            window.location.href = "login.html";
            return false;
        }
        return true;
    },

    // HTTP Request Handler
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            "Content-Type": "application/json",
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, { ...options, headers });

            if (response.status === 401) {
                // Token expired or invalid
                this.logout();
                throw new Error("Session expired. Please log in again.");
            }

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                const errorMsg = data.message || data.error || `HTTP error ${response.status}`;
                throw new Error(errorMsg);
            }

            return data;
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    },

    // Auth Endpoints
    async login(username, password) {
        return this.request("/auth/login", {
            method: "POST",
            body: JSON.stringify({ username, password })
        });
    },

    async register(userData) {
        return this.request("/auth/register", {
            method: "POST",
            body: JSON.stringify(userData)
        });
    },

    async getProfile() {
        return this.request("/auth/me");
    },

    async changePassword(currentPassword, newPassword) {
        return this.request("/auth/change-password", {
            method: "POST",
            body: JSON.stringify({ currentPassword, newPassword })
        });
    },

    // Chatbot Endpoints
    async sendMessage(message, conversationId = null) {
        return this.request("/chat", {
            method: "POST",
            body: JSON.stringify({ message, conversationId })
        });
    },

    async getConversations() {
        return this.request("/conversations");
    },

    async getConversationMessages(id) {
        return this.request(`/conversations/${id}`);
    },

    async deleteConversation(id) {
        return this.request(`/conversations/${id}`, { method: "DELETE" });
    },

    async sendFeedback(messageId, rating, comments = "") {
        return this.request("/chat/feedback", {
            method: "POST",
            body: JSON.stringify({ messageId, rating, comments })
        });
    },

    // FAQs & Knowledge Endpoints
    async getFaqs(category = "") {
        const query = category ? `?category=${encodeURIComponent(category)}` : "";
        return this.request(`/faqs${query}`);
    },

    async searchFaqs(query = "") {
        return this.request(`/faqs/search?query=${encodeURIComponent(query)}`);
    },

    // Academic Endpoints
    async getCourses() {
        return this.request("/academic/courses");
    },

    async getCalendar() {
        return this.request("/academic/calendar");
    },

    async getAnnouncements() {
        return this.request("/announcements");
    },

    // Support Tickets Endpoints
    async getMyTickets() {
        return this.request("/tickets/my");
    },

    async createTicket(ticketData) {
        return this.request("/tickets", {
            method: "POST",
            body: JSON.stringify(ticketData)
        });
    },

    async getTicketById(id) {
        return this.request(`/tickets/${id}`);
    },

    async addTicketResponse(id, responseText) {
        return this.request(`/tickets/${id}/responses`, {
            method: "POST",
            body: JSON.stringify({ responseText })
        });
    },

    // Admin Endpoints
    async getAdminDashboard() {
        return this.request("/admin/dashboard");
    },

    async getAdminTickets(status = "") {
        const query = status ? `?status=${encodeURIComponent(status)}` : "";
        return this.request(`/admin/tickets${query}`);
    },

    async updateTicketStatus(id, status, assignedTo = null) {
        return this.request(`/admin/tickets/${id}/status`, {
            method: "PUT",
            body: JSON.stringify({ status, assignedTo })
        });
    },

    async addStaffResponse(id, responseText) {
        return this.request(`/admin/tickets/${id}/responses`, {
            method: "POST",
            body: JSON.stringify({ responseText })
        });
    },

    async createFaq(faq) {
        return this.request("/admin/faqs", {
            method: "POST",
            body: JSON.stringify(faq)
        });
    },

    async updateFaq(id, faq) {
        return this.request(`/admin/faqs/${id}`, {
            method: "PUT",
            body: JSON.stringify(faq)
        });
    },

    async deleteFaq(id) {
        return this.request(`/admin/faqs/${id}`, { method: "DELETE" });
    },

    async createAnnouncement(announcement) {
        return this.request("/admin/announcements", {
            method: "POST",
            body: JSON.stringify(announcement)
        });
    },

    async updateAnnouncement(id, announcement) {
        return this.request(`/admin/announcements/${id}`, {
            method: "PUT",
            body: JSON.stringify(announcement)
        });
    },

    async deleteAnnouncement(id) {
        return this.request(`/admin/announcements/${id}`, { method: "DELETE" });
    },

    async getUnansweredQueries() {
        return this.request("/admin/unanswered-queries");
    },

    async resolveUnansweredQuery(id, status) {
        return this.request(`/admin/unanswered-queries/${id}`, {
            method: "PUT",
            body: JSON.stringify({ status })
        });
    },

    async getAdminUsers() {
        return this.request("/admin/users");
    },

    async getModelInfo() {
        return this.request("/admin/model-info");
    },

    async getModelMetrics() {
        return this.request("/admin/model-metrics");
    },

    async getModelComparison() {
        return this.request("/admin/model-comparison");
    },

    // Toast Notification System
    showToast(message, type = "info") {
        let container = document.querySelector(".toast-container-fixed");
        if (!container) {
            container = document.createElement("div");
            container.className = "toast-container-fixed";
            document.body.appendChild(container);
        }

        const toast = document.createElement("div");
        const bgClass = type === "success" ? "bg-success text-white" :
                        type === "error" ? "bg-danger text-white" :
                        type === "warning" ? "bg-warning text-dark" : "bg-primary text-white";

        toast.className = `toast align-items-center ${bgClass} border-0 show p-2 shadow-lg`;
        toast.style.borderRadius = "8px";
        toast.style.minWidth = "260px";
        toast.style.animation = "fadeIn 0.3s";
        toast.innerHTML = `
            <div class="d-flex align-items-center justify-content-between">
                <div class="toast-body py-1 px-2 d-flex align-items-center gap-2">
                    <i class="bi ${type === 'success' ? 'bi-check-circle-fill' : type === 'error' ? 'bi-exclamation-triangle-fill' : 'bi-info-circle-fill'}"></i>
                    <span>${message}</span>
                </div>
                <button type="button" class="btn-close ${type !== 'warning' ? 'btn-close-white' : ''} me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;

        container.appendChild(toast);
        setTimeout(() => {
            toast.remove();
        }, 4000);
    }
};

// Global helper to render dynamic user navbar state
function updateNavbarAuth() {
    const user = API.getCurrentUser();
    const navAuth = document.getElementById("navbar-auth-section");
    if (!navAuth) return;

    if (user) {
        navAuth.innerHTML = `
            <div class="dropdown">
                <button class="btn btn-outline-uni dropdown-toggle d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                    <span class="chat-avatar student-av" style="width:28px;height:28px;font-size:0.75rem;">${user.fullName.charAt(0)}</span>
                    <span class="d-none d-sm-inline fw-semibold">${user.fullName.split(' ')[0]}</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2">
                    <li><h6 class="dropdown-header">${user.role === 'ROLE_ADMIN' ? 'Administrator' : 'Student'}</h6></li>
                    ${user.role === 'ROLE_ADMIN' ? '<li><a class="dropdown-item" href="admin.html"><i class="bi bi-speedometer2 me-2"></i>Admin Dashboard</a></li>' : ''}
                    <li><a class="dropdown-item" href="dashboard.html"><i class="bi bi-grid me-2"></i>Student Dashboard</a></li>
                    <li><a class="dropdown-item" href="profile.html"><i class="bi bi-person me-2"></i>Profile & Settings</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="javascript:void(0)" onclick="API.logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
                </ul>
            </div>
        `;
    } else {
        navAuth.innerHTML = `
            <a href="login.html" class="btn btn-outline-uni me-2">Sign In</a>
            <a href="register.html" class="btn btn-primary-uni">Get Started</a>
        `;
    }
}

document.addEventListener("DOMContentLoaded", updateNavbarAuth);
