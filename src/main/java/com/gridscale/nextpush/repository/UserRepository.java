package com.gridscale.nextpush.repository;

import com.gridscale.nextpush.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find users by user type
    List<User> findByUserType(String userType);
    
    // Find active users only
    @Query("SELECT u FROM User u WHERE u.activeFlag = true")
    List<User> findActiveUsers();
    
    // Find users by active flag
    List<User> findByActiveFlag(Boolean activeFlag);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Find users by user name containing (case insensitive search)
    @Query("SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByUserNameContainingIgnoreCase(@Param("name") String name);
}