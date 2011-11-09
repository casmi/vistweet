package vistweet.graphics;

import java.util.Timer;
import java.util.TimerTask;

import casmi.graphics.Graphics;
import casmi.graphics.color.Color;
import casmi.graphics.color.ColorSet;
import casmi.graphics.element.RoundRect;
import casmi.graphics.element.Text;
import casmi.graphics.element.TextAlign;
import casmi.graphics.font.Font;
import casmi.graphics.group.Group;

public final class Indicator extends Group {

    private RoundRect roundRect;
    private RoundRect outerRoundRect;
    private Text      message;
    private int[]     highlight = {-1, -1, -1};
    private boolean   isStarting = false;
    private Timer     timer;
    
    public Indicator(double x, double y) {
        this.x = x;
        this.y = y;
        setup();
    }
    
    @Override
    public void setup() {
        roundRect = new RoundRect(1, 15, 4.5);
        roundRect.setStrokeColor(new Color(60));
        
        outerRoundRect = new RoundRect(10, 0, 0, 200, 160);
        outerRoundRect.setFillColor(new Color(70, 200));
        outerRoundRect.setStroke(false);
        
        Font f = new Font();
        f.setSize(14);
        message = new Text(null, f, 0, -40);
        message.setStrokeColor(ColorSet.WHITE);
        message.setAlign(TextAlign.CENTER);
        
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (11 < highlight[0]++) highlight[0] = 0;
                if (11 < highlight[1]++) highlight[1] = 0;
                if (11 < highlight[2]++) highlight[2] = 0;                
            }
        }, 0, 90);
    }
    
    @Override
    public void draw(Graphics g) {
        if (!isStarting) return;
        
        g.render(outerRoundRect);
        
        for (int i = 0; i < 12; i++) {
            if (i == highlight[0]) {
                roundRect.setFillColor(new Color(255));
            } else if (i == highlight[1]) {
                roundRect.setFillColor(new Color(200));
            } else if (i == highlight[2]) {
                roundRect.setFillColor(new Color(150));
            } else {
                roundRect.setFillColor(new Color(100));        
            }
            
            roundRect.setX(17 * Math.cos(Math.toRadians((90.0 - i * 30.0))));
            roundRect.setY(17 * Math.sin(Math.toRadians((90.0 - i * 30.0))) + 30);
            roundRect.setRotate(90.0 - i * 30.0);
            g.render(roundRect);
        }
        
        g.render(message);
    }
    
    public final void start() {
        highlight[0] =  0;
        highlight[1] = -1;
        highlight[2] = -1;
        isStarting = true;
    }
    
    public final void stop() {
        isStarting = false;
    }
    
    public final void setMessage(String str) {
        if (str == null) str = "";
        message.setText(str);
    }
}
    
