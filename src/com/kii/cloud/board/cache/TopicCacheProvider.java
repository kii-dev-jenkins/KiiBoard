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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class TopicCacheProvider extends ContentProvider {

    private static final String DATABASE_NAME = "board.db";
    private static final int DATABASE_VERSION = 3;

    private ThreadDatabaseHelper mHelper;

    private static final UriMatcher URL_MATCHER;

    private static final int TOPICS = 1;
    private static final int TOPIC_ID = 2;

    static {
        URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URL_MATCHER.addURI(TopicCache.AUTHORITY, "/", TOPICS);
        URL_MATCHER.addURI(TopicCache.AUTHORITY, "#", TOPIC_ID);
    }

    private static final String TOPIC_TABLE = "topics";

    private static class ThreadDatabaseHelper extends SQLiteOpenHelper {
        // Context mContext;
        public ThreadDatabaseHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
            // mContext = ctx;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create sdcard database
            db.execSQL("CREATE TABLE " + TOPIC_TABLE + " ("
                    + "_id INTEGER PRIMARY KEY autoincrement, "
                    + TopicCache.DATE + " INTEGER, "
                    + TopicCache.MESSAGE_COUNT + " INTEGER, "
                    + TopicCache.NAME + " TEXT, "
                    + TopicCache.UUID + " TEXT, "
                    + TopicCache.CREATOR_ID + " TEXT, "
                    + TopicCache.CREATOR_NAME + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            switch (oldVersion) {
                default:
                    db.execSQL("DROP TABLE IF EXISTS settings;");
                    onCreate(db);
                    break;
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count;
        String myWhere;

        switch (URL_MATCHER.match(uri)) {
            case TOPICS:
                count = db.delete(TOPIC_TABLE, selection, selectionArgs);
                break;

            case TOPIC_ID:
                myWhere = "_id="
                        + uri.getPathSegments().get(0)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                                + ")" : "");
                count = db.delete(TOPIC_TABLE, myWhere, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URL_MATCHER.match(uri)) {
            case TOPICS:
                return "vnd.android.cursor.dir/vnd.kii.cloud.board.topic";
            case TOPIC_ID:
                return "vnd.android.cursor.item/vnd.kii.cloud.board.topic";
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        long rowID;
        ContentValues values;

        if (initialValues != null)
            values = new ContentValues(initialValues);
        else
            throw new SQLException("Failed to insert row for NULL value");

        Uri res;

        if (URL_MATCHER.match(uri) == TOPICS) {

            rowID = db.insert(TOPIC_TABLE, TopicCache.UUID, values);
            res = ContentUris.withAppendedId(TopicCache.CONTENT_URI, rowID);
        } else {
            throw new IllegalArgumentException("Unknown URL " + uri);
        }

        if (rowID > 0) {
            assert (res != null);
            getContext().getContentResolver().notifyChange(res, null);
            return res;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        mHelper = new ThreadDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String defaultSort = TopicCache.DEFAULT_SORT_ORDER;

        switch (URL_MATCHER.match(uri)) {
            case TOPICS:
                qb.setTables(TOPIC_TABLE);
                break;
            case TOPIC_ID:
                qb.setTables(TOPIC_TABLE);
                qb.appendWhere("_id=" + uri.getPathSegments().get(0));
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        String orderBy;

        if (TextUtils.isEmpty(sortOrder))
            orderBy = defaultSort;
        else
            orderBy = sortOrder;

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, orderBy);

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int count;
        String myWhere;

        switch (URL_MATCHER.match(uri)) {
            case TOPICS:
                count = db.update(TOPIC_TABLE, values, selection,
                        selectionArgs);
                break;

            case TOPIC_ID:
                myWhere = "_id="
                        + uri.getPathSegments().get(0)
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                                + ")" : "");
                count = db.update(TOPIC_TABLE, values, myWhere, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
