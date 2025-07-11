package com.bms.bms.controller;

import com.bms.bms.model.Book;
import com.bms.bms.repository.BookRepository;
import com.bms.bms.service.S3Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Book Routes")
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final S3Service s3Service;
    BookController(BookRepository br, S3Service S3Service) {
        this.bookRepository = br;
        this.s3Service = S3Service;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String saveBook(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("content") String content,
            @RequestParam("username") String username,
            @RequestParam("isbn") Integer isbn,
            @RequestParam("category") String category,
            @RequestParam(name = "image", required = false) MultipartFile image
    ) {
        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setDescription(description);
        newBook.setContent(content);
        newBook.setUsername(username);
        newBook.setISBN(isbn);
        newBook.setCategory(category);
        newBook.setDate(LocalDate.now());
        Book uploadedBook = bookRepository.save(newBook);
        if (image != null) {
            String uploadStatus = s3Service.Upload(image, uploadedBook.getId());
            if ("Error".equals(uploadStatus)) {
                bookRepository.deleteById(uploadedBook.getId());
                return "Error uploading Image. Did NOT add book.";
            }
            uploadedBook.setImageURL(uploadStatus);
            bookRepository.save(uploadedBook);

        }
        return "Added a Book";
    }

}
