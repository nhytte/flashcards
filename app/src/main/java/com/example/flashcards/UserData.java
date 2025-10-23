package com.example.flashcards;

import com.google.gson.annotations.SerializedName;

// Simple class to hold user data for sending to Supabase
public class UserData {

    @SerializedName("user_id") // Match the column name in your Supabase table
    String userId;

    @SerializedName("display_name") // Match the column name
    String displayName;

    @SerializedName("email") // Match the column name
    String email;

    // Constructor
    public UserData(String userId, String displayName, String email) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
    }

    // Getters might be needed depending on usage, but not strictly for sending
}