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
import java.util.Optional;

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
        } else {
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
            String url = s3Service.Upload(image, uploadedBook.getId());
            if (url == null) {
                bookRepository.deleteById(uploadedBook.getId());
                return "Error. Book NOT Added";
            }
            uploadedBook.setImageURL(url);
            bookRepository.save(uploadedBook);
        }
        return "Success. Added a Book";
    }

    @DeleteMapping
    public String deleteBook(@RequestParam long id, @RequestParam String username) {
        try {
            Optional<Book> toDelO = bookRepository.findById(id);
            if (toDelO.isPresent()) {
                Book toDel = toDelO.get();
                if (toDel.getUsername().equals(username)) {
                    bookRepository.deleteById(id);
                    return "Success. Deleted book";
                }
                return "Error. username does'nt match";
            }
            return "Error. Book by given id not found";
        } catch (Exception e) {
            return "Error. Couldn't delete book";
        }
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String editBook(
            @RequestParam("id") long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("content") String content,
            @RequestParam("username") String username,
            @RequestParam("isbn") Integer isbn,
            @RequestParam("category") String category,
            @RequestParam(name = "image", required = false) MultipartFile image
    ) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return "Error. Book not found";
        }
        if (!book.getUsername().equals(username)) {
            return "Error. Username does'nt match";
        }
        book.setTitle(title);
        book.setDescription(description);
        book.setContent(content);
        book.setUsername(username);
        book.setISBN(isbn);
        book.setCategory(category);
        if (image != null) {
            String url = s3Service.Upload(image, id);
            if (url == null) {
                return "Error. Couldn't upload image";
            }
            book.setImageURL(url);
        }

        bookRepository.save(book);
        return "Success. Book updated";
    }

    @PutMapping("/remove-image")
    public String removeBookImage(@RequestParam long id, @RequestParam String username) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            if (!book.getUsername().equals(username)) {
                return "Error. Username doesn't match";
            }
            book.setImageURL(null);
            bookRepository.save(book);
            return "Success. Image removed";
        } else {
            return "Error. Book not found";
        }
    }


}
