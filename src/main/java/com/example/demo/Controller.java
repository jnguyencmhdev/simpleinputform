// src/main/java/com/example/demo/EntryController.java
package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Controller
@RequestMapping("/entries")
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private List<EntryField> entries = new ArrayList<>();

    @PostMapping
    public String createEntry(@ModelAttribute EntryField entryField, Model model) {
        logger.info("Received new entry: Name={}, Age={}, Title={}, Hometown={}", entryField.getName(), entryField.getAge(), entryField.getTitle(), entryField.getHometown());

        entries.add(entryField);
        model.addAttribute("entries", entries);
        return "entries";
    }

    @GetMapping
    public String getAllEntries(Model model) {
        logger.info("display entry");

        model.addAttribute("entries", entries);
        return "entries";
    }

        @GetMapping("/")
    public String showIndex() {
        return "index";
    }
}