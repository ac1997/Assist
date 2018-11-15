package com.caltruism.assist.data;

import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;

public class Users {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String gender;
    private String photoURL;
    private FieldValue joinedDate;
    private float ratings;
    private HashMap<String, Integer> stats;

    public Users() {
        // Firestore requires empty constructor
    }

    public Users(String firstName, String lastName, String email, String photoURL) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.photoURL = photoURL;

        this.phoneNumber = null;
        this.gender = null;
        this.joinedDate = FieldValue.serverTimestamp();
        this.ratings = 0.0f;
        this.stats = new HashMap<>();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public FieldValue getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(FieldValue joinedDate) {
        this.joinedDate = joinedDate;
    }

    public float getRatings() {
        return ratings;
    }

    public void setRatings(float ratings) {
        this.ratings = ratings;
    }

    public HashMap<String, Integer> getStats() {
        return stats;
    }

    public void setStats(HashMap<String, Integer> stats) {
        this.stats = stats;
    }
}
