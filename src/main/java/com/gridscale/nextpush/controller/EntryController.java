package com.gridscale.nextpush.controller;

import com.gridscale.nextpush.entity.Entry;
import com.gridscale.nextpush.repository.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entries")
@CrossOrigin(origins = "*")
public class EntryController {

    @Autowired
    private EntryRepository entryRepository;

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
}