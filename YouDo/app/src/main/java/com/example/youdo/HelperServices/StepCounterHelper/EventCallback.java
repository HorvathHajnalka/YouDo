package com.example.youdo.HelperServices.StepCounterHelper;

// Implementations of this interface can be used to receive notifications
//  when an event is added successfully or when an error occurs during the process.

public interface EventCallback {
    void onEventAdded(String eventId);
    void onError();
}