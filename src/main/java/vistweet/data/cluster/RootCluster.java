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

package vistweet.data.cluster;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import vistweet.data.sql.StatusInterface;

public class RootCluster extends Cluster {

    private double x = 0.0, y = 0.0, z = 0.0;
    private double cox = 0.0, coy = 0.0, coz = 0.0;
    private int size = 1;
    
    public RootCluster(StatusInterface main) {

        super(main);
    }
    
    public RootCluster(StatusInterface main, Cluster[] refs) {

        super(main, refs);
    }
    
    public void setLocationAuto() {
        
        double r = 15.0;
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(main.getCreatedAt());
        double theta = Math.PI * 2.0 * 
            (1.0 / 24.0 / 60.0 / 60.0 * (double)calendar.get(Calendar.SECOND) + 
             1.0 / 24.0 / 60.0 * (double)calendar.get(Calendar.MINUTE) + 
             1.0 / 24.0 * (double)calendar.get(Calendar.HOUR_OF_DAY));

        double x = r * Math.sin(Math.PI / 2.0 - theta);
        double y = r * Math.cos(Math.PI / 2.0 - theta);
        double z = (new Date().getTime() - main.getCreatedAt().getTime()) / 1000000;
        
        setLocation(x, y, z);
    }
    
    public void setLocationOfRefsAuto() {
        
        for (int i = 0; i < refs.length; i++) {
            Cluster ref = refs[i];
            
            double radius = 100;
            double angle = Math.PI * 2.0 * i / refs.length + 20.0 * Math.PI / 180.0;
            double offsetX = radius * Math.cos(angle);
            double offsetY = radius * Math.sin(angle);

            ref.getMain().setX(offsetX);
            ref.getMain().setY(offsetY);

            cox += offsetX;
            coy += offsetY;
            size++;
            
            setLocationOfRefsAuto(ref);
        }
        
        cox /= size;
        coy /= size;
    }
    
    private void setLocationOfRefsAuto(Cluster cluster) {
        
        if (!cluster.hasRefs()) return;

        for (int i = 0; i < cluster.getRefs().length; i++) {
            Cluster ref = cluster.getRefs()[i];
            
            double radius = 100;
            double baseAngle = Math.atan2(cluster.getMain().getY() - main.getY(),
                    cluster.getMain().getX() - main.getX());
            double angle = baseAngle - Math.PI / 4.0 + Math.PI / 2.0 * (i + 1) / (cluster.getRefs().length + 1);
            double offsetX = radius * Math.cos(angle);
            double offsetY = radius * Math.sin(angle);

            ref.getMain().setX(cluster.getMain().getX() + offsetX);
            ref.getMain().setY(cluster.getMain().getY() + offsetY);

            cox += cluster.getMain().getX() + offsetX;
            coy += cluster.getMain().getY() + offsetY;
            size++;
            
            setLocationOfRefsAuto(ref);
        }
    }

    public void setLocation(double x, double y, double z) {

        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {

        return x;
    }

    public double getY() {

        return y;
    }

    public double getZ() {

        return z;
    }

    public double getCox() {
        return cox;
    }

    public double getCoy() {
        return coy;
    }

    public double getCoz() {
        return coz;
    }

    public int getSize() {
        return size;
    }
}
