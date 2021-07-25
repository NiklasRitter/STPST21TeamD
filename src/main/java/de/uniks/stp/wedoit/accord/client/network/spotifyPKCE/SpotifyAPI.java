package de.uniks.stp.wedoit.accord.client.network.spotifyPKCE;
/*
import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;

public class SpotifyAPI {

    public SpotifyAPI() {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId("556ecc7a828f481898a2dca095dcb3fd")
                .setClientSecret("1c43ad61b8cc47c6b9ec1a7126056304")
                .setRedirectUri("https://ac.uniks.de")
                .build();
    }

    // For all requests an access token is needed
    SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken("taHZ2SdB-bPA3FsK3D7ZN5npZS47cMy-IEySVEGttOhXmqaVAIo0ESvTCLjLBifhHOHOIuhFUKPW1WMDP7w6dj3MAZdWT8CLI2MkZaXbYLTeoDvXesf2eeiLYPBGdx8tIwQJKgV8XdnzH_DONk")
            .build();

    // Create a request object with the optional parameter "market"
    final GetSomethingRequest getSomethingRequest = spotifyApi.getSomething("qKRpDADUKrFeKhFHDMdfcu")
            .market(CountryCode.DE)
            .build();

    void getSomething_Sync() {
        try {
            // Execute the request synchronous
            final Something something = getSomethingRequest.execute();

            // Print something's name
            System.out.println("Name: " + something.getName());
        } catch (Exception e) {
            System.out.println("Something went wrong!\n" + e.getMessage());
        }
    }

    void getSomething_Async() {
        try {
            // Execute the request asynchronous
            final Future<Something> somethingFuture = getSomethingRequest.executeAsync();

            // Do other things...

            // Wait for the request to complete
            final Something something = somethingFuture.get();

            // Print something's name
            System.out.println("Name: " + something.getName());
        } catch (Exception e) {
            System.out.println("Something went wrong!\n" + e.getMessage());
        }
    }
}
*/