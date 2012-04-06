//
//
// Copyright 2012 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//

package com.kii.cloud.board;

import java.text.DateFormat;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.cloud.board.cache.TopicCache;
import com.kii.cloud.board.sdk.Constants;
import com.kii.cloud.board.sdk.KiiBoardClient;
import com.kii.cloud.board.sdk.Message;
import com.kii.cloud.board.sdk.Topic;
import com.kii.cloud.board.utils.AdsUtil;
import com.kii.cloud.board.utils.ProgressingDialog;
import com.kii.cloud.board.utils.Utils;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.query.KQExp;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

public class RemoteMessageListActivity extends ListActivity {
    private ChatListAdaptor mAdapter;
    private Cursor mCursor;
    ProgressingDialog progressing;
    private TextView mUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.remote_thread);

        mUpdateTime = (TextView) findViewById(R.id.header_text);

        ListView lv = getListView();
        lv.setFastScrollEnabled(false);
        lv.setTextFilterEnabled(true);
        lv.setOnCreateContextMenuListener(this);

        mAdapter = (ChatListAdaptor) getLastNonConfigurationInstance();
        if (mAdapter == null) {
            mAdapter = new ChatListAdaptor(getApplication(), this,
                    R.layout.chat_list_item, mCursor, new String[] {},
                    new int[] {});
            setListAdapter(mAdapter);
            setTitle(R.string.app_name);
            getListCursor(mAdapter.getQueryHandler(), null);
        } else {
            mAdapter.setActivity(this);
            setListAdapter(mAdapter);
            mCursor = mAdapter.getCursor();
            if (mCursor != null) {
                init(mCursor);
            } else {
                getListCursor(mAdapter.getQueryHandler(), null);
            }
        }

        progressing = new ProgressingDialog(this);
        updateRefreshTime(false);
        Intent intent = getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)
                && action.contentEquals(Constants.ACTION_REFRESH)) {
            handleRefresh(null);
        }
		AdsUtil.addToLayout(this, R.id.main_remote, AdsUtil.getKiiAdsLayout(
				this, Constants.APP_ID, Constants.APP_KEY));
    }

    private static final int MENU_DELETE = 0;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        if (item.getItemId() == MENU_DELETE) {
            progressing.showProcessing(0, "Deleting Thread...");
            mCursor.moveToPosition(info.position);
            int idx = mCursor.getColumnIndex(TopicCache.UUID);
            final String uuid = mCursor.getString(idx);
            final String reference = KiiBoardClient.CONTAINER_TOPIC + "/"
                    + uuid;
            final Uri uri = ContentUris.withAppendedId(TopicCache.CONTENT_URI,
                    info.id);
            KiiQuery query = new KiiQuery();
            query.setWhere(KQExp.equals(Message.PROPERTY_TOPIC, reference));
            KiiObject
                    .deleteQuery(null, KiiBoardClient.CONTAINER_MESSAGE, query);
            KiiObject obj = KiiBoardClient.getKiiObjectByUuid(
                    KiiBoardClient.CONTAINER_TOPIC, uuid);
            obj.delete(new KiiObjectCallBack() {
                @Override
                public void onDeleteCompleted(int token, boolean success,
                        Exception exception) {
                    if (success) {
                        RemoteMessageListActivity.this.getContentResolver()
                                .delete(uri, null, null);
                    }
                    progressing.closeProgressDialog();
                    mAdapter.notifyDataSetChanged();
                }

            });

        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        menu.add(0, MENU_DELETE, 0, "Delete");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, RemoteConversationActivity.class);
        intent.putExtra(Utils.INTENT_TOPIC_ID, id);

        startActivity(intent);
    }

    public void init(Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    public void handleRefresh(View v) {
        progressing.showProcessing(0, "Refreshing Topic info...");
        mQuery = new KiiQuery();
        mQuery.setLimit(25);
        KiiObject.query(mCallBack, KiiBoardClient.CONTAINER_TOPIC, mQuery);
    }

    private KiiQuery mQuery;
    KiiObjectCallBack mCallBack = new KiiObjectCallBack() {
        @Override
        public void onQueryCompleted(int token, boolean success,
                KiiQueryResult<KiiObject> objects, Exception exception) {
            progressing.closeProgressDialog();
            if (success) {
                List<KiiObject> result = objects.getResult();
                updateLocalDB(result);
                updateRefreshTime(true);
            }
        }
    };

    public static final String TAG = "RemoteMessageList";

    private void updateRefreshTime(boolean isUpdatePreference) {
        long time = System.currentTimeMillis();
        if (isUpdatePreference) {
            KiiBoardClient.setUpdateTime(this, time);
        } else {
            time = KiiBoardClient.getUpdateTime(this);
        }

        if (time > 0) {
            mUpdateTime.setText("Last refresh time:"
                    + Utils.formatTimeStampString(this, time, true));
        }
    }

    public void handleNewTopic(View v) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(
                R.layout.alert_dialog_text_entry, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle("Create new topic")
                .setView(textEntryView)
                .setPositiveButton("Create",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                TextView input = (TextView) textEntryView
                                        .findViewById(R.id.topic_edit);
                                String topic_name = input.getText().toString();
                                if (!TextUtils.isEmpty(topic_name)) {
                                    KiiObject topic = new KiiObject(
                                            KiiBoardClient.CONTAINER_TOPIC);
                                    topic.set(Topic.PROPERTY_NAME, topic_name);
                                    topic.set(Topic.PROPERTY_CREATOR,
                                            KiiBoardClient.getInstance()
                                                    .getloginUser()
                                                    .getUsername());

                                    progressing.showProcessing(0,
                                            "Creating topic...");
                                    topic.save(new KiiObjectCallBack() {
                                        @Override
                                        public void onSaveCompleted(int token,
                                                boolean success,
                                                KiiObject object,
                                                Exception exception) {
                                            progressing.closeProgressDialog();
                                            if (success) {
                                                KiiObject newTopic = (KiiObject) object;
                                                ContentValues values = new ContentValues();
                                                values.put(
                                                        TopicCache.CREATOR_ID,
                                                        newTopic.getString(Topic.PROPERTY_CREATOR));
                                                values.put(
                                                        TopicCache.NAME,
                                                        newTopic.getString(Topic.PROPERTY_NAME));
                                                values.put(
                                                        TopicCache.UUID,
                                                        newTopic.toUri()
                                                                .getLastPathSegment()
                                                                .toString());
                                                values.put(
                                                        TopicCache.DATE,
                                                        newTopic.getModifedTime());
                                                RemoteMessageListActivity.this
                                                        .getContentResolver()
                                                        .insert(TopicCache.CONTENT_URI,
                                                                values);
                                            } else {
                                                Toast.makeText(
                                                        RemoteMessageListActivity.this,
                                                        "create topic error!",
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }

                                        }

                                    });
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {

                            }
                        }).create();

        dialog.show();
    }

    private void updateLocalDB(List<KiiObject> entities) {
        getContentResolver().delete(TopicCache.CONTENT_URI, null, null);

        if (entities == null || entities.size() == 0)
            return;
        for (KiiObject obj : entities) {
            ContentValues values = new ContentValues();
            values.put(TopicCache.CREATOR_ID,
                    obj.getString(Topic.PROPERTY_CREATOR, ""));
            values.put(TopicCache.NAME, obj.getString(Topic.PROPERTY_NAME, ""));
            values.put(TopicCache.UUID, obj.toUri().getLastPathSegment());
            values.put(TopicCache.DATE, obj.getModifedTime());
            getContentResolver().insert(TopicCache.CONTENT_URI, values);
        }
        mCursor.requery();
        mAdapter.notifyDataSetChanged();
    }

    private Uri mUri = TopicCache.CONTENT_URI;

    public Cursor getListCursor(AsyncQueryHandler async, String filter) {
        Cursor ret = null;
        String selection = null;

        if (filter != null)
            selection = filter;

        if (async != null) {
            async.startQuery(0, null, mUri, null, selection, null, null);
        } else {
            ret = managedQuery(mUri, null, selection, null, null);
        }

        return ret;
    }

    public Cursor getListCursor() {
        return mCursor;
    }

    public void setListCursor(Cursor c) {
        mCursor = c;
    }

    static class ChatListAdaptor extends SimpleCursorAdapter {
        private AsyncQueryHandler mQueryHandler;
        private RemoteMessageListActivity mActivity;
        private int mTitleIdx;

        private boolean mConstraintIsValid = false;
        private String mConstraint = null;

        public class ViewHolder {
            public TextView line1;
            public TextView time;
        }

        class QueryHandler extends AsyncQueryHandler {
            QueryHandler(ContentResolver res) {
                super(res);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie,
                    Cursor cursor) {
                mActivity.init(cursor);
            }
        }

        public ChatListAdaptor(Context context,
                RemoteMessageListActivity currentactivity, int layout,
                Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            mActivity = currentactivity;
            mQueryHandler = new QueryHandler(context.getContentResolver());
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = super.newView(context, cursor, parent);
            ViewHolder vh = new ViewHolder();
            vh.line1 = (TextView) v.findViewById(R.id.line1);
            vh.time = (TextView) v.findViewById(R.id.time);
            v.setTag(vh);
            int position = cursor.getPosition();
            if (position % 2 == 0) {
                v.setBackgroundColor(Color.rgb(0xFA, 0xFA, 0xFA));
            } else {
                v.setBackgroundColor(Color.rgb(0xF7, 0xF7, 0xF7));
            }
            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder vh = (ViewHolder) view.getTag();
            String name = cursor.getString(mTitleIdx);
            vh.line1.setText(name);
            long time = cursor.getLong(cursor.getColumnIndex(TopicCache.DATE));
            vh.time.setText(DateUtils.formatSameDayTime(time / 1000,
                    System.currentTimeMillis(), DateFormat.DEFAULT,
                    DateFormat.DEFAULT));
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            String s = constraint.toString();
            if (mConstraintIsValid
                    && ((s == null && mConstraint == null) || (s != null && s
                            .equals(mConstraint)))) {
                return getCursor();
            }

            Cursor c = mActivity.getListCursor(null, s);
            mConstraint = s;
            mConstraintIsValid = true;
            return c;
        }

        public AsyncQueryHandler getQueryHandler() {
            return mQueryHandler;
        }

        public void setActivity(RemoteMessageListActivity newactivity) {
            mActivity = newactivity;
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor != mActivity.getListCursor()) {
                mActivity.setListCursor(cursor);
                getColumnIndices(cursor);
                super.changeCursor(cursor);
            }

        }

        private void getColumnIndices(Cursor cursor) {
            if (cursor != null) {
                mTitleIdx = cursor.getColumnIndexOrThrow(TopicCache.NAME);
            }
        }

    }
}
