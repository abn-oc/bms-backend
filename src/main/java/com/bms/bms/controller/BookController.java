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
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "pdf", required = false) MultipartFile pdf
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
        try {
            Optional<Book> toDelO = bookRepository.findById(id);
            if (toDelO.isPresent()) {
                Book toDel = toDelO.get();
                bookRepository.deleteById(id);
                s3Service.DeleteImg(id);
                s3Service.deletePdf(id);
                return "Success. Deleted book";
            }
            return "Error. Book by given id not found";
        } catch (Exception e) {
            return "Error. Couldn't delete book";
        }
    }


    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String editBook(
            @PathVariable(name = "id") long id,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "content", required = false) String content,
            @RequestParam(name = "isbn", required = false) Integer isbn,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "pdf", required = false) MultipartFile pdf
    ) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return "Error. Book not found";
        }

        if (title != null) book.setTitle(title);
        if (description != null) book.setDescription(description);
        if (content != null) book.setContent(content);
        if (isbn != null) book.setISBN(isbn);
        if (category != null) book.setCategory(category);
        if (username != null) book.setUsername(username);

        if (image != null) {
            String url = s3Service.UploadImg(image, id);
            if (url == null) {
                return "Error. Couldn't upload image";
            }
            book.setImageURL(url);
        }
        if (pdf != null) {
            String url = s3Service.uploadPdf(pdf, id);
            if (url == null) {
                return "Error. Couldn't upload pdf";
            }
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
            book.setPdfURL(null);
            bookRepository.save(book);
            s3Service.deletePdf(id);
            return "Success. Image removed";
        } else {
            return "Error. Book not found";
        }
    }
}
