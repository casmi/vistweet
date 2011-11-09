package vistweet.graphics;

import java.net.MalformedURLException;
import java.net.URL;

import vistweet.data.sql.StatusInterface;
import vistweet.net.Twitter;
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
import casmi.util.DateUtil;

public final class InfoGroup extends Group {
    
    private static final double[] DEFAULT_SIZE = {270, 60}; 

    private StatusInterface status;
    private double width  = DEFAULT_SIZE[0];
    private double height = DEFAULT_SIZE[1];
    private String dateStr;
    
    private Rect    rect;
    private Text    text, timeText;
    private TextBox textBox;
    private Texture icon;
    
    public InfoGroup(double x, double y) {
        this.x = x;
        this.y = y;
        setup();
    }
    
    @Override
    public void setup() {
        rect = new Rect(0, 0, width, height);
        rect.setFillColor(new Color(200));
        rect.setStrokeColor(ColorSet.WHITE);
        rect.setStrokeWidth(1);
        
        Font f = new Font();
        f.setSize(12);
        text = new Text(null, f);
        textBox = new TextBox(text, 20, 10, width - 45, height - 20);
        
        f.setSize(9);
        timeText = new Text(null, f);
        timeText.setStrokeColor(new Color(100));
        timeText.setAlign(TextAlign.RIGHT);
        timeText.setX(width  / 2 - 5);
        timeText.setY(-height / 2 + 5);
    }
    
    @Override
    public void draw(Graphics g) {
        if (status == null) return;
        
        g.render(rect);
        g.render(textBox);
        g.render(timeText);
        g.render(icon);
    }

    public final StatusInterface getStutas() {
        return status;
    }

    public final void setStutas(StatusInterface stutas) {
        this.status = stutas;
        
        dateStr = DateUtil.format(status.getCreatedAt(), "yyyy-MM-dd HH:mm");
        timeText.setText(dateStr);
        
        text.setText(status.getText());
        textBox.setText(text);
        
        try {
            icon = new Texture(new URL(status.getProfileImageURL()));
        } catch (Exception e) {
            try {
                icon = new Texture(new URL(Twitter.DEFAULT_ICON_URL));
            } catch (MalformedURLException e1) {
                // ignore
            }
        }
        icon.set(-this.width / 2 + 20, 10, 30, 30);
    }

    public final double getWidth() {
        return width;
    }

    public final double getHeight() {
        return height;
    }
}
