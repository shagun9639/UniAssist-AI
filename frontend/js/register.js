/**
 * UniAssist AI - Student Registration Logic
 */

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("register-form");
    const alertBox = document.getElementById("register-alert");
    const submitBtn = document.getElementById("register-submit-btn");

    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        alertBox.classList.add("d-none");

        const fullName = document.getElementById("fullName").value.trim();
        const studentId = document.getElementById("studentId").value.trim();
        const email = document.getElementById("email").value.trim();
        const mobileNumber = document.getElementById("mobileNumber").value.trim();
        const department = document.getElementById("department").value;
        const course = document.getElementById("course").value;
        const semester = parseInt(document.getElementById("semester").value, 10);
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;
        const termsCheck = document.getElementById("termsCheck").checked;

        // Validations
        if (!fullName || !studentId || !email || !department || !course || !password) {
            showAlert("Please fill in all mandatory fields marked with an asterisk (*).", "danger");
            return;
        }

        if (password.length < 6) {
            showAlert("Password must be at least 6 characters long.", "danger");
            return;
        }

        if (password !== confirmPassword) {
            showAlert("Passwords do not match. Please re-enter carefully.", "danger");
            return;
        }

        if (!termsCheck) {
            showAlert("You must agree to the university IT usage policy.", "warning");
            return;
        }

        submitBtn.disabled = true;
        submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span> Creating account...`;

        try {
            const authData = await API.register({
                fullName,
                studentId,
                email,
                mobileNumber,
                department,
                course,
                semester,
                password
            });

            API.setSession(authData);
            API.showToast("Registration successful! Welcome to UniAssist AI.", "success");

            setTimeout(() => {
                window.location.href = "dashboard.html";
            }, 800);

        } catch (err) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = `<span>Create Student Account</span> <i class="bi bi-check-circle-fill"></i>`;
            showAlert(err.message || "Registration failed. Please check your inputs.", "danger");
        }
    });

    function showAlert(msg, type) {
        alertBox.textContent = msg;
        alertBox.className = `alert alert-${type} small mb-3`;
        alertBox.classList.remove("d-none");
    }
});
