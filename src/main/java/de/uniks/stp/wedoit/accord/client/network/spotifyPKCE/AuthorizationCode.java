package de.uniks.stp.wedoit.accord.client.network.spotifyPKCE;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthorizationCode {
    private static final String clientId = "14d2a17f341444c189820d1f5d704839";
    private static final URI redirectUri = SpotifyHttpManager.makeUri("https://ac.uniks.de");
    private static final String code = "c-oGaPdYJF3tu3oUZRUiBHWQvm4oHnBrsxfHackYzzomKJiy5te1k04LJdr6XxjACe9TonpJR8NPOQ3o5btASx_oMw4trmXLYdkda77wY0NJ9Scl69lKvGiOfdnRi5Q0IbBu185Y0TZgyUJz3Auqqv-Wk7zjRke4DzqYEc3ucyUBOq08j5223te-G2K72aL9PxgVJaEHBbLvhdJscCy-zcyU29EZoNlG_E5";
    private static final String codeVerifier = "NlJx4kD4opk4HY7zBM6WfUHxX7HoF8A2TUhOIPGA74w";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setRedirectUri(redirectUri)
            .build();
    private static final AuthorizationCodePKCERequest authorizationCodePKCERequest = spotifyApi.authorizationCodePKCE(code, codeVerifier)
            .build();

    public static void authorizationCode_Sync() {
        try {
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodePKCERequest.execute();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void authorizationCode_Async() {
        try {
            final CompletableFuture<AuthorizationCodeCredentials> authorizationCodeCredentialsFuture = authorizationCodePKCERequest.executeAsync();

            // Thread free to do other tasks...

            // Example Only. Never block in production code.
            final AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeCredentialsFuture.join();

            // Set access and refresh token for further "spotifyApi" object usage
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            System.out.println("Expires in: " + authorizationCodeCredentials.getExpiresIn());
        } catch (CompletionException e) {
            System.out.println("Error: " + e.getCause().getMessage());
        } catch (CancellationException e) {
            System.out.println("Async operation cancelled.");
        }
    }

    public static void main(String[] args) {
        authorizationCode_Sync();
        authorizationCode_Async();
    }
}
