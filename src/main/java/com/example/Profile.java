package com.example;

public class Profile {
    private int profileId;
    private int userId;
    private String bio;
    private String profilePicture;

    public Profile(int profileId, int userId, String bio, String profilePicture) {
        this.profileId = profileId;
        this.userId = userId;
        this.bio = bio;
        this.profilePicture = profilePicture;
    }

    // Getters and setters
    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
