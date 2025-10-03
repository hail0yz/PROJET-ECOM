package com.ecom.avis.dao;

import com.ecom.avis.model.Review;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ReviewDAO {
    List<Review> findAll();
    Review findById(long id);
    Review save(Review review);
}