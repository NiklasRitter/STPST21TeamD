package de.uniks.stp.wedoit.accord.client.network.spotifyPKCE;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.net.URI;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthOld {
    private final String clientId = "556ecc7a828f481898a2dca095dcb3fd";
    private final URI redirectUri = SpotifyHttpManager.makeUri("https://example.com/spotify-redirect");
    private final String codeChallenge = "w6iZIj99vHGtEx_NVl9u3sthTN646vvkiP8OMCGfPmo";

    private final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setRedirectUri(redirectUri)
            .build();
    private final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(codeChallenge)
//          .state("x4xkmn9pu3j6ukrs8n")
//          .scope("user-read-birthdate,user-read-email")
//          .show_dialog(true)
            .build();

    public void main (String[]args){
        authorizationCodeUri_Sync();
        authorizationCodeUri_Async();
    }

    public void authorizationCodeUri_Sync() {
        final URI uri = authorizationCodeUriRequest.execute();

        System.out.println("URI: " + uri.toString());
    }

    public void authorizationCodeUri_Async() {
        try {
            final CompletableFuture<URI> uriFuture = authorizationCodeUriRequest.executeAsync();

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            final URI uri = uriFuture.join();

            System.out.println("URI: " + uri.toString());
        } catch (CompletionException e) {
            System.out.println("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            System.out.println("Async operation cancelled.");
        }
    }
}