package com.example.test;

public class Review {
    private String reviewId; // 리뷰의 고유 ID
    private String userId;
    private String storeName;
    private String storeAddress;
    private float rating;
    private String review;
    private boolean glamCategory;
    private boolean simpleCategory;
    private boolean uniqueCategory;
    private boolean y2kCategory;
    private boolean themeCategory;
    private boolean romanticCategory;
    private boolean drawCategory;
    private boolean patternCategory;
    private boolean graCategory;
    private String imageUrl;
    private int price;
    private String date;

    public Review() {
        // Default constructor required for Firebase
    }

    public Review(String reviewId, String userId, String storeName,String storeAddress, float rating,
                  String review, boolean glamCategory, boolean simpleCategory,
                  boolean uniqueCategory,boolean y2kCategory, boolean themeCategory, boolean drawCategory,
                  boolean romanticCategory, boolean patternCategory, boolean graCategory, int price, String date) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.storeName = storeName;
        this.storeAddress = storeAddress;
        this.rating = rating;
        this.review = review;
        this.glamCategory = glamCategory;
        this.simpleCategory = simpleCategory;
        this.uniqueCategory = uniqueCategory;
        this.y2kCategory = y2kCategory;
        this.themeCategory = themeCategory;
        this.romanticCategory = romanticCategory;
        this.drawCategory = drawCategory;
        this.patternCategory = patternCategory;
        this.graCategory = graCategory;
        this.imageUrl = "";
        this.price = price;
        this.date = date;
    }

    public String getReviewId() {
        return reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public String getStoreName() {
        return storeName;
    }
    public String getStoreAddress(){return storeAddress;}

    public float getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public boolean isGlamCategory() {
        return glamCategory;
    }

    public boolean isSimpleCategory() {
        return simpleCategory;
    }

    public boolean isUniqueCategory() {
        return uniqueCategory;
    }

    public boolean isY2kCategory() {
        return y2kCategory;
    }

    public boolean isThemeCategory() {
        return themeCategory;
    }

    public boolean isRomanticCategory() {
        return romanticCategory;
    }

    public boolean isPatternCategory() {
        return patternCategory;
    }

    public boolean isGraCategory() {
        return graCategory;
    }

    public boolean isDrawCategory() {
        return drawCategory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public String getDate() {return date;}
}
