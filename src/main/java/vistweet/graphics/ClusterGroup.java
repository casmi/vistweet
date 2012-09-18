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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import vistweet.data.cluster.Cluster;
import casmi.graphics.color.Color;
import casmi.graphics.color.ColorSet;
import casmi.graphics.element.Rect;
import casmi.graphics.element.Text;
import casmi.graphics.element.TextAlign;
import casmi.graphics.element.TextBox;
import casmi.graphics.element.Texture;
import casmi.graphics.font.Font;
import casmi.graphics.font.FontStyle;
import casmi.graphics.group.Group;

/**
 * @author T. Takeuchi
 */
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
        super();
        this.cluster = cluster;
        setup();
    }
    
    public void setup() {

        rect = new Rect(width, height);
        add(rect);
        
        Font f = new Font("Default", FontStyle.PLAIN, 12.0);
        Text t = new Text(cluster.getMain().getText(), f);
        t.setStrokeColor(ColorSet.WHITE);
        textBox = new TextBox(t, 
                              0.0, height / 2.0 - TEXTBOX_SIZE[1] / 2.0,
                              TEXTBOX_SIZE[0], TEXTBOX_SIZE[1]);
        add(textBox);
        
        height = ICON_SIZE + textBox.getHeight();        
        
        generateIconList(cluster);
        
        int i = 0;
        for (Texture icon : iconList) {
            icon.set(- width / 2 + ICON_SIZE * (0.5 + i) + 5,
                     - height / 2 + ICON_SIZE / 2 + 5,
                     ICON_SIZE,
                     ICON_SIZE);
            add(icon);
            i++;
        }
    }
    
    @Override
    public void update() {}
    
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
