package com.uniassist.service;

import com.uniassist.dto.AuthDtos.*;
import com.uniassist.entity.User;
import com.uniassist.exception.BadRequestException;
import com.uniassist.exception.ResourceNotFoundException;
import com.uniassist.repository.UserRepository;
import com.uniassist.security.JwtTokenProvider;
import com.uniassist.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("An account with email " + req.getEmail() + " already exists.");
        }
        if (userRepository.existsByStudentId(req.getStudentId())) {
            throw new BadRequestException("Student enrollment number " + req.getStudentId() + " is already registered.");
        }

        User user = new User(
                req.getFullName(),
                req.getStudentId(),
                req.getEmail(),
                req.getMobileNumber(),
                req.getDepartment(),
                req.getCourse(),
                req.getSemester(),
                passwordEncoder.encode(req.getPassword()),
                "ROLE_STUDENT"
        );

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getStudentId(),
                savedUser.getRole(),
                savedUser.getDepartment(),
                savedUser.getCourse(),
                savedUser.getSemester()
        );
    }

    public AuthResponse login(LoginRequest req) {
        // Find user to resolve whether username is email or studentId
        User user = userRepository.findByEmail(req.getUsername())
                .or(() -> userRepository.findByStudentId(req.getUsername()))
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStudentId(),
                user.getRole(),
                user.getDepartment(),
                user.getCourse(),
                user.getSemester()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getStudentId(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getDepartment(),
                user.getCourse(),
                user.getSemester(),
                user.getRole(),
                user.getAccountStatus()
        );
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match.");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}
