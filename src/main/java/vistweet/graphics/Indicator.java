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

package vistweet.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import casmi.graphics.color.ColorSet;
import casmi.graphics.color.GrayColor;
import casmi.graphics.element.RoundRect;
import casmi.graphics.element.Text;
import casmi.graphics.element.TextAlign;
import casmi.graphics.font.Font;
import casmi.graphics.group.Group;

/**
 * @author T. Takeuchi
 */
public final class Indicator extends Group {

    private List<RoundRect> roundRectList = new ArrayList<RoundRect>();
    private RoundRect outerRoundRect;
    private Text message;
    
    private int[]   highlight = {-1, -1, -1};
    private boolean isStarting = false;
    
    private Timer timer;
    
    public Indicator() {
        super();        
        setup();
    }
    
    public void setup() {
        
        outerRoundRect = new RoundRect(10, 0, 0, 200, 160);
        outerRoundRect.setFillColor(new GrayColor(0.27, 0.8));
        outerRoundRect.setStroke(false);
        add(outerRoundRect);
        
        for (int i = 0; i < 12; ++i) {
            RoundRect el = new RoundRect(1, 13, 4);
            el.setStrokeColor(new GrayColor(0.25));
            add(el);
            roundRectList.add(el);
        }
        
        Font f = new Font();
        f.setSize(14);
        message = new Text(null, f, 0, -40);
        message.setStrokeColor(ColorSet.WHITE);
        message.setAlign(TextAlign.CENTER);
        add(message);
        
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
    public void update() {
        if (!isStarting) return;
        
        for (int i = 0; i < roundRectList.size(); ++i) {
            RoundRect el = roundRectList.get(i);
            
            if (i == highlight[0]) {
                el.setFillColor(new GrayColor(1.0));
            } else if (i == highlight[1]) {
                el.setFillColor(new GrayColor(0.8));
            } else if (i == highlight[2]) {
                el.setFillColor(new GrayColor(0.6));
            } else {
                el.setFillColor(new GrayColor(0.4));        
            }
            
            el.setX(18.0 * Math.cos(Math.toRadians((90.0 - i * 30.0))));
            el.setY(18.0 * Math.sin(Math.toRadians((90.0 - i * 30.0))) + 30.0);
            el.setRotation(90.0 - i * 30.0);
        }
    }
    
    public void start() {
        highlight[0] =  0;
        highlight[1] = -1;
        highlight[2] = -1;
        isStarting = true;
    }
    
    public void stop() {
        isStarting = false;
    }
    
    public void setMessage(String str) {
        if (str == null) str = "";
        message.setText(str);
    }
}
    
