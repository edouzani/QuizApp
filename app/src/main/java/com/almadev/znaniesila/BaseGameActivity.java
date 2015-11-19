/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almadev.znaniesila;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

/**
 * Example base class for games. This implementation takes care of setting up
 * the GamesClient object and managing its lifecycle. Subclasses only need to
 * override the @link{#onSignInSucceeded} and @link{#onSignInFailed} abstract
 * methods. To initiate the sign-in flow when the user clicks the sign-in
 * button, subclasses should call @link{#beginUserInitiatedSignIn}. By default,
 * this class only instantiates the GamesClient object. If the PlusClient or
 * AppStateClient objects are also wanted, call the BaseGameActivity(int)
 * constructor and specify the requested clients. For example, to request
 * PlusClient and GamesClient, use BaseGameActivity(CLIENT_GAMES | CLIENT_PLUS).
 * To request all available clients, use BaseGameActivity(CLIENT_ALL).
 * Alternatively, you can also specify the requested clients via
 *
 * @author Bruno Oliveira (Google)
 * @link{#setRequestedClients}, but you must do so before @link{#onCreate}
 * gets called, otherwise the call will have no effect.
 */
public abstract class BaseGameActivity extends FragmentActivity implements
                                                                GoogleApiClient.ConnectionCallbacks,
                                                                GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnectionSuspended(final int i) {
        mGoogleApiClient.connect();
    }

    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow        = true;
    private boolean mSignInClicked              = false;

    protected void gpSignIn() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    protected void gpSignOut() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(final ConnectionResult pConnectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                                                        mGoogleApiClient, pConnectionResult,
                                                        RC_SIGN_IN, getResources().getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;

//                Toast.makeText(this, "К сожалению, подключение к сервису Google Games в данный момент невозможно", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void getAllLeaderboards() {
        if (mGoogleApiClient != null) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
            try {
                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), 2);
            } catch (Exception e) {
                Log.d("GplayServices", "Problem connecting to playservices");
                e.printStackTrace();
            }
        } else {
            Log.d("GplayServices", "Still not connected");
        }
    }

    protected void openLeaderBoard(String leaderBoardId, int requestId) {
        if (mGoogleApiClient != null) {
            try {
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, leaderBoardId), requestId);
            } catch (Exception e) {
                Log.d("GplayServices", "Problem connecting to playservices");
                e.printStackTrace();
            }
        } else {
            Log.d("GplayServices", "Still not connected");
        }
    }

    protected void submitScore(String leaderboardId, int score) {
     if (mGoogleApiClient != null) {
         Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardId, score);
     }
    }

    @Override
    public void onConnected(final Bundle pBundle) {

    }

    /**
     * Constructs a BaseGameActivity with default client (GamesClient).
     */
    protected BaseGameActivity() {
        super();
    }

    /**
     * Constructs a BaseGameActivity with the requested clients.
     *
     * @param requestedClients The requested clients (a combination of CLIENT_GAMES,
     *                         CLIENT_PLUS and CLIENT_APPSTATE).
     */
    protected BaseGameActivity(int requestedClients) {
        super();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                        // add other APIs and scopes here as needed
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
      if (mGoogleApiClient.isConnected()) {
          mGoogleApiClient.disconnect();
      }
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);

        if (request == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (response == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user th    at sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                                                      request, response, R.string.signin_failure);
            }
        }
    }

}
