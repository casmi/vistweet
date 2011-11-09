package vistweet.data.json;

import java.util.Date;

public final class JsonStatus {

    public long id;

    public Date created_at;

    public String text;

    public long in_reply_to_status_id;

    public JsonUser user;
}
