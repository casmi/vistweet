package vistweet.data.sql;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import vistweet.data.KindOfStatus;
import vistweet.net.Twitter;
import casmi.graphics.element.Texture;
import casmi.sql.Entity;
import casmi.sql.annotation.Fieldname;
import casmi.sql.annotation.Ignore;
import casmi.sql.annotation.PrimaryKey;
import casmi.sql.annotation.Tablename;

@Tablename("retweet")
public final class Retweet extends Entity implements StatusInterface {

    @PrimaryKey
    private String id;
    
    @Fieldname("status_id")
    private long statusID;
    
    @Fieldname("created_at")
    private Date createdAt;

    private String text;
    
    @Fieldname("in_reply_to_status_id")
    private long inReplyToStatusID = 0;
    
    @Fieldname("user_id")
    private long userID;
    
    @Fieldname("screen_name")
    private String screenname;
    
    @Fieldname("profile_image_url")
    private String profileImageURL;

    @Ignore
    private double x = 0, y = 0;
    
    @Ignore
    private boolean isEnabled = true;
    
    @Ignore
    private KindOfStatus kind = KindOfStatus.RT;
    
    @Ignore
    private Texture texture = null;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public long getStatusID() {
        return statusID;
    }

    @Override
    public void setStatusID(long statusID) {
        this.statusID = statusID;
    }

    @Override
    public long getUserID() {
        return userID;
    }

    @Override
    public void setUserID(long userID) {
        this.userID = userID;
    }

    @Override
    public String getScreenname() {
        return screenname;
    }

    @Override
    public void setScreenname(String screenname) {
        this.screenname = screenname;
    }

    @Override
    public String getProfileImageURL() {
        return profileImageURL;
    }

    @Override
    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    @Override
    public void loadTexture() {
        try {
            texture = new Texture(new URL(profileImageURL));
        } catch (Exception e) {
            try {
                texture = new Texture(new URL(Twitter.DEFAULT_ICON_URL));
            } catch (MalformedURLException e1) {
                // ignore
            }
        }
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public long getInReplyToStatusID() {
        return inReplyToStatusID;
    }

    @Override
    public void setInReplyToStatusID(long inReplyToStatusID) {
        this.inReplyToStatusID = inReplyToStatusID;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public KindOfStatus getKind() {
        return kind;
    }

    @Override
    public void setKind(KindOfStatus kind) {
        this.kind = kind;
    }

    @Override
    public Texture getTexture() {
        return texture;
    }
}
