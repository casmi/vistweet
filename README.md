# About vistweet

## Abstract
"vistweet" is a application to visualize relations among tweets on Twitter.
First, you have to authorize Vital Atlas on Twitter and input PIN code.
It reads and analyzes recent tweets when you click a reload button at upper right (or every 5 minutes). 
When you click a timeline at left, a cluster that consists of your tweet and relational tweets (reply/RT) will be shown.
A red line and icon shows "reply," a green shows "official retweet (RT)," and a blue shows "unofficial retweet (QT: quote tweet)."
You can change the scale by using plus/minus buttons at upper right.

## Inside of Program
1. It reads a timeline of Twitter using Twitter API every 5 minutes (Request.java, Twitter.java).
2. It parse downloaded JSON data and stores to JSON objects (JsonStatus.java, JsonUser.java).
3. Then, it maps them on O/R mapping, and insert to a SQLite database (UserTimeline/Mention/Retweet.java).  
4. After that, it analyzes relations of tweets (reply, RT, QT), and generates a cluster (RootCluster/Cluster.java).
5. It draws a timeline that represents a source tweet and user icons of relative tweets at left in the window.
6. When you click a tweet in the timeline, it visualizes relations of tweets at right space in the window. 

- For Twitter authentication, it uses casmi.extension.net.OAuth
- For reading tweets via Twitter API, it uses casmi.net.HTTP
- For parsing downloaded data, it uses casmi.parser.JSON
- For using a SQLite database, it uses casmi.sql.SQLite/Entity
- For regular requests, it uses casmi.util.Cron
- For drawing a timeline, it uses casmi.graphics.element.Rect/TextBox/Texture
- For drawing a relation of tweets, it uses casmi.element.Line/Text/Texture
- For grouping each components, it uses casmi.graphics.group.Group