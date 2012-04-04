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

package com.kii.cloud.board.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kii.cloud.board.R;

public class ProgressingDialog {
    private Context mContext;
    private AlertDialog mProgressDialog;
    private TextView mTitle;
    public ProgressingDialog(Context context){
        mContext = context;
    }
    
    public void showProcessing(final int token, String process_title) {
        closeProgressDialog();

        LayoutInflater factory = LayoutInflater.from(mContext);

        final View processView = factory.inflate(R.layout.processing, null);
        mTitle = (TextView) processView
                .findViewById(R.id.process_text);
        mTitle.setText(process_title);

        mProgressDialog = new AlertDialog.Builder(mContext)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle("Progressing")
                .setView(processView)
                .create();

        mProgressDialog.show();
    }

    public void closeProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    
    public void updateDialogTitle(String newTitle){
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mTitle.setText(newTitle);
        }
    }
}
