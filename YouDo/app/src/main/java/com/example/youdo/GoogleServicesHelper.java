package com.example.youdo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar; // Google Calendar API

import java.security.GeneralSecurityException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;




public class GoogleServicesHelper {
    private GoogleSignInClient googleSignInClient;
    private Activity activity;

    public GoogleServicesHelper(Activity activity) {
        this.activity = activity;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR))
                .build();
        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public GoogleSignInAccount getSignInAccountFromIntent(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            return task.getResult(ApiException.class);
        } catch (ApiException e) {
            return null;
        }
    }

    public void handleSignInResult(GoogleSignInAccount account, GoogleSignInResultCallback callback) {
        if (account != null) {
            // google sign in account data
            callback.onSignInSuccess(account);
        } else {
            callback.onSignInFailure(new Exception("Google sign in failed."));
        }
    }

    public interface GoogleSignInResultCallback {
        void onSignInSuccess(GoogleSignInAccount account);
        void onSignInFailure(Exception e);
    }

    // calendar

    public Calendar getGoogleCalendarService(GoogleSignInAccount account) throws GeneralSecurityException, IOException {
        // creates and returns a Google Calendar service instance, authenticated with the user's Google account.
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                activity, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccount(account.getAccount());

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("YouDo")
                .build();
    }


    public java.util.Calendar getCalendarInstance() {
        return java.util.Calendar.getInstance();
    }

    private String formatDateTimeForGoogleCalendar(java.util.Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        return dateFormat.format(calendar.getTime());
    }

    public void createGoogleCalendarEvent(GoogleSignInAccount account, String title, String description, java.util.Calendar startTime, java.util.Calendar endTime, GoogleCalendarEventCallback callback) {
        try {
            Calendar service = getGoogleCalendarService(account);
            Event event = new Event()
                    .setSummary(title)
                    .setDescription(description);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));

            String startDateTime = dateFormat.format(startTime.getTime());
            String endDateTime = dateFormat.format(endTime.getTime());

            event.setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDateTime)).setTimeZone("CET"));
            event.setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endDateTime)).setTimeZone("CET"));

            // Set the event reminders
            /*
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(
                            new EventReminder().setMethod("email").setMinutes(24 * 60),
                            new EventReminder().setMethod("popup").setMinutes(10)));
            event.setReminders(reminders);*/

            String calendarId = "primary";
            event = service.events().insert(calendarId, event).execute();
            callback.onEventCreated(event);
        } catch (Exception e) {
            callback.onEventCreationError(e);
        }
    }

    public interface GoogleCalendarEventCallback {
        void onEventCreated(Event event);
        void onEventCreationError(Exception e);
    }

    public static GoogleSignInAccount getSignedInAccount(Context context) {
        return GoogleSignIn.getLastSignedInAccount(context);
    }



}