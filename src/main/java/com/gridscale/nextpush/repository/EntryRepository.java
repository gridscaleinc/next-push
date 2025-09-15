package com.gridscale.nextpush.repository;

import com.gridscale.nextpush.entity.Entry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    
    @Query("SELECT e FROM Entry e ORDER BY e.createdAt DESC")
    Page<Entry> findAllOrderByCreatedAtDesc(Pageable pageable);
}