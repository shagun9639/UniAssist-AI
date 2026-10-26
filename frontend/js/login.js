/**
 * UniAssist AI - Login Logic
 */

document.addEventListener("DOMContentLoaded", () => {
    // If already authenticated, redirect
    if (API.isAuthenticated()) {
        const user = API.getCurrentUser();
        if (user && user.role === "ROLE_ADMIN") {
            window.location.href = "admin.html";
        } else {
            window.location.href = "dashboard.html";
        }
        return;
    }

    const form = document.getElementById("login-form");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const togglePasswordBtn = document.getElementById("toggle-password");
    const alertBox = document.getElementById("login-alert");
    const submitBtn = document.getElementById("login-submit-btn");

    // Show/Hide password toggle
    if (togglePasswordBtn) {
        togglePasswordBtn.addEventListener("click", () => {
            const isPassword = passwordInput.type === "password";
            passwordInput.type = isPassword ? "text" : "password";
            togglePasswordBtn.innerHTML = isPassword ? '<i class="bi bi-eye-slash"></i>' : '<i class="bi bi-eye"></i>';
        });
    }

    // Quick Demo Autofill Buttons
    const fillStudentBtn = document.getElementById("fill-student-demo");
    if (fillStudentBtn) {
        fillStudentBtn.addEventListener("click", () => {
            usernameInput.value = "student@uniassist.edu";
            passwordInput.value = "Student@123";
            alertBox.classList.add("d-none");
        });
    }

    const fillAdminBtn = document.getElementById("fill-admin-demo");
    if (fillAdminBtn) {
        fillAdminBtn.addEventListener("click", () => {
            usernameInput.value = "admin@uniassist.edu";
            passwordInput.value = "Admin@123";
            alertBox.classList.add("d-none");
        });
    }

    // Submit handler
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        alertBox.classList.add("d-none");
        alertBox.className = "alert d-none small";

        const username = usernameInput.value.trim();
        const password = passwordInput.value;

        if (!username || !password) {
            showAlert("Please enter your university email and password.", "danger");
            return;
        }

        submitBtn.disabled = true;
        submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span> Authenticating...`;

        try {
            const authData = await API.login(username, password);
            API.setSession(authData);
            API.showToast("Authentication successful! Welcome to UniAssist.", "success");

            setTimeout(() => {
                if (authData.role === "ROLE_ADMIN") {
                    window.location.href = "admin.html";
                } else {
                    window.location.href = "dashboard.html";
                }
            }, 600);

        } catch (err) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<span>Sign In</span> <i class="bi bi-arrow-right"></i>`;
            showAlert(err.message || "Invalid email, student ID, or password.", "danger");
        }
    });

    function showAlert(msg, type) {
        alertBox.textContent = msg;
        alertBox.className = `alert alert-${type} small mb-3`;
        alertBox.classList.remove("d-none");
    }
});
