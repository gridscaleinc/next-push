package com.gridscale.nextpush.repository;

import com.gridscale.nextpush.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    @Query("SELECT n FROM News n ORDER BY n.date DESC")
    Page<News> findAllOrderByDateDesc(Pageable pageable);
}