package com.example.demo.src.store.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuOptions {
    private String optionsTitle;
    private String isRequired;
    private int choiceCount;
    private List<MenuOptionsDetail> menuOptionsDetail;
}
