/**
 * UniAssist AI - Profile & Password Settings Logic
 */

document.addEventListener("DOMContentLoaded", async () => {
    if (!API.requireAuth()) return;

    try {
        const user = await API.getProfile();
        if (user) {
            document.getElementById("profile-full-name").textContent = user.fullName;
            document.getElementById("profile-avatar").textContent = user.fullName.charAt(0);
            document.getElementById("profile-student-id").textContent = user.studentId || "N/A";
            document.getElementById("profile-email").textContent = user.email;
            document.getElementById("profile-department").textContent = user.department || "General";
            document.getElementById("profile-course").textContent = user.course || "General";
            document.getElementById("profile-semester").textContent = user.semester ? `Semester ${user.semester}` : "N/A";
            document.getElementById("profile-role-badge").textContent = user.role === "ROLE_ADMIN" ? "Administrator" : "Student";
        }
    } catch (err) {
        console.error("Failed to load profile:", err);
    }

    const passwordForm = document.getElementById("change-password-form");
    if (passwordForm) {
        passwordForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const currentPassword = document.getElementById("currentPassword").value;
            const newPassword = document.getElementById("newPassword").value;
            const confirmNewPassword = document.getElementById("confirmNewPassword").value;
            const submitBtn = document.getElementById("btn-save-password");

            if (newPassword.length < 6) {
                API.showToast("New password must be at least 6 characters.", "warning");
                return;
            }

            if (newPassword !== confirmNewPassword) {
                API.showToast("New passwords do not match.", "warning");
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = "Updating...";

            try {
                await API.changePassword(currentPassword, newPassword);
                API.showToast("Password updated successfully!", "success");
                passwordForm.reset();
            } catch (err) {
                API.showToast(err.message || "Failed to update password. Check current password.", "error");
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = "Update Password";
            }
        });
    }
});
