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
import vistweet.graphics.ClusterGroup;
import casmi.exception.NetException;
import casmi.exception.ParserException;
import casmi.sql.Query;
import casmi.sql.SQLite;

public final class Request implements Runnable {

    private final Twitter twitter;
    private final SQLite  sqlite;
    
    private UserTimeline[] userTimelines = null;
    private Mention[]      mentions      = null;
    
    private final List<Cluster> clusterList = new CopyOnWriteArrayList<Cluster>();

    public Request(Twitter twitter, SQLite sqlite) {
        super();
        this.twitter = twitter;
        this.sqlite  = sqlite;
    }

    @Override
    public void run() {
        Vistweet.getIndicator().start();
        Vistweet.getIndicator().setMessage("Loading tweet...");
        
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
        
        Vistweet.getIndicator().setMessage("Analyzing tweet...");

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
            
            List<ClusterGroup> cgList = new CopyOnWriteArrayList<ClusterGroup>();
            for (Cluster cluster : Vistweet.getClusterList()) {
                ClusterGroup ce = new ClusterGroup(cluster);
                cgList.add(ce);
            }
            Vistweet.setClusterGroupList(cgList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        Vistweet.getIndicator().stop();
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
        
        Vistweet.setClusterList(clusterList);
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
