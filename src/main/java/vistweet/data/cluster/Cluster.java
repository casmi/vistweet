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

import vistweet.data.sql.StatusInterface;

public class Cluster {

    protected final StatusInterface main;
    protected Cluster[] refs = null;

    public Cluster(StatusInterface main) {

        this(main, null);
    }

    public Cluster(StatusInterface main, Cluster[] refs) {

        this.main = main;
        this.refs = refs;
    }
    
    public boolean hasRefs() {
        
        return !(refs == null || refs.length == 0); 
    }

    public StatusInterface getMain() {

        return main;
    }

    public Cluster[] getRefs() {

        return refs;
    }

    public void setRefs(Cluster[] refs) {

        this.refs = refs;
    }

    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder(main.toString());
        return recursiveToString(this, sb, 0);
    }
    
    private String recursiveToString(Cluster cluster, StringBuilder sb, int depth) {
        
        if (!cluster.hasRefs()) {
            return null;
        }
        
        for (Cluster ref : cluster.getRefs()) {
            sb.append('\n');
            for (int i = 0; i < depth + 1; i++) {
                sb.append("  ");
            }
            sb.append(ref.getMain());
            recursiveToString(ref, sb, depth + 1);
        }
        
        return sb.toString();
    }
}
