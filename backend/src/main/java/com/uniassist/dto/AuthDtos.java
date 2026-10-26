package com.uniassist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static class LoginRequest {
        @NotBlank(message = "Email or Student ID is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        private Boolean rememberMe = false;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public Boolean getRememberMe() { return rememberMe; }
        public void setRememberMe(Boolean rememberMe) { this.rememberMe = rememberMe; }
    }

    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 150)
        private String fullName;

        @NotBlank(message = "Student enrollment number is required")
        private String studentId;

        @NotBlank(message = "University email is required")
        @Email(message = "Must be a valid email format")
        private String email;

        private String mobileNumber;

        @NotBlank(message = "Department is required")
        private String department;

        @NotBlank(message = "Course is required")
        private String course;

        private Integer semester = 1;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getCourse() { return course; }
        public void setCourse(String course) { this.course = course; }

        public Integer getSemester() { return semester; }
        public void setSemester(Integer semester) { this.semester = semester; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        private String tokenType = "Bearer";
        private Long userId;
        private String fullName;
        private String email;
        private String studentId;
        private String role;
        private String department;
        private String course;
        private Integer semester;

        public AuthResponse(String token, Long userId, String fullName, String email,
                            String studentId, String role, String department, String course, Integer semester) {
            this.token = token;
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.studentId = studentId;
            this.role = role;
            this.department = department;
            this.course = course;
            this.semester = semester;
        }

        public String getToken() { return token; }
        public String getTokenType() { return tokenType; }
        public Long getUserId() { return userId; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getStudentId() { return studentId; }
        public String getRole() { return role; }
        public String getDepartment() { return department; }
        public String getCourse() { return course; }
        public Integer getSemester() { return semester; }
    }

    public static class PasswordChangeRequest {
        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class UserProfileDto {
        private Long id;
        private String fullName;
        private String studentId;
        private String email;
        private String mobileNumber;
        private String department;
        private String course;
        private Integer semester;
        private String role;
        private String accountStatus;

        public UserProfileDto() {}

        public UserProfileDto(Long id, String fullName, String studentId, String email,
                              String mobileNumber, String department, String course,
                              Integer semester, String role, String accountStatus) {
            this.id = id;
            this.fullName = fullName;
            this.studentId = studentId;
            this.email = email;
            this.mobileNumber = mobileNumber;
            this.department = department;
            this.course = course;
            this.semester = semester;
            this.role = role;
            this.accountStatus = accountStatus;
        }

        public Long getId() { return id; }
        public String getFullName() { return fullName; }
        public String getStudentId() { return studentId; }
        public String getEmail() { return email; }
        public String getMobileNumber() { return mobileNumber; }
        public String getDepartment() { return department; }
        public String getCourse() { return course; }
        public Integer getSemester() { return semester; }
        public String getRole() { return role; }
        public String getAccountStatus() { return accountStatus; }
    }
}
