/*
 *   vistweet
 *   https://github.com/casmi/vistweet
 *   Copyright (C) 2011, Xcoo, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

@Tablename("user_timeline")
public final class UserTimeline extends Entity implements StatusInterface {

    @PrimaryKey
    @Fieldname("status_id")
    private long statusID;

    @Fieldname("created_at")
    private Date createdAt;

    private String text;

    @Fieldname("in_reply_to_status_id")
    private long inReplyToStatusID;

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
    private KindOfStatus kind = KindOfStatus.NORMAL;
    
    @Ignore
    private Texture texture = null;
    
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
    public long getStatusID() {
        return statusID;
    }

    @Override
    public void setStatusID(long statusID) {
        this.statusID = statusID;
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

    @Override
    public String toString() {
        return "@" + screenname + ": " + text;
    }
}
