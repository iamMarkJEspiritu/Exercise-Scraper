package com.springprojs.webscraper.controllers;

import java.io.IOException;
import java.util.List;

import com.springprojs.webscraper.models.Reviewer;
import com.springprojs.webscraper.services.HomePageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomePageController {
    private final HomePageService hpservice;

    @Autowired
    public HomePageController(HomePageService hpservice){
        this.hpservice = hpservice;
    }

    @GetMapping
    public List<Reviewer> getReviewers() throws IOException {
        //Include Filtering
        return hpservice.getAllReviewers();
    }

    @PostMapping
    public List<String> generateReviewers() throws IOException{
        hpservice.scrape("https://www.yelp.com/biz/honeybear-boba-san-francisco-3");
        //hpservice.scrape(user-input-json);
        return List.of("Hello", "Philippines");
    }
}
