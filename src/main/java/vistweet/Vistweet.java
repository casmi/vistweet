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

package vistweet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import vistweet.data.cluster.Cluster;
import vistweet.data.cluster.RootCluster;
import vistweet.graphics.ClusterGroup;
import vistweet.graphics.Indicator;
import vistweet.graphics.DetailView;
import vistweet.graphics.PINFrame;
import vistweet.graphics.VizGroup;
import vistweet.net.Request;
import vistweet.net.Twitter;
import casmi.Applet;
import casmi.AppletRunner;
import casmi.KeyEvent;
import casmi.MouseButton;
import casmi.MouseEvent;
import casmi.graphics.color.Color;
import casmi.graphics.color.ColorSet;
import casmi.graphics.color.GrayColor;
import casmi.graphics.element.Element;
import casmi.graphics.element.MouseClickCallback;
import casmi.graphics.element.MouseOverCallback;
import casmi.graphics.element.Texture;
import casmi.graphics.element.Triangle;
import casmi.sql.SQLite;
import casmi.tween.Tween;
import casmi.tween.TweenElement;
import casmi.tween.TweenType;
import casmi.tween.equations.Linear;
import casmi.util.Cron;
import casmi.util.FileUtil;
import casmi.util.SystemUtil;

/**
 * "vistweet" is a visualizing tool of references among Twitter.
 * 
 * <p>
 * A Color of lines and icons expresses a kind of reference.
 * Yellow shows your root tweet, red shows a reply to the root tweet, green shows
 * RT (official retweet), and blue shows QT (quote tweet: unofficial retweet). 
 * 
 * @author T. Takeuchi
 */
public final class Vistweet extends Applet implements ActionListener {

    private static final String DB_PATH = SystemUtil.JAVA_TMP_PATH + "vistweet.sqlite";
    
    private static final String RELOAD_ICON_PATH      = Vistweet.class.getResource("reload_icon.png").getPath();
    private static final String SCALE_ICON_PLUS_PATH  = Vistweet.class.getResource("scale_icon_plus.png").getPath();
    private static final String SCALE_ICON_MINUS_PATH = Vistweet.class.getResource("scale_icon_minus.png").getPath();
    
    private Twitter twitter;
    private SQLite  sqlite;
    private Cron    cron;
    
    private List<Cluster> clusterList = new CopyOnWriteArrayList<Cluster>();
    
    private List<ClusterGroup> cgList = new CopyOnWriteArrayList<ClusterGroup>();
    private int listTop = 0;
    
    private PINFrame pinFrame;
    
    private Indicator indicator;
    
    private VizGroup     vizGroup;
    private float        visGroupScale = 1.0f;
    private DetailView   detailView;
    private TweenElement tweenElement;  
    
    private Triangle upArrow, downArrow;
    private Texture  reloadButton, plusButton, minusButton;

    @Override
    public void setup() {
        
        // set properties of Applet
        setSize(1152, 680);
//        setFPS(15);
        
        // Setup elements and groups
        detailView = new DetailView();
        detailView.setPosition(getWidth() - 145.0,  40.0);
        addObject(detailView);

        setupArrows();
        
        setupButtons();
        
        vizGroup = new VizGroup();
        addObject(vizGroup);
        
        indicator = new Indicator();
        indicator.setPosition(getWidth() / 2.0,  getHeight() / 2.0);
        indicator.hidden();
        addObject(indicator);
        
        // initialize SQLite instance
        try {
            if (!FileUtil.exist(DB_PATH)) {
                SQLite.createDatabase(DB_PATH);
            }
            sqlite = new SQLite(DB_PATH);
            sqlite.connect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        twitter = new Twitter();
        cron = new Cron("*/10 * * * *", new Request(this, twitter, sqlite));
        
        // Authorize a user's account
        try {
            if (twitter.isAuthorized()) {
                twitter.loadAccessToken();
                cron.start();
            } else {
                twitter.authorizeRequest();
                
                // create a window to input PIN code
                pinFrame = new PINFrame();
                pinFrame.addActionListenerToButton(this);
                pinFrame.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void setupArrows() {
        upArrow = new Triangle(180, getHeight() - 5, 160, getHeight() - 17, 200, getHeight() - 17);
        upArrow.setFillColor(ColorSet.GRAY);
        upArrow.addMouseEventCallback(new MouseOverCallback() {
            
            @Override
            public void run(MouseOverTypes eventtype, Element element) {
                switch (eventtype) {
                case ENTERED:
                case EXISTED:
                    upArrow.setFillColor(ColorSet.WHITE);
                    break;
                case EXITED:
                default:
                    upArrow.setFillColor(ColorSet.GRAY);
                    break;
                }
            }
        });
        upArrow.addMouseEventCallback(new MouseClickCallback() {
            
            @Override
            public void run(MouseClickTypes eventtype, Element element) {
                if (eventtype == MouseClickTypes.CLICKED) {
                    listTop--;
                }
            }
        });
        addObject(upArrow);
        
        downArrow = new Triangle(180, 5, 160, 17, 200, 17);
        downArrow.setFillColor(ColorSet.GRAY);
        downArrow.addMouseEventCallback(new MouseOverCallback() {
            
            @Override
            public void run(MouseOverTypes eventtype, Element element) {
                switch (eventtype) {
                case ENTERED:
                case EXISTED:
                    downArrow.setFillColor(ColorSet.WHITE);
                    break;
                case EXITED:
                default:
                    downArrow.setFillColor(ColorSet.GRAY);
                    break;
                }
            }
        });
        downArrow.addMouseEventCallback(new MouseClickCallback() {
            
            @Override
            public void run(MouseClickTypes eventtype, Element element) {
                if (eventtype == MouseClickTypes.CLICKED) {
                    listTop++;
                }
            }
        });
        addObject(downArrow);
    }
    
    private void setupButtons() {
        reloadButton = new Texture(RELOAD_ICON_PATH);
        reloadButton.set(getWidth()  - 25, getHeight() - 25, 40, 40);
        reloadButton.setFillColor(ColorSet.GRAY);
        reloadButton.addMouseEventCallback(new MouseOverCallback() {
            
            @Override
            public void run(MouseOverTypes eventtype, Element element) {
                switch (eventtype) {
                case ENTERED:
                case EXISTED:
                    reloadButton.setFillColor(ColorSet.WHITE);
                    break;
                case EXITED:
                default:
                    reloadButton.setFillColor(ColorSet.GRAY);
                    break;
                }
            }
        });
        reloadButton.addMouseEventCallback(new MouseClickCallback() {
            
            @Override
            public void run(MouseClickTypes eventtype, Element element) {
                if (eventtype == MouseClickTypes.CLICKED) {
                    cron.exec();
                }
            }
        });
        addObject(reloadButton);
        
        plusButton = new Texture(SCALE_ICON_PLUS_PATH);
        plusButton.set(getWidth()  - 105, getHeight() - 25, 40, 40);
        plusButton.setFillColor(ColorSet.GRAY);
        plusButton.addMouseEventCallback(new MouseOverCallback() {
    
            @Override
            public void run(MouseOverTypes eventtype, Element element) {
                switch (eventtype) {
                case ENTERED:
                case EXISTED:
                    plusButton.setFillColor(ColorSet.WHITE);
                    break;
                case EXITED:
                default:
                    plusButton.setFillColor(ColorSet.GRAY);
                    break;
                }
            }
        });
        plusButton.addMouseEventCallback(new MouseClickCallback() {
            
            @Override
            public void run(MouseClickTypes eventtype, Element element) {
                if (eventtype == MouseClickTypes.CLICKED) {
                    visGroupScale += 0.1f;
                    Tween tween = Tween.to(tweenElement, TweenType.SCALE, 300, Linear.INOUT).target(visGroupScale);
                    addTween(tween);
                }
            }
        });
        addObject(plusButton);
        
        minusButton = new Texture(SCALE_ICON_MINUS_PATH);
        minusButton.set(getWidth() - 65, getHeight() - 25, 40, 40);
        minusButton.setFillColor(ColorSet.GRAY);
        minusButton.addMouseEventCallback(new MouseOverCallback() {
    
            @Override
            public void run(MouseOverTypes eventtype, Element element) {
                switch (eventtype) {
                case ENTERED:
                case EXISTED:
                    minusButton.setFillColor(ColorSet.WHITE);
                    break;
                case EXITED:
                default:
                    minusButton.setFillColor(ColorSet.GRAY);
                    break;
                }
            }
        });
        minusButton.addMouseEventCallback(new MouseClickCallback() {
            
            @Override
            public void run(MouseClickTypes eventtype, Element element) {
                if (eventtype == MouseClickTypes.CLICKED) {
                    visGroupScale -= 0.1f;
                    Tween tween = Tween.to(tweenElement, TweenType.SCALE, 300, Linear.INOUT).target(visGroupScale);
                    addTween(tween);
                }
            }
        });
        addObject(minusButton);
    }
    
    @Override
    public void update() {
        
        // calculate begin and end of list in the window
        int begin = (0 < listTop ? listTop - 1 : 0);
        int end   = begin;
        int totalHeight = 0;
        for (int i = begin; i < cgList.size(); i++) {
            ClusterGroup cg = cgList.get(cgList.size() - 1 - i);
            totalHeight += cg.getHeight();
            if (getHeight() - 40 < totalHeight) {
                end = i + 2;
                break;
            }
        }
        if (cgList.size() < end) end = cgList.size();
        
        // ClusterGroup
        for (int i = begin; i < end; ++i) {
            final ClusterGroup cg = cgList.get(cgList.size() - 1 - i);
            
            Color c = (i % 2 == 0 ? new GrayColor(0.18) : new GrayColor(0.21));
            cg.setRectFillColor(c);
            cg.setPosition(cg.getWidth() / 2.0,
                           getHeight() - cg.getHeight() / 2.0 - (i - listTop) * (cg.getHeight() + 10.0) - 25.0);
            
            
            cg.addMouseEventCallback(new MouseClickCallback() {
                
                @Override
                public void run(MouseClickTypes eventtype, Element element) {
                    if (eventtype == MouseClickTypes.CLICKED &&
                        20 < getMouseY() && getMouseY() < getHeight() - 20) {

                        double width  = getWidth() - cg.getWidth();
                        double height = getHeight();
                        double x = width  / 2 + cg.getWidth() + 5;
                        double y = height / 2;

                        vizGroup.setRootCluster((RootCluster)cg.getCluster());
                        vizGroup.setPosition(x, y);
                        vizGroup.setSceneAlpha(0);
                        visGroupScale = 1.0f;

                        tweenElement = null;
                        tweenElement = new TweenElement(vizGroup);
                        Tween tween = Tween.to(tweenElement, TweenType.ALPHA, 500, Linear.INOUT).target(255.0f);
                        addTween(tween);

                        detailView.setStutas(cg.getCluster().getMain());
                    }
                }
            });
        }
        
        int topNum;
        if (cgList.isEmpty()) {
            topNum = 0;
        } else {
            topNum = cgList.size() - (int)(getHeight() / (cgList.get(0).getHeight() + 10));
        }
        
        if (listTop == 0) {
            upArrow.hidden();
        } else if (listTop == topNum - 1) {
            downArrow.hidden();
        } else {
            upArrow.visible();
            downArrow.visible();
        }
    }

    public List<Cluster> getClusterList() {
        return new CopyOnWriteArrayList<Cluster>(clusterList);
    }
    
    public void setClusterList(List<Cluster> list) {
        clusterList = list;
    }
    
    public List<ClusterGroup> getClusterGroupList() {
        return new CopyOnWriteArrayList<ClusterGroup>(cgList);
    }
    
    public void setClusterGroupList(List<ClusterGroup> list) {
        
        if (cgList != null) {
            for (ClusterGroup el : cgList) {
                el.remove();
            }
        }
        
        cgList = list;
        
        for (ClusterGroup el : cgList) {
            addObject(el);
        }
    }
    
    public Indicator getIndicator() {
        return indicator;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            twitter.authorize(pinFrame.getPIN());
            pinFrame.setVisible(false);
            authFinished();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void authFinished() {
        // load tweets every 5 minutes
        cron = new Cron("*/5 * * * *", new Request(this, twitter, sqlite));
        cron.start();
    }

    @Override
    public void mouseEvent(MouseEvent e, MouseButton b) {}

    @Override
    public void keyEvent(KeyEvent e) {
        // exit by ESC
        if (e == KeyEvent.PRESSED && getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }
    
    public static final void main(String[] args) {
        AppletRunner.run("vistweet.Vistweet", "vistweet: a visualization tool for Twitter");
    }
    
}
