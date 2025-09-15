package com.gridscale.nextpush.controller;

import com.gridscale.nextpush.dto.LoginRequest;
import com.gridscale.nextpush.dto.LoginResponse;
import com.gridscale.nextpush.entity.User;
import com.gridscale.nextpush.repository.UserRepository;
import com.gridscale.nextpush.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Validate input
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new LoginResponse("メールアドレスは必須です"));
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new LoginResponse("パスワードは必須です"));
            }

            // Find user by email
            Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getEmail().trim());
            
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("メールアドレスまたはパスワードが正しくありません"));
            }

            User user = optionalUser.get();

            // Check if user is active
            if (!user.getActiveFlag()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("アカウントが無効になっています"));
            }

            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("メールアドレスまたはパスワードが正しくありません"));
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            // Create successful response
            LoginResponse response = new LoginResponse(
                token,
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getUserType()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log error (in production, use proper logging)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse("ログイン処理中にエラーが発生しました"));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<LoginResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("認証トークンが無効です"));
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            
            if (!jwtUtil.isValidToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("認証トークンが無効です"));
            }

            String email = jwtUtil.extractUsername(token);
            Optional<User> optionalUser = userRepository.findByEmail(email);
            
            if (optionalUser.isEmpty() || !optionalUser.get().getActiveFlag()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("ユーザーが見つからないか無効です"));
            }

            User user = optionalUser.get();
            LoginResponse response = new LoginResponse(
                token,
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getUserType()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse("トークン検証中にエラーが発生しました"));
        }
    }
}