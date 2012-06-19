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

import java.net.MalformedURLException;
import java.net.URL;

import vistweet.data.sql.StatusInterface;
import vistweet.net.Twitter;
import casmi.graphics.color.ColorSet;
import casmi.graphics.color.GrayColor;
import casmi.graphics.element.Rect;
import casmi.graphics.element.Text;
import casmi.graphics.element.TextAlign;
import casmi.graphics.element.TextBox;
import casmi.graphics.element.Texture;
import casmi.graphics.font.Font;
import casmi.graphics.group.Group;
import casmi.util.DateUtil;

/**
 * @author T. Takeuchi
 */
public final class DetailView extends Group {
    
    private static final double[] DEFAULT_SIZE = {270, 60}; 

    private StatusInterface status;
    private double width  = DEFAULT_SIZE[0];
    private double height = DEFAULT_SIZE[1];
    private String dateStr;
    
    private Rect    rect;
    private Text    text, timeText;
    private TextBox textBox;
    private Texture icon;
    
    public DetailView() {
        super();
        setup();
    }
    
    public void setup() {
        
        rect = new Rect(0, 0, width, height);
        rect.setFillColor(new GrayColor(0.4));
        rect.setStrokeColor(ColorSet.WHITE);
        rect.setStrokeWidth(1);
        add(rect);
        
        Font f = new Font();
        f.setSize(12);
        text = new Text(null, f);
        textBox = new TextBox(text, 20, 10, width - 45, height - 20);
        add(textBox);
        
        f.setSize(9);
        timeText = new Text(null, f);
        timeText.setStrokeColor(new GrayColor(0.4));
        timeText.setAlign(TextAlign.RIGHT);
        timeText.setX(width  / 2 - 5);
        timeText.setY(-height / 2 + 5);
        add(timeText);
        
        add(icon);
    }
    
    @Override
    public void update() {
        
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
