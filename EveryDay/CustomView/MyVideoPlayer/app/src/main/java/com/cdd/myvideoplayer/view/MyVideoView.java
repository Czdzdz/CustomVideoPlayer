package com.cdd.myvideoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

import static android.content.ContentValues.TAG;

/**
 * Created by Cdd on 2017/4/20 0020 23:39.
 * <p>
 * From url:
 */

public class MyVideoView extends VideoView {

    private int defalutWidth = 1920;
    private int defaultHeight = 1080;

    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getDefaultSize(defalutWidth, widthMeasureSpec);
        int height = getDefaultSize(defaultHeight, heightMeasureSpec);

        Log.e(TAG, "onMeasure: width: " + width + " height: " + height);

        setMeasuredDimension(width, height);
    }
}
