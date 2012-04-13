//
//
//  Copyright 2012 Kii Corporation
//  http://kii.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//

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
    public static final String URI = "uri";
    public static final String MESSAGE_COUNT = "message_count";

    public static final String DEFAULT_SORT_ORDER = DATE + " DESC";
}
