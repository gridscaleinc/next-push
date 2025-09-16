package com.gridscale.nextpush.controller;

import com.gridscale.nextpush.entity.Entry;
import com.gridscale.nextpush.entity.User;
import com.gridscale.nextpush.repository.EntryRepository;
import com.gridscale.nextpush.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/entries")
@CrossOrigin(origins = "*")
public class EntryController {

    @Autowired
    private EntryRepository entryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private boolean canDeleteEntry(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        
        String currentUserEmail = authentication.getName();
        Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
        
        if (currentUserOpt.isEmpty()) {
            return false;
        }
        
        User currentUser = currentUserOpt.get();
        return "admin".equals(currentUser.getUserType()) || "editor".equals(currentUser.getUserType());
    }

    @GetMapping
    public ResponseEntity<Page<Entry>> getAllEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Entry> entriesPage = entryRepository.findAllOrderByCreatedAtDesc(pageable);
        
        return ResponseEntity.ok(entriesPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entry> getEntryById(@PathVariable Long id) {
        return entryRepository.findById(id)
                .map(entry -> ResponseEntity.ok().body(entry))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id, Authentication authentication) {
        if (!canDeleteEntry(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return entryRepository.findById(id)
                .map(entry -> {
                    entryRepository.delete(entry);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}