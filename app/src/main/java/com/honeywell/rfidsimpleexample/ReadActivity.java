package com.honeywell.rfidsimpleexample;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.honeywell.rfidservice.EventListener;
import com.honeywell.rfidservice.RfidManager;
import com.honeywell.rfidservice.TriggerMode;
import com.honeywell.rfidservice.rfid.OnTagReadListener;
import com.honeywell.rfidservice.rfid.RfidReader;
import com.honeywell.rfidservice.rfid.TagAdditionData;
import com.honeywell.rfidservice.rfid.TagReadData;
import com.honeywell.rfidservice.rfid.TagReadOption;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReadActivity extends AppCompatActivity {
    private RfidManager mRfidMgr;
    private RfidReader mReader;
    private List mTagDataList = new ArrayList();

    private boolean mIsReadBtnClicked;
    private Button mBtnRead;
    private ListView mLv;
    private ArrayAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRfidMgr = MyApplication.getInstance().rfidMgr;
        mReader = MyApplication.getInstance().mRfidReader;
        setContentView(R.layout.activity_read);
        mBtnRead = findViewById(R.id.btn_read);
        mLv = findViewById(R.id.lv);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTagDataList);
        mLv.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRfidMgr.addEventListener(mEventListener);
        showBtn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRfidMgr.removeEventListener(mEventListener);
        mIsReadBtnClicked = false;
        stopRead();
    }

    private void showBtn() {
        if (mIsReadBtnClicked) {
            mBtnRead.setText("Stop");
            mBtnRead.setTextColor(Color.rgb(255, 128, 0));
        } else {
            mBtnRead.setText("Read");
            mBtnRead.setTextColor(Color.rgb(0, 0, 0));
        }
    }

    public void clickBtnRead(View view) {
        if (mIsReadBtnClicked) {
            mIsReadBtnClicked = false;
            stopRead();
        } else {
            mIsReadBtnClicked = true;
            read();
        }

        showBtn();
    }

    private EventListener mEventListener = new EventListener() {
        @Override
        public void onDeviceConnected(Object o) {
        }

        @Override
        public void onDeviceDisconnected(Object o) {
        }

        @Override
        public void onReaderCreated(boolean b, RfidReader rfidReader) {
        }

        @Override
        public void onRfidTriggered(boolean trigger) {
            if (mIsReadBtnClicked || !trigger) {
                mIsReadBtnClicked = false;
                stopRead();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showBtn();
                    }
                });
            } else {
                read();
            }
        }

        @Override
        public void onTriggerModeSwitched(TriggerMode triggerMode) {
        }
    };

    private boolean isReaderAvailable() {
        return mReader != null && mReader.available();
    }

    private void read() {
        if (isReaderAvailable()) {
            mTagDataList.clear();
            mReader.setOnTagReadListener(dataListener);
            mReader.read(TagAdditionData.get("None"), new TagReadOption());
        }
    }

    private void stopRead() {
        if (isReaderAvailable()) {
            mReader.stopRead();
            mReader.removeOnTagReadListener(dataListener);
        }
    }

    private OnTagReadListener dataListener = new OnTagReadListener() {
        @Override
        public void onTagRead(final TagReadData[] t) {
            synchronized (mTagDataList) {
                for (TagReadData trd : t) {
                    String epc = trd.getEpcHexStr();

                    if (!mTagDataList.contains(epc)) {
                        mTagDataList.add(epc);
                    }
                }

                mHandler.sendEmptyMessage(0);
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
