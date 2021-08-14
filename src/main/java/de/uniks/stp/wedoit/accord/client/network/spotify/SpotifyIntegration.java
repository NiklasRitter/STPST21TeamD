package de.uniks.stp.wedoit.accord.client.network.spotify;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import de.uniks.stp.wedoit.accord.client.Editor;
import org.apache.hc.core5.http.ParseException;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpotifyIntegration implements HttpHandler {

    private final Editor editor;
    private SpotifyApi spotifyApi;
    private final String clientId = "14d2a17f341444c189820d1f5d704839";
    private final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8000/spotify");
    private String codeChallenge;
    private String codeVerifier;
    public ExecutorService executorService;
    private HttpServer server;
    private AuthorizationCodeCredentials authorizationCodeCredentials;

    public SpotifyIntegration(Editor editor) {
        this.editor = editor;
        this.executorService = Executors.newCachedThreadPool();
    }

    //------------------------------------------------------------------------------------------------------------------
    // Authorization
    //------------------------------------------------------------------------------------------------------------------

    public void authorize() {
        try {
            this.server = HttpServer.create(new InetSocketAddress("localhost", 8000), 0);
            this.server.createContext("/spotify", this);
            this.server.setExecutor(executorService);
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setRedirectUri(redirectUri)
                .build();

        generateCodeChallengeAndVerifier();
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodePKCEUri(codeChallenge)
                .scope("user-read-currently-playing")
                .build();

        final URI uri = authorizationCodeUriRequest.execute();

        Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            desktop.browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reauthorize() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setRefreshToken(this.editor.getRefreshToken())
                .build();

        try {
            AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequest = spotifyApi.authorizationCodePKCERefresh().build();

            authorizationCodeCredentials = authorizationCodePKCERefreshRequest.execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            this.editor.saveRefreshToken(this.spotifyApi.getRefreshToken());

            this.editor.getSpotifyManager().setupTrackTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();

        Map<String, String> queryMap = getQuery(query);
        String response;
        OutputStream outputStream = exchange.getResponseBody();

        if (queryMap.containsKey("code")) {
            response = "authorization successful";
            AuthorizationCodePKCERequest authorizationCodePKCERequest = spotifyApi.authorizationCodePKCE(queryMap.get("code"), codeVerifier)
                    .build();

            getAuthCodeCredentials(authorizationCodePKCERequest);
            this.editor.saveRefreshToken(this.spotifyApi.getRefreshToken());
            try {
                exchange.sendResponseHeaders(200, response.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response = "authorization failure";
            try {
                exchange.sendResponseHeaders(400, response.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.editor.getSpotifyManager().setupTrackTimer();
        stopServer();
    }

    private void getAuthCodeCredentials(AuthorizationCodePKCERequest authorizationCodePKCERequest) {
        try {
            this.authorizationCodeCredentials = authorizationCodePKCERequest.execute();

            this.spotifyApi.setAccessToken(this.authorizationCodeCredentials.getAccessToken());
            this.spotifyApi.setRefreshToken(this.authorizationCodeCredentials.getRefreshToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getQuery(String queryString) {
        Map<String, String> queryParts = new LinkedHashMap<>();
        String[] parts = queryString.split("&");
        for (String part: parts) {
            int index = part.indexOf("=");
            queryParts.put(URLDecoder.decode(part.substring(0, index), StandardCharsets.UTF_8),
                    URLDecoder.decode(part.substring(index + 1), StandardCharsets.UTF_8));
        }
        return queryParts;
    }

    protected void stopServer() {
        server.stop(0);
        executorService.shutdown();
    }

    //------------------------------------------------------------------------------------------------------------------
    // Get Users currently playing track
    //------------------------------------------------------------------------------------------------------------------

    public void setUsersCurrentlyPlayingTrack() {
        GetUsersCurrentlyPlayingTrackRequest getUsersCurrentlyPlayingTrackRequest = this.spotifyApi
                .getUsersCurrentlyPlayingTrack()
                .build();
        CurrentlyPlaying currentlyPlaying = null;
        try {
            currentlyPlaying = getUsersCurrentlyPlayingTrackRequest.execute();
        } catch (UnauthorizedException e) {
            reauthorize();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        String description = createDescription(currentlyPlaying);
        this.editor.getLocalUser().setSpotifyCurrentlyPlaying(description);
    }

    public String createDescription(CurrentlyPlaying currentlyPlaying) {
        String response = "";
        if (currentlyPlaying != null) {
            Track track = (Track) currentlyPlaying.getItem();
            StringBuilder artistNames = new StringBuilder();
            boolean multipleArtists = false;
            if (track != null) {
                for (ArtistSimplified artist : track.getArtists()) {
                    if (!multipleArtists) {
                        artistNames.append(artist.getName());
                        multipleArtists = true;
                    } else {
                        artistNames.append(" , ").append(artist.getName());
                    }
                }
                response = track.getName() + " - " + artistNames;
            }
        } else {
            response = null;
        }
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    // PKCE Methods
    //------------------------------------------------------------------------------------------------------------------

    public void generateCodeChallengeAndVerifier() {
        this.codeVerifier = generateCodeVerifier();
        this.codeChallenge = generateCodeChallenge(codeVerifier);
    }

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private String generateCodeChallenge(String codeVerifier) {
        byte[] digest = null;
        try {
            byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes, 0, bytes.length);
            digest = messageDigest.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}
