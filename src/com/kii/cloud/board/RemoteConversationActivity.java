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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kii.ad.KiiAdNetLayout;
import com.kii.cloud.board.cache.TopicCache;
import com.kii.cloud.board.sdk.Constants;
import com.kii.cloud.board.sdk.KiiBoardClient;
import com.kii.cloud.board.sdk.Message;
import com.kii.cloud.board.utils.AdsUtil;
import com.kii.cloud.board.utils.Utils;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.query.KQExp;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;

public class RemoteConversationActivity extends ListActivity {
    private long thread_id;
    private String mTopicUri;
    private ImageView mMoreButton;
    private RemoteSMSAdapter mAdapter;
    private KiiQuery mQuery;
    private EditText mMessageInput = null;
    private KiiAdNetLayout mAdLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.conversation);
        mAdLayout = AdsUtil.getKiiAdsLayout(this, Constants.APP_ID,
                Constants.APP_KEY);
        AdsUtil.addToLayout(this, R.id.main_conversation, mAdLayout);

        String topic_name = "";
        Intent intent = this.getIntent();
        if (intent != null) {
            thread_id = intent.getLongExtra(Utils.INTENT_TOPIC_ID, 0);
            Cursor c = null;

            try {
                Uri uri = ContentUris.withAppendedId(TopicCache.CONTENT_URI,
                        thread_id);
                String[] projection = new String[] { TopicCache.NAME,
                        TopicCache.URI };
                c = managedQuery(uri, projection, null, null,
                        Utils.DEFAULT_ORDER);

                if (c != null & c.getCount() > 0) {
                    c.moveToFirst();
                    topic_name = c.getString(0);
                    mTopicUri = c.getString(1);
                }
            } finally {
                if (c != null)
                    c.close();
            }
            mMoreButton = (ImageView) findViewById(R.id.header_more_button);
            mMoreButton.setEnabled(false);
        }

        TextView nameView = (TextView) this.findViewById(R.id.title);
        nameView.setText(topic_name);

        mMessageInput = (EditText) findViewById(R.id.message_field);

        ListView lv = getListView();
        lv.setFastScrollEnabled(false);
        lv.setTextFilterEnabled(false);

        mAdapter = new RemoteSMSAdapter(this);

        lv.setAdapter(mAdapter);
        getRemoteMessage();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.getListView().refreshDrawableState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdLayout = null;
    }

    public void handleMore(View v) {
        getRemoteMessage();
    }

    public void handleRefresh(View v) {
        mQuery = null;
        mAdapter.clearBodies();
        getRemoteMessage();
    }

    public void handleSendMessage(View v) {

        final String input = mMessageInput.getText().toString();
        mMessageInput.setEnabled(false);
        if (!TextUtils.isEmpty(input)) {
            // hide soft keyboard
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            KiiObject msg = new KiiObject(KiiBoardClient.CONTAINER_MESSAGE);
            msg.set(Message.PROPERTY_CONTENT, input);
            KiiUser user = KiiBoardClient.getInstance().getloginUser();
            msg.set(Message.PROPERTY_CREATOR, user.getUsername());
            msg.set(Message.PROPERTY_CREATOR_NAME, user.getEmail());
            msg.set(Message.PROPERTY_TOPIC, mTopicUri);
            msg.save(new KiiObjectCallBack() {
                @Override
                public void onSaveCompleted(int token, boolean success,
                        KiiObject object, Exception exception) {
                    mMessageInput.setEnabled(true);
                    if (success) {
                        mQuery = null;
                        mAdapter.clearBodies();
                        mMessageInput.clearComposingText();
                        mMessageInput.setText(null);
                        getRemoteMessage();
                    } else {
                        Toast.makeText(RemoteConversationActivity.this,
                                "create message error!", Toast.LENGTH_SHORT)
                                .show();
                    }

                }

            });

        }
    }

    private void getRemoteMessage() {
        if (mQuery == null) {
            mQuery = new KiiQuery();
            mQuery.setWhere(KQExp.equals(Message.PROPERTY_TOPIC, mTopicUri));
            mQuery.sortByAsc(Message.PROPERTY_CREATE_TIME);
            mQuery.setLimit(100);
        }
        KiiObject.query(mCallBack, KiiBoardClient.CONTAINER_MESSAGE, mQuery);
    }

    KiiObjectCallBack mCallBack = new KiiObjectCallBack() {
        @Override
        public void onQueryCompleted(int token, boolean success,
                KiiQueryResult<KiiObject> objects, Exception exception) {
            if (success) {
                mAdapter.addBodies(objects.getResult());
                mQuery = objects.getNextKiiQuery();
                mMoreButton.setEnabled(objects.hasNext());
            }
        }
    };

    private class RemoteSMSAdapter extends BaseAdapter {

        private Context mContext;
        private List<KiiObject> mBodies;
        private String mCurrentUser;

        public RemoteSMSAdapter(Context context) {
            mContext = context;
            mBodies = new ArrayList<KiiObject>();
            KiiUser user = KiiBoardClient.getInstance().getloginUser();
            if (user != null) {
                mCurrentUser = user.getEmail();
            }
        }

        public void clearBodies() {
            mBodies.clear();
            notifyDataSetChanged();
        }

        public void addBodies(List<KiiObject> list) {
            if (list == null)
                return;
            for (KiiObject body : list) {
                mBodies.add(0, body);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mBodies.size();
        }

        @Override
        public Object getItem(int position) {
            return mBodies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = ((Activity) mContext).getLayoutInflater().inflate(
                        R.layout.conversation_list_item, parent, false);
                setViewHolder(view);
            }

            ViewHolder vh = (ViewHolder) view.getTag();
            String lastCreator = null;
            KiiObject message = mBodies.get(position);

            if (position >= 1) {
                KiiObject lastMsg = mBodies.get(position - 1);
                lastCreator = lastMsg.getString(Message.PROPERTY_CREATOR_NAME)
                        .toLowerCase();
                vh.empty.setVisibility(View.VISIBLE);
            } else {
                vh.empty.setVisibility(View.GONE);
            }
            String text = message.getString(Message.PROPERTY_CONTENT);
            String creatorName = message
                    .getString(Message.PROPERTY_CREATOR_NAME);

            vh.receive_layout.setVisibility(View.GONE);
            vh.send_layout.setVisibility(View.GONE);

            long time = message.getCreatedTime() / 1000;

            String strTime = Utils.formatTimeStampString(
                    RemoteConversationActivity.this, time);

            if (!mCurrentUser.equalsIgnoreCase(creatorName)) {
                vh.sender.setText(creatorName);
                vh.receive_layout.setVisibility(View.VISIBLE);
                vh.receive_body.setText(text);
                vh.receive_time.setText(strTime);
            } else {
                vh.sender.setText("me");
                vh.send_layout.setVisibility(View.VISIBLE);
                vh.send_body.setText(text);
                vh.send_time.setText(strTime);
            }
            if ((lastCreator != null)
                    && lastCreator.equalsIgnoreCase(creatorName)) {
                vh.sender.setVisibility(View.GONE);
            } else {
                vh.sender.setVisibility(View.VISIBLE);
            }

            return view;
        }

        class ViewHolder {
            View empty;
            TextView sender;
            TextView receive_body;
            TextView send_body;

            View receive_layout;
            View send_layout;

            TextView receive_time;
            TextView send_time;

        }

        private void setViewHolder(View v) {
            ViewHolder vh = new ViewHolder();
            vh.empty = v.findViewById(R.id.empty);
            vh.sender = (TextView) v.findViewById(R.id.sender);
            vh.send_body = (TextView) v.findViewById(R.id.send_body);
            vh.receive_body = (TextView) v.findViewById(R.id.receive_body);

            vh.receive_layout = v.findViewById(R.id.receive_layout);
            vh.send_layout = v.findViewById(R.id.send_layout);

            vh.send_time = (TextView) v.findViewById(R.id.send_time);
            vh.receive_time = (TextView) v.findViewById(R.id.receive_time);

            v.setTag(vh);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMessageInput != null) {
            mMessageInput.setEnabled(true);
        }
    }

}