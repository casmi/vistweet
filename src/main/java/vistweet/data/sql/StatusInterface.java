package vistweet.data.sql;

import java.util.Date;

import vistweet.data.KindOfStatus;

import casmi.graphics.element.Texture;


public interface StatusInterface {

    void loadTexture();
    
    long getStatusID();
    void setStatusID(long statusID);
    
    Date getCreatedAt();
    void setCreatedAt(Date createdAt);
    
    String getText();
    void setText(String text);
    
    long getInReplyToStatusID();
    void setInReplyToStatusID(long inReplyToStatusID);
    
    long getUserID();
    void setUserID(long userID);
    
    String getScreenname();
    void setScreenname(String screenname);
    
    String getProfileImageURL();
    void setProfileImageURL(String profileImageURL);
    
    boolean isEnabled();
    void setEnabled(boolean isEnabled);

    double getX();
    void setX(double x);

    double getY();
    void setY(double y);

    KindOfStatus getKind();
    void setKind(KindOfStatus kind);

    Texture getTexture();
}
