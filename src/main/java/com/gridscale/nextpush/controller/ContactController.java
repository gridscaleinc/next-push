package com.gridscale.nextpush.controller;

import com.gridscale.nextpush.entity.Contact;
import com.gridscale.nextpush.entity.User;
import com.gridscale.nextpush.repository.ContactRepository;
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
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private boolean canDeleteContact(Authentication authentication) {
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
    public ResponseEntity<Page<Contact>> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Contact> contactsPage = contactRepository.findAllOrderByCreatedAtDesc(pageable);
        
        return ResponseEntity.ok(contactsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        return contactRepository.findById(id)
                .map(contact -> ResponseEntity.ok().body(contact))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id, Authentication authentication) {
        if (!canDeleteContact(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return contactRepository.findById(id)
                .map(contact -> {
                    contactRepository.delete(contact);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}