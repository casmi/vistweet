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
