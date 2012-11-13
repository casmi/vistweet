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

import vistweet.data.cluster.Cluster;
import vistweet.data.cluster.RootCluster;
import vistweet.data.sql.StatusInterface;
import casmi.graphics.color.ColorSet;
import casmi.graphics.element.Line;
import casmi.graphics.element.Rect;
import casmi.graphics.element.Text;
import casmi.graphics.element.Texture;
import casmi.graphics.font.Font;
import casmi.graphics.font.FontStyle;
import casmi.graphics.group.Group;

/**
 * @author T. Takeuchi
 */
public final class VizGroup extends Group {

    private static final double ICON_SIZE = 30;
    
    private RootCluster root = null;
    
    public void setup() {
    
        if (root == null) return;
        
        // calculate location of cluster
        root.setLocationOfRefsAuto();
        
        clear();
        
        setupLine();
        setupIcon();
    }
    
    private void setupIcon() {
        // tweet text
        String str = "";
        if (12 < root.getMain().getText().length()) {
            str = root.getMain().getText().substring(0, 12) + "...";
        } else if (0 < root.getMain().getText().length()) {
            str = root.getMain().getText();
        }
        Text text = new Text(str, new Font("Default", FontStyle.PLAIN, 13.0));
        text.setPosition(25.0 - root.getCox(), - 2.0 - root.getCoy());
        text.setStrokeColor(ColorSet.WHITE, this.getSceneStrokeColor().getAlpha());
        add(text);
        
        // rect
        Rect rect = new Rect(ICON_SIZE, ICON_SIZE);
        rect.setStrokeColor(ColorSet.YELLOW, this.getSceneStrokeColor().getAlpha());
        rect.setStroke(true);
        rect.setFill(false);
        rect.setX(-root.getCox());
        rect.setY(-root.getCoy());
        add(rect);
        
        // icon
        Texture icon = root.getMain().getTexture();
        icon.set(-root.getCox(), -root.getCoy(), ICON_SIZE, ICON_SIZE);
//        icon.getFillColor().setAlpha(this.getSceneFillColor().getAlpha());
        add(icon);
        
        setupIconRecursively(root);
    }
    
    private void setupIconRecursively(Cluster cluster) {
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
            Text text = new Text(str, new Font("Default", FontStyle.PLAIN, 13.0));
            text.setText(str);
            text.setX(st.getX() + 25 - root.getCox());
            text.setY(st.getY() - 2  - root.getCoy());
            text.setStrokeColor(ColorSet.WHITE, this.getSceneStrokeColor().getAlpha());
            add(text);

            // rect
            Rect rect = new Rect(ICON_SIZE, ICON_SIZE);
            switch (st.getKind()) {
            case REPLY:
                rect.setStrokeColor(ColorSet.RED, this.getSceneStrokeColor().getAlpha());
                rect.setStroke(true);
                break;
            case QT:
                rect.setStrokeColor(ColorSet.BLUE, this.getSceneStrokeColor().getAlpha());
                rect.setStroke(true);
                break;
            case RT:
                rect.setStrokeColor(ColorSet.GREEN, this.getSceneStrokeColor().getAlpha());
                rect.setStroke(true);
                break;
            default:
                rect.setStroke(false);
            }
            rect.setFill(false);
            rect.setX(st.getX() - root.getCox());
            rect.setY(st.getY() - root.getCoy());
            add(rect);
            
            // icon
            Texture icon = st.getTexture();
            if (icon != null) {
                icon.set(st.getX() - root.getCox(), st.getY() - root.getCoy(), ICON_SIZE, ICON_SIZE);
                icon.getFillColor().setAlpha(this.getSceneFillColor().getAlpha());
                add(icon);
            }
            
            setupIconRecursively(ref);
        }
    }
    
    private void setupLine() {   
        setupLineRecursively(root);
    }
    
    private void setupLineRecursively(Cluster cluster) {
        if (!cluster.hasRefs()) return;
        
        for (Cluster ref : cluster.getRefs()) {
            StatusInterface st = ref.getMain();
            
            Line line = new Line();
            line.setStrokeWidth(1.0);
            
            switch (st.getKind()) {
            case REPLY:
                line.setStrokeColor(ColorSet.RED, this.getSceneStrokeColor().getAlpha());
                break;
            case QT:
                line.setStrokeColor(ColorSet.BLUE, this.getSceneStrokeColor().getAlpha());
                break;
            case RT:
                line.setStrokeColor(ColorSet.GREEN, this.getSceneStrokeColor().getAlpha());
                break;
            default:
                line.setStrokeColor(ColorSet.WHITE, this.getSceneStrokeColor().getAlpha());
                break;
            }
            line.set(cluster.getMain().getX() - root.getCox(), cluster.getMain().getY() - root.getCoy(),
                     st.getX() - root.getCox(), st.getY() - root.getCoy());
            add(line);
            
            setupLineRecursively(ref);
        }
    }
    
    @Override
    public void update() {}
    
    public void setRootCluster(RootCluster cluster) {
        this.root = cluster;
        setup();
    }
}
