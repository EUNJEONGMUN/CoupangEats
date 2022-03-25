package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhotoReview {
    private int reviewIdx;
    private String reviewImgUrl;
    private String content;
    private int score;
    private String createdAt;
}
