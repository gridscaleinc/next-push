package com.gridscale.nextpush.controller;

import com.gridscale.nextpush.entity.User;
import com.gridscale.nextpush.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        // Check if the authenticated user has admin privileges
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String currentUserEmail = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
        
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = currentUserOpt.get();
        if (!"admin".equals(currentUser.getUserType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // Get users by user type
    @GetMapping("/type/{userType}")
    public ResponseEntity<List<User>> getUsersByType(@PathVariable String userType) {
        List<User> users = userRepository.findByUserType(userType);
        return ResponseEntity.ok(users);
    }

    // Get active users only
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> users = userRepository.findActiveUsers();
        return ResponseEntity.ok(users);
    }

    // Search users by name
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String name) {
        List<User> users = userRepository.findByUserNameContainingIgnoreCase(name);
        return ResponseEntity.ok(users);
    }

    // Create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Check if the authenticated user has admin privileges
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String currentUserEmail = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
        
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = currentUserOpt.get();
        if (!"admin".equals(currentUser.getUserType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate required fields
        if (user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (user.getUserType() == null || user.getUserType().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (user.getActiveFlag() == null) {
            user.setActiveFlag(true); // Default to active
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        // Check if the authenticated user has admin privileges
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String currentUserEmail = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
        
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User currentUser = currentUserOpt.get();
        if (!"admin".equals(currentUser.getUserType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return userRepository.findById(id)
                .map(user -> {
                    if (userDetails.getUserName() != null) {
                        user.setUserName(userDetails.getUserName());
                    }
                    if (userDetails.getEmail() != null) {
                        // Check if email is being changed and if new email already exists
                        if (!user.getEmail().equals(userDetails.getEmail()) && 
                            userRepository.existsByEmail(userDetails.getEmail())) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).<User>build();
                        }
                        user.setEmail(userDetails.getEmail());
                    }
                    if (userDetails.getUserType() != null) {
                        user.setUserType(userDetails.getUserType());
                    }
                    if (userDetails.getActiveFlag() != null) {
                        user.setActiveFlag(userDetails.getActiveFlag());
                    }
                    
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Deactivate user (soft delete)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<User> deactivateUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActiveFlag(false);
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Activate user
    @PatchMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActiveFlag(true);
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}