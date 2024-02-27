package com.example.youdo;

import android.app.Activity;
import android.content.Intent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;

// for calendar apis

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

import java.security.GeneralSecurityException;
import java.io.IOException;
import java.util.Collections;


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

}
