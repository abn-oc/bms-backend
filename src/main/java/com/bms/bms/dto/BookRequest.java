package com.bms.bms.dto;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class BookRequest {
    private String Title;
    private String Description;
    private String Content;
    private String Username;
    private Integer ISBN;
    private String Category;
}
