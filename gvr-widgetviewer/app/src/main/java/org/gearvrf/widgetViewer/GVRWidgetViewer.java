/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.widgetViewer;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.io.GVRTouchPadGestureListener;
import org.gearvrf.utility.Log;
import org.gearvrf.widgetplugin.GVRWidgetPlugin;

public class GVRWidgetViewer extends GVRActivity
{
    private GVRWidgetPlugin mPlugin;
    private static final int BUTTON_INTERVAL = 500;
    private static final int TAP_INTERVAL = 300;
    private long mLatestButton = 0;
    private long mLatestTap = 0;
    private ViewerMain mMain = null;
    private GestureDetector mDetector = null;
    public MyGdxWidget mWidget;
    float mYangle = 0.0f;
    boolean mMovestart = false;
    float mMoveoffset = 0.0f;

    private GVRTouchPadGestureListener mSwipeListener = new GVRTouchPadGestureListener()
    {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.v("TOUCH", "onSingleTap");
            if (System.currentTimeMillis() > mLatestTap + TAP_INTERVAL) {
                mLatestTap = System.currentTimeMillis();
                mMain.onSingleTap(e);
            }
            return false;
        }
    };

    // GVRWidgetPlugin mPlugin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWidget = new MyGdxWidget();
        mPlugin = new GVRWidgetPlugin(this, mWidget);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mPlugin.setViewSize(displaymetrics.widthPixels,
                displaymetrics.heightPixels);
        mDetector = new GestureDetector(getBaseContext(), mSwipeListener);

        mMain = new ViewerMain(mPlugin);
        mPlugin.setMain(mMain);
        mWidget.mMain = mMain;
        setMain(mMain, "gvr.xml");
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > mLatestButton + BUTTON_INTERVAL) {
            mLatestButton = System.currentTimeMillis();
            mMain.onButtonDown();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mLatestButton = System.currentTimeMillis();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mPlugin.getWidgetView() == null)
            return false;
        return mPlugin.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        if (mMain.mObjectPointed) {
            float x = 0, dx = 0, y = 0, dy = 0.0f;
            if (event.getAction() == 0) {
                mMovestart = false;
                mMoveoffset = 0.0f;
                x = event.getX();
                y = event.getY();
            }
            if (event.getAction() == 2 || event.getAction() == 1) {
                dx = event.getX() - x;
                dy = event.getY() - y;
                if (event.getAction() == 1) {
                    mMovestart = false;
                    mMoveoffset = 0.0f;
                    mYangle = mMain.mRotateY;
                }
            }
            if (dx > dy && event.getAction() == 2) {
                mWidget.mCheckBox.setChecked(false);
                if (!mMovestart && mMoveoffset == 0.0f)
                    mMovestart = true;
                if (mMovestart) {
                    mMoveoffset = dx / 2;
                    mMovestart = false;
                    mMain.mRotateY = (mYangle) % 360;
                } else {
                    mMain.mRotateY = (dx / 2 + mYangle - mMoveoffset) % 360;
                }
                Log.d("NOLA", "onTouchEvent rotateY = %f", mMain.mRotateY);
            }
        }
        return true;
    }

}
