package com.kii.cloud.board.cache;

import android.net.Uri;
import android.provider.BaseColumns;

public class TopicCache implements BaseColumns{
    public static final String AUTHORITY = "com.kii.cloud.board.topiccache";
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "");
    
    public static final String DATE = "date";
    public static final String NAME = "name";
    public static final String CREATOR_ID = "creator";
    public static final String CREATOR_NAME = "creator_name";
    public static final String UUID = "uuid";
    public static final String MESSAGE_COUNT = "message_count";

    public static final String DEFAULT_SORT_ORDER = DATE + " DESC";
}
