package com.bms.bms.controller;

import com.bms.bms.model.Book;
import com.bms.bms.model.User;
import com.bms.bms.repository.BookRepository;
import com.bms.bms.service.S3Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "Book Routes (Authenticated)")
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final S3Service s3Service;

    BookController(BookRepository br, S3Service s3Service) {
        this.bookRepository = br;
        this.s3Service = s3Service;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((User) authentication.getPrincipal()).getUsername();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String saveBook(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("content") String content,
            @RequestParam("isbn") Integer isbn,
            @RequestParam("category") String category,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "pdf", required = false) MultipartFile pdf
    ) {
        String username = getCurrentUsername();

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
            String url = s3Service.UploadImg(image, uploadedBook.getId());
            if (url == null) {
                bookRepository.deleteById(uploadedBook.getId());
                return "Error. Book NOT Added (Image failed)";
            }
            uploadedBook.setImageURL(url);
        }

        if (pdf != null) {
            String pdfUrl = s3Service.uploadPdf(pdf, uploadedBook.getId());
            if (pdfUrl == null) {
                bookRepository.deleteById(uploadedBook.getId());
                return "Error. Book NOT Added (PDF failed)";
            }
            uploadedBook.setPdfURL(pdfUrl);
        }

        bookRepository.save(uploadedBook);
        return "Success. Added a Book";
    }

    @DeleteMapping
    public String deleteBook(@RequestParam long id) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            if (!book.getUsername().equals(getCurrentUsername())) {
                return "Error. Not authorized to delete this book";
            }
            bookRepository.deleteById(id);
            s3Service.DeleteImg(id);
            s3Service.deletePdf(id);
            return "Success. Deleted book";
        }
        return "Error. Book not found";
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String editBook(
            @PathVariable long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer isbn,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) MultipartFile pdf
    ) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return "Error. Book not found";

        if (!book.getUsername().equals(getCurrentUsername()))
            return "Error. Not authorized to edit this book";

        if (title != null) book.setTitle(title);
        if (description != null) book.setDescription(description);
        if (content != null) book.setContent(content);
        if (isbn != null) book.setISBN(isbn);
        if (category != null) book.setCategory(category);

        if (image != null) {
            String url = s3Service.UploadImg(image, id);
            if (url == null) return "Error. Couldn't upload image";
            book.setImageURL(url);
        }

        if (pdf != null) {
            String url = s3Service.uploadPdf(pdf, id);
            if (url == null) return "Error. Couldn't upload pdf";
            book.setPdfURL(url);
        }

        bookRepository.save(book);
        return "Success. Book updated";
    }

    @PutMapping("/remove-image/{id}")
    public String removeBookImage(@PathVariable long id) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            if (!book.getUsername().equals(getCurrentUsername()))
                return "Error. Not authorized";
            book.setImageURL(null);
            bookRepository.save(book);
            s3Service.DeleteImg(id);
            return "Success. Image removed";
        } else {
            return "Error. Book not found";
        }
    }

    @PutMapping("/remove-pdf/{id}")
    public String removeBookPDF(@PathVariable long id) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            if (!book.getUsername().equals(getCurrentUsername()))
                return "Error. Not authorized";
            book.setPdfURL(null);
            bookRepository.save(book);
            s3Service.deletePdf(id);
            return "Success. PDF removed";
        } else {
            return "Error. Book not found";
        }
    }

    @GetMapping
    public List<Book> myBooks(@RequestParam(required = false) String category) {
        String username = getCurrentUsername();
        List<Book> result = new ArrayList<>();
        bookRepository.findAll().forEach(book -> {
            if (book.getUsername().equals(username)) {
                if (category == null || book.getCategory().equals(category)) {
                    result.add(book);
                }
            }
        });
        return result;
    }

}
