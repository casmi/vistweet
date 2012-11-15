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

package vistweet.net;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import vistweet.Vistweet;
import vistweet.data.KindOfStatus;
import vistweet.data.cluster.Cluster;
import vistweet.data.cluster.RootCluster;
import vistweet.data.json.JsonStatus;
import vistweet.data.json.JsonUser;
import vistweet.data.sql.Mention;
import vistweet.data.sql.Retweet;
import vistweet.data.sql.StatusInterface;
import vistweet.data.sql.UserTimeline;
import vistweet.graphics.ClusterView;
import vistweet.graphics.Indicator;
import casmi.exception.NetException;
import casmi.exception.ParserException;
import casmi.sql.Query;
import casmi.sql.SQLite;

public final class Request implements Runnable {

    private final Vistweet vistweet;
    private final Twitter twitter;
    private final SQLite sqlite;
    
    private UserTimeline[] userTimelines = null;
    private Mention[]      mentions      = null;
    
    private final List<Cluster> clusterList = new CopyOnWriteArrayList<Cluster>();

    public Request(Vistweet vistweet, Twitter twitter, SQLite sqlite) {
        super();
        this.vistweet = vistweet; 
        this.twitter = twitter;
        this.sqlite = sqlite;
    }

    @Override
    public void run() {
        
        Indicator indicator = vistweet.getIndicator();
        indicator.visible();
        indicator.start();
        indicator.setMessage("Loading tweets...");
///*        
        try {
            loadUserTimeline();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            loadMentions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            loadRetweets();
        } catch (Exception e) {
            e.printStackTrace();
        }
//*/        
        indicator.setMessage("Analyzing tweets...");

        try {
            userTimelines = sqlite.all(UserTimeline.class, new Query().order("created_at"));
            mentions      = sqlite.all(Mention.class,      new Query().order("created_at"));
            
            // Load icons
            for (UserTimeline ut : userTimelines) {
                ut.loadTexture();
            }
            for (Mention mt : mentions) {
                mt.loadTexture();
            }
            
            addCluster();
            
            List<ClusterView> cgList = new CopyOnWriteArrayList<ClusterView>();

            for (Cluster cluster : clusterList) {
                ClusterView cg = new ClusterView(cluster);
                cgList.add(cg);
            }
            vistweet.setClusterGroupList(cgList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        indicator.stop();
        indicator.hidden();
    }
    
    private final void loadUserTimeline() 
        throws IOException, NetException, ParserException {
        JsonStatus[] sts = twitter.getUserTimeline();
        
        for (JsonStatus st : sts) {
            UserTimeline ut = sqlite.entity(UserTimeline.class);
            ut.setStatusID(st.id);
            ut.setCreatedAt(st.created_at);
            ut.setText(st.text);
            ut.setInReplyToStatusID(st.in_reply_to_status_id);
            ut.setUserID(st.user.id);
            ut.setScreenname(st.user.screen_name);
            ut.setProfileImageURL(st.user.profile_image_url.toString());
            try {
                ut.save();
            } catch (SQLException e) {
                if (e.getMessage().indexOf("PRIMARY KEY") == -1) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final void loadMentions()
        throws IOException, NetException, ParserException {
        JsonStatus[] sts = twitter.getMentions();
        
        for (JsonStatus st : sts) {
            Mention mt = sqlite.entity(Mention.class);
            mt.setStatusID(st.id);
            mt.setCreatedAt(st.created_at);
            mt.setText(st.text);
            mt.setInReplyToStatusID(st.in_reply_to_status_id);
            mt.setUserID(st.user.id);
            mt.setScreenname(st.user.screen_name);
            mt.setProfileImageURL(st.user.profile_image_url.toString());
            try {
                mt.save();
            } catch (SQLException e) {
                if (e.getMessage().indexOf("PRIMARY KEY") == -1) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private final void loadRetweets()
        throws ParserException, NetException, IOException {
        JsonStatus[] sts = twitter.getRetweetsOfMe();
        
        for (JsonStatus st : sts) {
            UserTimeline ut = sqlite.entity(UserTimeline.class);
            ut.setStatusID(st.id);
            ut.setCreatedAt(st.created_at);
            ut.setText(st.text);
            ut.setInReplyToStatusID(st.in_reply_to_status_id);
            ut.setUserID(st.user.id);
            ut.setScreenname(st.user.screen_name);
            ut.setProfileImageURL(st.user.profile_image_url.toString());
            try {
                ut.save();
            } catch (SQLException e) {
                if (e.getMessage().indexOf("PRIMARY KEY") == -1) {
                    e.printStackTrace();
                }
            }
            
            JsonUser[] users = twitter.getRetweetedBy(st.id);
            for (JsonUser user : users) {
                Retweet rt = sqlite.entity(Retweet.class);
                rt.setId(String.valueOf(st.id) + String.valueOf(user.id));
                rt.setStatusID(st.id);
                rt.setCreatedAt(st.created_at);
                rt.setText(st.text);
                rt.setUserID(user.id);
                rt.setScreenname(user.screen_name);
                rt.setProfileImageURL(user.profile_image_url.toString());
                try {
                    rt.save();
                } catch (SQLException e) {
                    if (e.getMessage().indexOf("PRIMARY KEY") == -1 &&
                        e.getMessage().indexOf("not unique")  == -1) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private final void addCluster() {
        clusterList.clear();
        
        for (UserTimeline ut : userTimelines) {
            if (!ut.isEnabled()) continue;
            
            List<Cluster> refs = new ArrayList<Cluster>();

            // search in mentions
            for (Mention mt : mentions) {
                if (mt.getInReplyToStatusID() == ut.getStatusID()) {
                    mt.setKind(KindOfStatus.REPLY);
                    mt.setEnabled(false);
                    refs.add(new Cluster(mt));
                } else if (Twitter.isQT(ut, mt)) {
                    mt.setKind(KindOfStatus.QT);
                    mt.setEnabled(false);
                    refs.add(new Cluster(mt));
                }
            }

            // search in retweets
            try {
                Retweet[] retweets = sqlite.all(Retweet.class, new Query().where("status_id=" + ut.getStatusID()));
                for (Retweet rt : retweets) {
                    rt.loadTexture();
                    refs.add(new Cluster(rt));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            // add recursively
            if (!refs.isEmpty()) {
                ut.setEnabled(false);
                Cluster cluster = new RootCluster(ut, refs.toArray(new Cluster[refs.size()]));
                recursiveAddCluster(cluster);
                clusterList.add(cluster);
            }
        }
        
        vistweet.setClusterList(clusterList);
    }

    private final void recursiveAddCluster(Cluster cluster) {
        if (!cluster.hasRefs()) return;

        try {
            for (Cluster ref : cluster.getRefs()) {
                if (ref.getMain().getKind() == KindOfStatus.RT) continue;
                
                List<Cluster> newRefs = new ArrayList<Cluster>();
                StatusInterface status = ref.getMain();

                for (Mention mt : mentions) {
                    if (!mt.isEnabled()) continue;
                    
                    if (mt.getInReplyToStatusID() == status.getStatusID()) {
                        mt.setKind(KindOfStatus.REPLY);
                        mt.setEnabled(false);
                        newRefs.add(new Cluster(mt));
                    } else if (Twitter.isQT(status, mt)) {
                        mt.setKind(KindOfStatus.QT);
                        mt.setEnabled(false);
                        newRefs.add(new Cluster(mt));
                    }
                }

                for (UserTimeline ut : userTimelines) {
                    if (!ut.isEnabled()) continue;
                    
                    if (ut.getInReplyToStatusID() == status.getStatusID()) {
                        newRefs.add(new Cluster(ut));
                        ut.setEnabled(false);
                        ut.setKind(KindOfStatus.REPLY);
                    }
                }

                ref.setRefs(newRefs.toArray(new Cluster[newRefs.size()]));
                recursiveAddCluster(ref);
            }
        } catch (StackOverflowError e) {
            e.printStackTrace();
        }
    }
}
