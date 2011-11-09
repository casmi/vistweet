package vistweet.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import vistweet.data.json.JsonStatus;
import vistweet.data.json.JsonUser;
import vistweet.data.sql.StatusInterface;
import casmi.exception.NetException;
import casmi.exception.ParserException;
import casmi.extension.net.OAuth;
import casmi.io.Reader;
import casmi.net.HTTP;
import casmi.parser.CSV;
import casmi.parser.JSON;
import casmi.util.DateUtil;
import casmi.util.FileUtil;
import casmi.util.SystemUtil;

public final class Twitter {

    public static final String DEFAULT_ICON_URL = "https://twimg0-a.akamaihd.net/sticky/default_profile_images/default_profile_6_normal.png";
    
    // API for authorization
    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String AUTHORIZE_URL     = "https://api.twitter.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL  = "https://api.twitter.com/oauth/access_token";
    
    // consumer key and secret
    private static final String CONSUMER_KEY      = "WrK0HwVTrMBgN558vFg5w";
    private static final String CONSUMER_SECRET   = "JytQH0bO9VYnquH53PrSuyif9hxAOu9mGy8rbQ";
    
    // a file path for save token
    private static final String TOKEN_SAVE_FILE   = SystemUtil.JAVA_TMP_PATH + "vistweet_token.csv";
    
    // API for twitter
    private static final String USER_TIMELINE_REQUEST_URL = "http://api.twitter.com/1/statuses/user_timeline.json?count=60";
    private static final String MENTIONS_REQUEST_URL      = "http://api.twitter.com/1/statuses/mentions.json?count=40";
    private static final String RETWEETS_OF_ME_URL        = "http://api.twitter.com/1/statuses/retweets_of_me.json";
    private static final String RETWEETED_BY_URL          = "http://api.twitter.com/1/statuses/:id/retweeted_by.json";
    
    private final String consumerKey    = CONSUMER_KEY;
    private final String consumerSecret = CONSUMER_SECRET;
    
    private OAuth oauth;
    
    private String token       = null;
    private String tokenSecret = null;
    
    public final boolean isAuthorized() {
        return FileUtil.exist(TOKEN_SAVE_FILE);
    }
    
    
    public final void authorizeRequest() throws NetException, IOException {
        oauth = new OAuth();
        oauth.setConsumer(consumerKey, consumerSecret);
        oauth.setProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);
        String url = oauth.retrieveRequestToken();
        
        // Browse an authorization page.
        SystemUtil.browse(new URL(url));
    }
    
    public final void authorize(String pin) throws NetException, IOException {
     // Retrieve an access token and a secret.
        oauth.retrieveAccessToken(pin);
        token       = oauth.getToken();
        tokenSecret = oauth.getTokenSecret();
        
        // Save tokens in a CSV file.
        saveAccessToken();
    }
    
    public final void sign(HTTP http) throws NetException {
        OAuth oauth = new OAuth();
        oauth.setConsumer(consumerKey, consumerSecret);
        oauth.setTokenWithSecret(token, tokenSecret);
        oauth.sign(http);
    }
    
    public final JsonStatus[] getUserTimeline() 
        throws IOException, ParserException, NetException {
        HTTP http = new HTTP(USER_TIMELINE_REQUEST_URL);
        sign(http);
        
        Reader reader = http.requestGet();
        JsonStatus[] statuses = new JSON().decode(reader, JsonStatus[].class);
        
        http.disconnect();
        reader.close();

        return statuses;
    }
    
    public final JsonStatus[] getMentions() 
        throws IOException, ParserException, NetException {
        HTTP http = new HTTP(MENTIONS_REQUEST_URL);
        sign(http);
        
        Reader reader = http.requestGet();
        JsonStatus[] mentions = new JSON().decode(reader, JsonStatus[].class);

        http.disconnect();
        reader.close();

        return mentions;
    }
    
    public final JsonStatus[] getRetweetsOfMe() 
        throws IOException, ParserException, NetException {
        HTTP http = new HTTP(RETWEETS_OF_ME_URL);
        sign(http);
        
        Reader reader = http.requestGet();
        JsonStatus[] statuses = new JSON().decode(reader, JsonStatus[].class);

        http.disconnect();
        reader.close();

        return statuses;
    }
    
    public final JsonUser[] getRetweetedBy(long id)
        throws IOException, ParserException, NetException {
        String url = RETWEETED_BY_URL.replaceAll(":id", String.valueOf(id));
        HTTP http = new HTTP(url);
        sign(http);
        
        Reader reader = http.requestGet();
        JsonUser[] users = new JSON().decode(reader, JsonUser[].class);

        http.disconnect();
        reader.close();

        return users;
    }
    
    private final void saveAccessToken() throws IOException {
        CSV csv = new CSV(new File(TOKEN_SAVE_FILE));
        csv.writeLine(token, tokenSecret);
        csv.close();
    }
    
    public final void loadAccessToken() throws IOException {
        CSV csv = new CSV(new File(TOKEN_SAVE_FILE));
        String[] line = csv.readLine();
        token       = line[0];
        tokenSecret = line[1];
        csv.close();
    }

    public final String getConsumerKey() {
        return consumerKey;
    }

    public final String getConsumerSecret() {
        return consumerSecret;
    }

    public final String getToken() {
        return token;
    }

    public final String getTokenSecret() {
        return tokenSecret;
    }
    
    public static final boolean isQT(StatusInterface status1, StatusInterface status2) {
        String t1 = status1.getText();
        String t2 = status2.getText();

        if (t1.equals(t2)) {
            return false;
        }

        if (lastIndexOfRTQT(t2) == -1) {
            return false;
        }

        long absElapse = Math.abs(status1.getCreatedAt().getTime() - status2.getCreatedAt().getTime());
        if (DateUtil.dayToMillis(1) < absElapse) {
            return false;
        }

        if (t1.length() <= 1) {
            return false;
        }

        int idx = t2.lastIndexOf(t1);
        if (idx != -1 && idx + t1.length() == t2.length()) {
            idx = lastIndexOfRTQT(t2.substring(0, idx));
            if (idx == -1) {
                return false;
            }
            idx = lastIndexOfRTQT(t2.substring(0, idx));
            if (idx == -1) {
                return true;
            }
        }

        return false;
    }
    
    private static final int lastIndexOfRTQT(String text) {
        int idx1 = text.lastIndexOf("RT");
        int idx2 = text.lastIndexOf("QT");

        if (idx1 < idx2)
            return idx2;
        else if (idx1 > idx2)
            return idx1;
        else
            return idx1;
    }
}
