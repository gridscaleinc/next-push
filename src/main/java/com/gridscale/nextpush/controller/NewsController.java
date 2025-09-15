package com.gridscale.nextpush.controller;

import com.gridscale.nextpush.entity.News;
import com.gridscale.nextpush.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    @GetMapping
    public ResponseEntity<Page<News>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.findAllOrderByDateDesc(pageable);
        
        return ResponseEntity.ok(newsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return newsRepository.findById(id)
                .map(news -> ResponseEntity.ok().body(news))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<News> createNews(@RequestBody News news) {
        // Validate required fields
        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (news.getDate() == null || news.getDate().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        News savedNews = newsRepository.save(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNews);
    }

    @PutMapping("/{id}")
    public ResponseEntity<News> updateNews(@PathVariable Long id, @RequestBody News news) {
        return newsRepository.findById(id)
                .map(existingNews -> {
                    // Validate required fields
                    if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
                        return ResponseEntity.badRequest().<News>build();
                    }
                    if (news.getDate() == null || news.getDate().trim().isEmpty()) {
                        return ResponseEntity.badRequest().<News>build();
                    }
                    
                    // Update fields
                    existingNews.setTitle(news.getTitle().trim());
                    existingNews.setDate(news.getDate());
                    existingNews.setContent(news.getContent());
                    
                    News updatedNews = newsRepository.save(existingNews);
                    return ResponseEntity.ok(updatedNews);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        return newsRepository.findById(id)
                .map(news -> {
                    newsRepository.delete(news);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}