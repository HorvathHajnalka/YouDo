package com.example.youdo.HelperServices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.youdo.Database.dbConnectToDo;
import com.example.youdo.HelperServices.StepCounterHelper.EventCallback;
import com.example.youdo.Models.ToDo;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;

// initializes the Google Calendar Service with the user's Google account.
public class GoogleCalendarService {
    private static final String TAG = "GoogleCalendarService";
    private com.google.api.services.calendar.Calendar service;
    private Context context;
    private dbConnectToDo db;

    public GoogleCalendarService(Context context, GoogleSignInAccount account) {
        this.context = context;
        this.db = new dbConnectToDo(context);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(CalendarScopes.CALENDAR));

        credential.setSelectedAccountName(account.getAccount().name);

        HttpTransport httpTransport = new NetHttpTransport();
        service = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, new GsonFactory(), credential)
                .setApplicationName("YouDo")
                .build();
    }

    //  creates an event in the user's Google Calendar and adds it to the local database.
    public void createAndAddEventToGoogleCalendar(ToDo newtodo, GoogleSignInAccount account, String title, String description, String dateStr, EventCallback callback) {

        String googleAccountId = account.getId();

        // convert date string to LocalDate
        LocalDate startDate = LocalDate.parse(dateStr);

        ZonedDateTime startTime = startDate.atStartOfDay(ZoneOffset.UTC);

        startTime = startTime.minusHours(1);

        if (ZoneId.of("Europe/Budapest").getRules().isDaylightSavings(startTime.toInstant())) {
            startTime = startTime.minusHours(1);
        }


        ZonedDateTime endTime = startTime.plusDays(1);

        // use GoogleAccountCredential for authentication
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(CalendarScopes.CALENDAR));

        credential.setSelectedAccountName(account.getAccount().name);

        // build the Calendar service
        com.google.api.services.calendar.Calendar service = null;
        HttpTransport httpTransport = new NetHttpTransport();
        service = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, new GsonFactory(), credential)
                .setApplicationName("YouDo")
                .build();

        addEventToGoogleCalendar(newtodo, service, title, description, startTime, endTime, new EventCallback(){
            @Override
            public void onEventAdded(String eventId) {
                callback.onEventAdded(eventId);
            }

            @Override
            public void onError() {
                callback.onError();
            }
        });
    }

    // add an event to Google Calendar using an AsyncTask to perform network operations on a separate thread.
    private void addEventToGoogleCalendar(ToDo newtodo, com.google.api.services.calendar.Calendar service,
                                          String title, String description,
                                          ZonedDateTime startTime, ZonedDateTime endTime,
                                          EventCallback callback) {
        new GoogleCalendarService.AddEventTask(newtodo, service, title, description, startTime, endTime, callback).execute();
    }

    private class AddEventTask extends AsyncTask<Void, Void, String> {
        ToDo newtodo;
        private com.google.api.services.calendar.Calendar service;
        private String title;
        private String description;
        private ZonedDateTime startTime;
        private ZonedDateTime endTime;
        private EventCallback callback;

        public AddEventTask(ToDo newtodo, com.google.api.services.calendar.Calendar service,
                            String title, String description,
                            ZonedDateTime startTime, ZonedDateTime endTime,
                            EventCallback callback) {
            this.newtodo = newtodo;
            this.service = service;
            this.title = title;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
            this.callback = callback;
        }


        @Override
        protected String doInBackground(Void... params) {
            try {
                Event event = new Event()
                        .setSummary(title)
                        .setDescription(description);

                DateTime start = new DateTime(Date.from(startTime.toInstant()));
                DateTime end = new DateTime(Date.from(endTime.toInstant()));

                event.setStart(new EventDateTime().setDateTime(start));
                event.setEnd(new EventDateTime().setDateTime(end));

                Event createdEvent = service.events().insert("primary", event).execute();
                return createdEvent.getId();
            } catch (GoogleJsonResponseException e) {
                Log.e("GoogleCalendar", "Google API error: " + e.getDetails().getMessage(), e);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("GoogleCalendar", "Network error while creating event: " + e.getMessage(), e);
                return null;
            } catch (Exception e) {
                Log.e("GoogleCalendar", "Unexpected error: " + e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String eventId) {
            // method executed on the UI thread after the background computation finishes.
            super.onPostExecute(eventId);

            if (eventId != null) {
                Toast.makeText(context, "Event added to your Google Calendar", Toast.LENGTH_LONG).show();
                newtodo.setGoogleTodoId(eventId);
                db.updateToDo(newtodo);
                // String val = String.valueOf(db.getToDoByGoogleId(eventId));
                // Toast.makeText(UploadToDoActivity.this, "Event updated"+ newtodo.getTodoId(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Failed to add even to your Google Calendar", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateEvent(GoogleSignInAccount account, String eventId, String strToDoName, String strToDoDesc, String strDate) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(CalendarScopes.CALENDAR));

        credential.setSelectedAccountName(account.getAccount().name);
        // build the Calendar service
        com.google.api.services.calendar.Calendar service = null;
        HttpTransport httpTransport = new NetHttpTransport();
        service = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, new GsonFactory(), credential)
                .setApplicationName("YouDo")
                .build();
        if (service == null) {
            Log.w("UpdateEvent", "Google Calendar API service not initialized.");
            return;
        }

        try {
            Event event = service.events().get("primary", eventId).execute();

            event.setSummary(strToDoName);
            event.setDescription(strToDoDesc);

            LocalDate startDate = LocalDate.parse(strDate);

            ZonedDateTime startTime = startDate.atStartOfDay(ZoneId.of("Europe/Budapest"));
            ZonedDateTime endTime = startTime.plusDays(1);

            EventDateTime start = new EventDateTime().setDateTime(new DateTime(startTime.toInstant().toString())).setTimeZone("Europe/Budapest");
            event.setStart(start);

            EventDateTime end = new EventDateTime().setDateTime(new DateTime(endTime.toInstant().toString())).setTimeZone("Europe/Budapest");
            event.setEnd(end);

            event = service.events().update("primary", event.getId(), event).execute();

            Log.d("UpdateEvent", "Event updated: " + event.getHtmlLink());


        } catch (Exception e) {
            Log.e("UpdateEvent", "Exception occurred: " + e.getMessage());
        }
    }

}

