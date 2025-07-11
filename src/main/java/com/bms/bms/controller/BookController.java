package com.bms.bms.controller;

import com.bms.bms.dto.BookRequest;
import com.bms.bms.model.Book;
import com.bms.bms.repository.BookRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Book Routes")
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    BookController(BookRepository br) {
        this.bookRepository = br;
    }

    @GetMapping
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
        }
        else {
            return allBooks;
        }
    }

    @PostMapping
    public String saveBook(@RequestBody BookRequest bookreq) {
        System.out.println("Adding a Book");
        Book newBook = new Book();
        newBook.setTitle(bookreq.getTitle());
        newBook.setDescription(bookreq.getDescription());
        newBook.setContent(bookreq.getContent());
        newBook.setUsername(bookreq.getUsername());
        newBook.setISBN(bookreq.getISBN());
        newBook.setCategory(bookreq.getCategory());
        newBook.setDate(LocalDate.now());
        bookRepository.save(newBook);
        System.out.println("Added a Book");
        System.out.println(bookreq);
        return "Added a Book";
    }

}
