package com.bms.bms.controller;

import com.bms.bms.model.Book;
import com.bms.bms.repository.BookRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "User Routes")
@RequestMapping("/users/")
public class UserController {

    private final BookRepository bookRepository;

    UserController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/{username}")
    public Iterable<Book> getUserBooks(@PathVariable("username") String username, @RequestParam(required = false) String category) {
        Iterable<Book> allBooks = bookRepository.findAll();
        List<Book> filtered = new ArrayList<Book>();
        if (category != null) {
            allBooks.forEach(book -> {
                if (book.getUsername().equals(username) && book.getCategory().equals(category)) {
                    filtered.add(book);
                }
            });
        }
        else {
            allBooks.forEach(book -> {
                if (book.getUsername().equals(username)) {
                    filtered.add(book);
                }
            });
        }
        return filtered;
    }
}
