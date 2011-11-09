package vistweet.data;

/**
 * Kinds of a tweet.
 * 
 * @author T. Takeuchi
 */
public enum KindOfStatus {

    /** An usual tweet. */
    NORMAL, 
    
    /** A reply. */
    REPLY, 
    
    /** An official retweet. */
    RT,
    
    /** An unofficial retweet (quote tweet). */
    QT
}
