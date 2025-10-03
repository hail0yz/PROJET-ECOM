package com.ecom.avis.dao;

import com.ecom.avis.model.Review;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
/*-------------------------------
 DONE:
 find By user/book ID
 find all
 find by review ID
 TO DO

 -----------------------------*/
@Repository
public class ReviewDaoImpl implements ReviewDAO{
    public static List<Review> reviews= new ArrayList<>();
    static {
        reviews.add(new Review(1,"blabla1",4,1,1));
        reviews.add(new Review(2,"blabla2",4,1,2));
        reviews.add(new Review(3,"blabla3",3,2,3));
    }
    @Override
    public List<Review> findAll() {
        return reviews;
    }

    @Override
    public Review findById(long id) {
        for (Review review: reviews){
            if (review.getId()==id) {
                return review;
            }
        }
        return null;
    }
    public List<Review> findByBookId(int bookId) {
        List<Review> reviewsRes = new ArrayList<>();
        for (Review review: reviews){
            if (review.getBookId()==bookId){
                reviewsRes.add(review);
            }
        }
        return reviewsRes;
    }
    public List<Review> findByUserId(int userId) {
        List<Review> reviewsRes = new ArrayList<>();
        for (Review review: reviews){
            if (review.getUserId()==userId){
                reviewsRes.add(review);
            }
        }
        return reviewsRes;
    }

    @Override
    public Review save(Review review) {
        reviews.add(review);
        return review;
    }
}
