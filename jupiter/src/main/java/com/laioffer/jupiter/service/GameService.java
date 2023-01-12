package com.laioffer.jupiter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.jupiter.entity.db.Item;
import com.laioffer.jupiter.entity.db.ItemType;
import com.laioffer.jupiter.entity.response.Game;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Service
public class GameService {
    /**
     * set up some fields --> TOKEN/CLIENT_ID/TOP_GAME_URL/GAME_SEARCH_URL_TEMPLATE/DEFAULT_GAME_LIMIT
     * */
    private static final String TOKEN = "Bearer iughuetdxhmhyry80gr3fsk7qlhqfz";
    private static final String CLIENT_ID = "6yo3b0bmtpfx5glncc2e2ncdneez35";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    // Get Top Games: Gets information about all broadcasts on Twitch.
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    // Get Game: You may get up to 100 categories or games by specifying their ID or name.
    private static final int DEFAULT_GAME_LIMIT = 20;

    // search
    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;


    /**Implement buildGameURL function.
     * It will help generate the correct URL when you call Twitch Game API.
     * */
    // 1. Build the request URL which will be used when calling Twitch APIs, e.g. https://api.twitch.tv/helix/games/top when trying to get top games.
    // 1.1: gameName --> GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s"
    // 1.2: limit --> TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s"
    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            return String.format(url, limit);
            // In java, String format() method returns a formatted string using the given locale,
            // specified format string, and arguments
        } else {
            try {
                // Encode special characters in URL, e.g. Rick Sun -> Rick%20Sun
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }


    // 2. Send HTTP request to Twitch Backend based on the given URL
    //    returns the body of the HTTP response returned from Twitch backend.
    private String searchTwitch(String url) throws TwitchException {
        // 2.1: create a HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // 2.2: Define the response handler to parse and return HTTP response body returned from Twitch
        // 这里的responseHandler是程序下面自己call --> httpclient.execute(request, responseHandler);

//        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
//            @Override
//            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
//                int responseCode = response.getStatusLine().getStatusCode();
//                if (responseCode != 200) {
//                    System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
//                    throw new TwitchException("Failed to get result from Twitch API");
//                }
//                HttpEntity entity = response.getEntity();
//                if (entity == null) {
//                    throw new TwitchException("Failed to get result from Twitch API");
//                }
//                JSONObject obj = new JSONObject(EntityUtils.toString(entity));
//                return obj.getJSONArray("data").toString();
//            }
//        }

        ResponseHandler<String> responseHandler = response -> {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new TwitchException("Failed to get result from Twitch API");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();
            // 这里的data是postman里面的data
        };

        try {
            // 2.3: Define the HTTP request, TOKEN and CLIENT_ID are used for user authentication on Twitch backend
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-Id", CLIENT_ID);
            return httpclient.execute(request, responseHandler); // callback -> on line 74
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
    * 3. Finally, add the getGameList method to convert Twitch returned data to a list of Game objects.
    * Then, provide two public methods topGames and searchGame to return the top game list, or a dedicated game.
    *
    * **/
    // 3.1: Convert JSON form at data returned from Twitch to an Arraylist of Game objects

    private List<Game> getGameList(String data){
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Jakson 把数据读取 但不能直接变成list Game
            return Arrays.asList(mapper.readValue(data, Game[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse game data from Twitch API");
        }
    }


    // 3.2: Integrate search() and getGameList() together, returns the top x popular games from Twitch.
    /**
     * topGames() and searchGame(gameName) are used in gameController
     * */
    public List<Game> topGames(int limit){
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT; // 20
        }
        /**
         * String gameUrl = buildGameURL(TOP_GAME_URL, "", limit)
         * String jasonString = searchTwitch(gameUrl) Http请求
         * return getGameList(jasonString) --> 把String变成Game List
         * */
        return getGameList(searchTwitch(buildGameURL(TOP_GAME_URL, "", limit)));
    }

    // Integrate search() and getGameList() together, returns the dedicated game based on the game name.
    public Game searchGame(String gameName){
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));

        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }




    /**
     * create search URL -->video/clip/stream
     * Similar to buildGameURL, build Search URL that will be used when calling Twitch API.
     * */
    // 1. Similar to buildGameURL, build Search URL that will be used when calling Twitch API.
    // e.g. https://api.twitch.tv/helix/clips?game_id=12924.

    private String buildSearchURL(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } // 可以删除 因为gameID不会有空过
        return String.format(url, gameId, limit);
    }
    // 2. Similar to getGameList, convert the json data returned from Twitch to a list of Item objects.
    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to parse item data from Twitch API");
        }
    }

    // Returns the top x streams based on game ID.
    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        List<Item> streams = getItemList(searchTwitch(buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }
        return streams;
    }

    // Returns the top x clips based on game ID.
    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    // Returns the top x videos based on game ID.
    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }

//        List<Item> streamItems = searchByType(gameId, ItemType.STREAM, DEFAULT_SEARCH_LIMIT);
//        List<Item> clipItems = searchByType(gameId, ItemType.CLIP, DEFAULT_SEARCH_LIMIT);
//        List<Item> videoItems =  searchByType(gameId, ItemType.VIDEO, DEFAULT_SEARCH_LIMIT);
//        itemMap.put("STREAM",streamItems );
//        itemMap.put("CLIP", clipItems);
//        itemMap.put("VIDEO", videoItems);

        return itemMap;
    }
}
