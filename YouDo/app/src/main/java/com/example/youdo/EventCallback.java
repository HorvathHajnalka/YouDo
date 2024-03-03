package com.example.youdo;

public interface EventCallback {
    void onEventAdded(String eventId);

    void onError();
}