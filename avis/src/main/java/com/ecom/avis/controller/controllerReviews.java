package com.ecom.avis.controller;

import com.ecom.avis.dao.ReviewDAO;
import com.ecom.avis.model.Review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class controllerReviews {
    private final ReviewDAO reviewDAO;
    public controllerReviews(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }
    @GetMapping("/reviews")
    public List<Review> listeReviews(){
        return reviewDAO.findAll();
    }
    @GetMapping ("/reviews/{id}")
    public Review listeReviews(@PathVariable int id){
        return new Review(id, "blabla",10,1,1);
    }

}
