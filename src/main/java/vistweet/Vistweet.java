package vistweet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import vistweet.data.cluster.Cluster;
import vistweet.data.cluster.RootCluster;
import vistweet.graphics.ClusterGroup;
import vistweet.graphics.Indicator;
import vistweet.graphics.InfoGroup;
import vistweet.graphics.PINFrame;
import vistweet.graphics.VizGroup;
import vistweet.net.Request;
import vistweet.net.Twitter;
import casmi.Applet;
import casmi.AppletRunner;
import casmi.graphics.Graphics;
import casmi.graphics.color.Color;
import casmi.graphics.color.ColorSet;
import casmi.graphics.element.MouseOver;
import casmi.graphics.element.Rect;
import casmi.graphics.element.Texture;
import casmi.graphics.element.Triangle;
import casmi.sql.SQLite;
import casmi.tween.Tween;
import casmi.tween.TweenElement;
import casmi.tween.TweenManager;
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
    
    private Twitter twitter;
    private SQLite  sqlite;
    private Cron    cron;
    
    private static List<Cluster> clusterList = new CopyOnWriteArrayList<Cluster>();
    
    private static List<ClusterGroup> cgList = new CopyOnWriteArrayList<ClusterGroup>();
    private int listTop = 0;
    
    private PINFrame pinFrame;
    private boolean  isAuthFinished = false;
    
    private static Indicator indicator;
    
    private VizGroup       vizGroup;
    private float          visGroupScale = 1;
    private InfoGroup      infoGroup;
    private TweenElement   tweenElement;  
    private TweenManager   manager = new TweenManager();
    
    private Triangle  upTriangle,   downTriangle;
    private MouseOver moUpTriangle, moDownTriangle;
    
    private Texture   reloadButton, plusButton, minusButton;
    private Rect      reloadRect,   plusRect,   minusRect;
    private MouseOver moReload,     moPlus,     moMinus;

    @Override
    public void setup() {
        // set properties of Applet
        setSize(1152, 680);
        setFPS(15);
        
        // create a window to input PIN code
        pinFrame = new PINFrame();
        pinFrame.addActionListenerToButton(this);
        
        // initialize elements and groups
        infoGroup = new InfoGroup(getWidth() - 145, 40);

        upTriangle   = new Triangle(180, getHeight() - 5, 160, getHeight() - 17, 200, getHeight() - 17);
        downTriangle = new Triangle(180, 5, 160, 17, 200, 17);
        upTriangle.setFillColor(ColorSet.GRAY);
        downTriangle.setFillColor(ColorSet.GRAY);
        moUpTriangle   = new MouseOver(upTriangle);
        moDownTriangle = new MouseOver(downTriangle);
        
        reloadButton = new Texture(Vistweet.class.getResource("reload_icon.png").getPath());
        reloadButton.set(getWidth()  - 25, getHeight() - 25, 40, 40);
        reloadRect = new Rect(getWidth() - 25, getHeight() - 25, 40, 40);
        moReload = new MouseOver(reloadRect);
        
        plusButton = new Texture(Vistweet.class.getResource("scale_icon_plus.png").getPath());
        plusButton.set(getWidth()  - 105, getHeight() - 25, 40, 40);
        plusRect = new Rect(getWidth() - 105, getHeight() - 25, 40, 40);
        moPlus = new MouseOver(plusRect);
        
        minusButton = new Texture(Vistweet.class.getResource("scale_icon_minus.png").getPath());
        minusButton.set(getWidth()  - 65, getHeight() - 25, 40, 40);
        minusRect = new Rect(getWidth() - 64, getHeight() - 25, 40, 40);
        moMinus = new MouseOver(minusRect);
        
        indicator = new Indicator(getWidth() / 2, getHeight() / 2);
        
        // initialize Twitter instance and authorize a user's account
        twitter = new Twitter();
        try {
            if (twitter.isAuthorized()) {
                twitter.loadAccessToken();
            } else {
                twitter.authorizeRequest();
                pinFrame.setVisible(true);
                while(!isAuthFinished) Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
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
        
        // load tweets every 5 minutes
        cron = new Cron("*/5 * * * *", new Request(twitter, sqlite));
        cron.start();
    }

    @Override
    public void draw(Graphics g) {
        // exit by ESC
        if (isKeyPressed() && getKeycode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
        
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
        for (int i = begin; i < end; i++) {
            ClusterGroup cg = cgList.get(cgList.size() - 1 - i);
            
            Color c = (i % 2 == 0 ? new Color(40) : new Color(60));
            cg.setRectFillColor(c);
            cg.setX(cg.getWidth() / 2);
            cg.setY(getHeight() - cg.getHeight() / 2 - (i - listTop) * cg.getHeight() - 25);
            cg.draw(g, getMouseX(), getMouseY());
            
            if (isMouseClicked() && cg.isMouseOver(getMouseX(), getMouseY()) &&
                20 < getMouseY() && getMouseY() < getHeight() - 20) {
                double width  = getWidth() - cg.getWidth();
                double height = getHeight();
                double x = width  / 2 + cg.getWidth() + 5;
                double y = height / 2;
                
                vizGroup = null;
                vizGroup = new VizGroup((RootCluster)cg.getCluster(), x, y);
                vizGroup.setSceneAlpha(0);
                visGroupScale = 1;
                
                tweenElement = null;
                tweenElement = new TweenElement(vizGroup);
                Tween.to(tweenElement, TweenElement.ALPHA, 500, Linear.INOUT).target(255.0f).addToManager(manager);
                    
                infoGroup.setStutas(cg.getCluster().getMain());
            }
        }
        
        // triangles
        if (moUpTriangle.isMouseOver(getMouseX(), getMouseY()) && 0 < listTop) {
            upTriangle.setFillColor(ColorSet.WHITE);
            if (isMouseClicked()) listTop--;
        } else {
            upTriangle.setFillColor(ColorSet.GRAY);
        }
        int topNum;
        if (cgList.isEmpty()) {
            topNum = 0;
        } else {
            topNum = cgList.size() - (int)(getHeight() / (cgList.get(0).getHeight() + 10));
        }
        if (moDownTriangle.isMouseOver(getMouseX(), getMouseY()) && listTop < topNum) {
            downTriangle.setFillColor(ColorSet.WHITE);
            if (isMouseClicked()) listTop++;
        } else {
            downTriangle.setFillColor(ColorSet.GRAY);
        }
        
        // reload button
        if (moReload.isMouseOver(getMouseX(), getMouseY())) {
            reloadButton.setFillColor(ColorSet.WHITE);
            if (isMouseClicked()) cron.exec();
        } else {
            reloadButton.setFillColor(ColorSet.GRAY);
        }
        
        // plus button
        if (moPlus.isMouseOver(getMouseX(), getMouseY())) {
            plusButton.setFillColor(ColorSet.WHITE);
            if (isMouseClicked()) {
                visGroupScale += 0.1f;
                Tween.to(tweenElement, TweenElement.Scale, 300, Linear.INOUT).target(visGroupScale).addToManager(manager);
            }
        } else {
            plusButton.setFillColor(ColorSet.GRAY);
        }
        
        // minus button
        if (moMinus.isMouseOver(getMouseX(), getMouseY())) {
            minusButton.setFillColor(ColorSet.WHITE);
            if (isMouseClicked()) {
                visGroupScale -= 0.1f;
                Tween.to(tweenElement, TweenElement.Scale, 300, Linear.INOUT).target(visGroupScale).addToManager(manager);
            }
        } else {
            minusButton.setFillColor(ColorSet.GRAY);
        }
        
        // rendering
        if (vizGroup != null) {
            g.render(manager);
            g.render(vizGroup);
        }
        g.render(infoGroup);
        g.render(indicator);
        g.render(plusButton);
        g.render(minusButton);
        g.render(reloadButton);
        if (listTop != 0) g.render(moUpTriangle);
        if (listTop != topNum - 1) g.render(moDownTriangle);
    }

    public static final List<Cluster> getClusterList() {
        return new CopyOnWriteArrayList<Cluster>(clusterList);
    }
    
    public static final void setClusterList(List<Cluster> list) {
        clusterList = list;
    }
    
    public static final List<ClusterGroup> getClusterGroupList() {
        return new CopyOnWriteArrayList<ClusterGroup>(cgList);
    }
    
    public static final void setClusterGroupList(List<ClusterGroup> list) {
        cgList = list;
    }
    
    public static final Indicator getIndicator() {
        return indicator;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            twitter.authorize(pinFrame.getPIN());
            pinFrame.setVisible(false);
            isAuthFinished = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static final void main(String[] args) {
        AppletRunner.run("vistweet.Vistweet", "vistweet: a visualization tool for Twitter");
    }
}
