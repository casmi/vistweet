package vistweet.graphics;

import vistweet.data.cluster.Cluster;
import vistweet.data.cluster.RootCluster;
import vistweet.data.sql.StatusInterface;
import casmi.graphics.Graphics;
import casmi.graphics.color.ColorSet;
import casmi.graphics.element.Line;
import casmi.graphics.element.Rect;
import casmi.graphics.element.Text;
import casmi.graphics.element.Texture;
import casmi.graphics.font.Font;
import casmi.graphics.group.Group;

public final class VizGroup extends Group {

    private static final double ICON_SIZE = 30;
    
    private final RootCluster root;
    
    private Text text;
    private Line line = new Line();
    private Rect rect = new Rect(ICON_SIZE, ICON_SIZE);
    
    public VizGroup(RootCluster cluster, double x, double y) {
        this.root = cluster;
        this.x = x;
        this.y = y;
        setup();
    }
    
    @Override
    public void setup() {
        Font f = new Font();
        f.setSize(13);
        text = new Text("", f);
        text.setStrokeColor(ColorSet.WHITE);
        
        rect.setFill(false);
        
        line.setStrokeColor(ColorSet.WHITE);
        line.setStrokeWidth(1);
        
        // calculate location of cluster
        root.setLocationOfRefsAuto();
    }
    
    @Override
    public void draw(Graphics g) {
        drawLine(g);
        drawIcon(g);
    }
    
    private final void drawIcon(Graphics g) {
        // tweet text
        String str = "";
        if (12 < root.getMain().getText().length()) {
            str = root.getMain().getText().substring(0, 12) + "...";
        } else if (0 < root.getMain().getText().length()) {
            str = root.getMain().getText();
        }
        text.setText(str);
        text.setX(25 - root.getCox());
        text.setY(-2 - root.getCoy());
        text.setStrokeColor(ColorSet.WHITE, this.getSceneStrokeColor().getA());
        
        // icon
        root.getMain().getTexture().set(-root.getCox(), -root.getCoy(), ICON_SIZE, ICON_SIZE);
        root.getMain().getTexture().getFillColor().setA(this.getSceneFillColor().getA());
        
        // rect
        rect.setStrokeColor(ColorSet.YELLOW, this.getSceneStrokeColor().getA());
        rect.setStroke(true);
        rect.setX(-root.getCox());
        rect.setY(-root.getCoy());
        
        // rendering
        g.render(text);
        g.render(root.getMain().getTexture());
        g.render(rect);

        recursiveDrawIcon(g, root);
    }
    
    private final void recursiveDrawIcon(Graphics g, Cluster cluster) {
        if (!cluster.hasRefs()) return;
        
        for (Cluster ref : cluster.getRefs()) {
            StatusInterface st = ref.getMain();
            
            // tweet text
            String str;
            if (15 < st.getText().length()) {
                str = st.getText().substring(0, 15) + "...";
            } else {
                str = st.getText();
            }           
            text.setText(str);
            text.setX(st.getX() + 25 - root.getCox());
            text.setY(st.getY() - 2  - root.getCoy());
            text.setStrokeColor(ColorSet.WHITE, this.getSceneStrokeColor().getA());
            
            // icon
            Texture icon = st.getTexture();
            if (icon != null) {
                icon.set(st.getX() - root.getCox(), st.getY() - root.getCoy(), ICON_SIZE, ICON_SIZE);
                icon.getFillColor().setA(this.getSceneFillColor().getA());
                g.render(icon);
            }

            // rect
            switch (st.getKind()) {
            case REPLY:
                rect.setStrokeColor(ColorSet.RED, this.getSceneStrokeColor().getA());
                rect.setStroke(true);
                break;
            case QT:
                rect.setStrokeColor(ColorSet.BLUE, this.getSceneStrokeColor().getA());
                rect.setStroke(true);
                break;
            case RT:
                rect.setStrokeColor(ColorSet.GREEN, this.getSceneStrokeColor().getA());
                rect.setStroke(true);
                break;
            default:
                rect.setStroke(false);
            }
            rect.setX(st.getX() - root.getCox());
            rect.setY(st.getY() - root.getCoy());
            
            // render
            g.render(text);
            g.render(rect);
            
            recursiveDrawIcon(g, ref);
        }
    }
    
    private final void drawLine(Graphics g) {   
        recursiveDrawLine(g, root);
    }
    
    private final void recursiveDrawLine(Graphics g, Cluster cluster) {
        if (!cluster.hasRefs()) return;
        
        for (Cluster ref : cluster.getRefs()) {
            StatusInterface st = ref.getMain();
            
            switch (st.getKind()) {
            case REPLY:
                line.setStrokeColor(ColorSet.RED, this.getSceneStrokeColor().getA());
                break;
            case QT:
                line.setStrokeColor(ColorSet.BLUE, this.getSceneStrokeColor().getA());
                break;
            case RT:
                line.setStrokeColor(ColorSet.GREEN, this.getSceneStrokeColor().getA());
                break;
            default:
                line.setStrokeColor(ColorSet.WHITE, this.getSceneStrokeColor().getA());
            }
            line.set(cluster.getMain().getX() - root.getCox(), cluster.getMain().getY() - root.getCoy(),
                     st.getX() - root.getCox(), st.getY() - root.getCoy());
            g.render(line);
            
            recursiveDrawLine(g, ref);
        }
    }
}
