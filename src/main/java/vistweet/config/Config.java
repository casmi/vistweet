/*
 *   vistweet
 *   https://github.com/casmi/vistweet
 *   Copyright (C) 2012, Xcoo, Inc.
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

package vistweet.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration class.
 * <p>
 * This class is singleton.
 * It reads a configuration file (e.g. config.properties).
 * <p>
 * Usage:
 * <code>
 * Config.getInstance().getHost()
 * </code>
 * 
 * @author T. Takeuchi
 */
public class Config {
    
    private static final String TWITTER_CONSUMER_KEY_KEY     = "twitter.consumer_key";
    private static final String TWITTER_CONSUMER_SECRET_KEY  = "twitter.consumer_secret";  
      
    private static Config instance = new Config();
    
    private Properties properties = new Properties();
    
    private Config() {}
    
    public static Config getInstance() {
        return instance;
    }
    
    public void load(String path) {
        InputStream is = null;
        try {
            is = new FileInputStream(path);
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }        
    }
    
    public String getTwitterConsumerKey() {
        return properties.getProperty(TWITTER_CONSUMER_KEY_KEY);
    }
    
    public String getTwitterConsumerSecret() {
        return properties.getProperty(TWITTER_CONSUMER_SECRET_KEY);
    }
}
