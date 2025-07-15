package com.bms.bms.controller;

import com.bms.bms.model.Book;
import com.bms.bms.repository.BookRepository;
import com.bms.bms.service.S3Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Public Routes")
@RequestMapping("/public")
public class PublicController {

    private final BookRepository bookRepository;

    PublicController(BookRepository br, S3Service S3Service) {
        this.bookRepository = br;
    }

    @GetMapping("/books")
    public Iterable<Book> getBooks(@RequestParam(required = false) String category) {
        Iterable<Book> allBooks = bookRepository.findAll();
        if (category != null) {
            List<Book> filtered = new ArrayList<Book>();
            allBooks.forEach(book -> {
                if (book.getCategory().equals(category)) {
                    filtered.add(book);
                }
            });
            return filtered;
        } else {
            return allBooks;
        }
    }

    @GetMapping("/search/{query}")
    public Iterable<Book> searchBooks(@PathVariable(required = true) String query) {
        return bookRepository.findByTitleContainingIgnoreCase(query);
    }
}
