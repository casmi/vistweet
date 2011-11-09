package vistweet.graphics;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import vistweet.data.cluster.Cluster;
import casmi.graphics.Graphics;
import casmi.graphics.color.Color;
import casmi.graphics.color.ColorSet;
import casmi.graphics.element.Rect;
import casmi.graphics.element.Text;
import casmi.graphics.element.TextAlign;
import casmi.graphics.element.TextBox;
import casmi.graphics.element.Texture;
import casmi.graphics.font.Font;
import casmi.graphics.group.Group;

public final class ClusterGroup extends Group {

    private static final double   DEFAULT_WIDTH  = 350;
    private static final double   DEFAULT_HEIGHT = 90;
    private static final double   ICON_SIZE      = 20;
    private static final double[] TEXTBOX_SIZE   = {340, 60};
    
    private final Cluster cluster;
    
    private double  width  = DEFAULT_WIDTH;
    private double  height = DEFAULT_HEIGHT;
    private TextBox textBox;
    private Rect    rect;
    private List<Texture> iconList = new ArrayList<Texture>();
    
    public ClusterGroup(Cluster cluster) {
        this.cluster = cluster;
        setup();
    }
    
    @Override
    public void setup() {
        Font f = new Font();
        f.setSize(12);
        Text t = new Text(cluster.getMain().getText(), f);
        t.setStrokeColor(Color.color(ColorSet.WHITE));
        t.setAlign(TextAlign.LEFT);
        textBox = new TextBox(t, 0, 0, TEXTBOX_SIZE[0], TEXTBOX_SIZE[1]);
        height = ICON_SIZE + textBox.getHeight();
        
        rect = new Rect(width, height);
        
        generateIconList(cluster);
    }
    
    @Override
    public void draw(Graphics g) {
    }

    public void draw(Graphics g, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            rect.setFillColor(new Color(150));
        }
        rect.setX(x);
        rect.setY(y);
        
        textBox.setX(x);
        textBox.setY(y + height / 2 - TEXTBOX_SIZE[1] / 2);
        
        g.render(rect);
        g.render(textBox);

        drawIcons(g);
    }
    
    public boolean isMouseOver(int mouseX, int mouseY) {
        if (x - width  / 2 < mouseX && mouseX < x + width  / 2 &&
            y - height / 2 < mouseY && mouseY < y + height / 2) {
            return true;
        }
        
        return false;
    }
    
    private final void drawIcons(Graphics g) {
        int i = 0;
        for (Texture icon : iconList) {
            icon.set(x - width / 2 + ICON_SIZE * (0.5 + i) + 5, y - height / 2 + ICON_SIZE / 2 + 5,
                     ICON_SIZE, ICON_SIZE);
            g.render(icon);
            i++;
        }
    }
    
    private final void generateIconList(Cluster cluster) {
        if (!cluster.hasRefs()) return;
        
        for (Cluster ref : cluster.getRefs()) {
            try {
                iconList.add(new Texture(new URL(ref.getMain().getProfileImageURL())));
            } catch (Exception e) {
                // ignore
            }
            generateIconList(ref);
        }
    }
    
    public final void setRectFillColor(Color color) {
        rect.setFillColor(color);
    }
    
    public Cluster getCluster() {
        return cluster;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
}
