package de.uniks.stp.wedoit.accord.client.network.spotifyPKCE;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.awt.*;
import java.net.URI;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Authorization {
    private final String clientId = "14d2a17f341444c189820d1f5d704839";
    private final URI redirectUri = SpotifyHttpManager.makeUri("https://example.com/spotify-redirect");
    private final String codeChallenge = getPkce();

    private final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setRedirectUri(redirectUri)
            .build();
    private final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(codeChallenge)
            .build();

    public void authorizationCodeUri_Sync() {
        final URI uri = authorizationCodeUriRequest.execute();

        Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            desktop.browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    //------------------------------------------------------------------------------------------------------------------
    // PKCE Methods
    //------------------------------------------------------------------------------------------------------------------

    public String getPkce() {
        return generateCodeChallange(generateCodeVerifier());
    }

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private String generateCodeChallange(String codeVerifier) {
        byte[] digest = null;
        try {
            byte[] bytes = codeVerifier.getBytes("US-ASCII");
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes, 0, bytes.length);
            digest = messageDigest.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}