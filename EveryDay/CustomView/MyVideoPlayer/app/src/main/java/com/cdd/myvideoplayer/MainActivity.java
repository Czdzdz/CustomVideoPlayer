package com.cdd.myvideoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cdd.myvideoplayer.view.MyVideoView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    MyVideoView mVideoView;
    MediaController mController;

    private TextView currentTimeTv, totalTimeTv;
    private ImageView pauseImg, screenImg, volumeImg;
    private SeekBar videoSeekBar, volumeSeekBar;
    private RelativeLayout videoLayout;

    private int screenWidth, screenHeight;

    private static final int UPDATE_UI = 0;
    private static final String TAG = "Cdd";
    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";

    private AudioManager mAudioManager;

    private boolean isFullScreen = false;
    private boolean isAdjust = false;//误触判断
    private int threshold = 54;//误触临界值

    private float mBrightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mVideoView = (MyVideoView) findViewById(R.id.videoview);
        currentTimeTv = (TextView) findViewById(R.id.current_time_tv);
        totalTimeTv = (TextView) findViewById(R.id.total_time_tv);
        pauseImg = (ImageView) findViewById(R.id.pause_img);
        screenImg = (ImageView) findViewById(R.id.screen_img);
        volumeImg = (ImageView) findViewById(R.id.volume_img);
        videoSeekBar = (SeekBar) findViewById(R.id.seekbar);
        volumeSeekBar = (SeekBar) findViewById(R.id.volume_seekbar);

        videoLayout = (RelativeLayout) findViewById(R.id.videoLayout);

        pauseImg.setOnClickListener(this);
        screenImg.setOnClickListener(this);

        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //更新拖动后的显示
                updateWithTimeFormat(currentTimeTv, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                //获取拖动后的进度
                int progress = seekBar.getProgress();
                //视频播放进度保持同步更新
                mVideoView.seekTo(progress);
                mHandler.sendEmptyMessage(UPDATE_UI);

            }
        });


        /**
         * 获取到设备屏幕宽高
         */
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;


        /**
         * 本地视频播放
         */
        mVideoView.setVideoPath(path);
        mVideoView.start();
        mHandler.sendEmptyMessageDelayed(UPDATE_UI, 500);

        //获取系统音频管理器
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        /**
         * 获取当前设备的最大音量
         */
        int streamMaxVolume = mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_MUSIC);
        /**
         * 获取设备当前的音量值
         */
        int streamVolume = mAudioManager.getStreamVolume(mAudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(streamMaxVolume);
        volumeSeekBar.setProgress(streamVolume);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /**
                 * 设置当前设备音量
                 */
                mAudioManager.setStreamVolume(mAudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        addVideoViewTouchListener();

        //使用MediaController绑定VideoView
//        initPlayer();
    }

    private void addVideoViewTouchListener() {
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            public float lastX = 0, lastY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                /**
                 * 触摸时的X轴和Y轴的初始位置
                 */
                float x = event.getX();
                float y = event.getY();


                switch (event.getAction()) {
                    /**
                     * 手指落下屏幕的那一刻（只会调用一次）
                     */
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;

                        break;
                    /**
                     * 手指在屏幕上滑动（调用多次）
                     */
                    case MotionEvent.ACTION_MOVE:

                        /**
                         * 在X轴，Y轴的偏移量
                         */
                        float deltaX = x - lastX;
                        float deltaY = y - lastY;

                        //绝对值
                        float absDeltaX = Math.abs(deltaX);
                        float absDeltaY = Math.abs(deltaY);

                        if (absDeltaX > threshold && absDeltaY > threshold) {
                            if (absDeltaX < absDeltaY) {
                                isAdjust = true;
                            } else {
                                isAdjust = false;
                            }

                        } else if (absDeltaX < threshold && absDeltaY > threshold) {
                            isAdjust = true;
                        } else if (absDeltaX > threshold && absDeltaY < threshold) {
                            isAdjust = false;
                        }

                        Log.e(TAG, "onTouch: 手势是否合法：" + isAdjust);

                        /**
                         * 在判断好当前手势时间已经合法的前提下，去区分此时手势应该调节亮度还是音量
                         */
                        if (isAdjust) {
                            if (x < screenWidth / 2) {
                                /**
                                 * 调节亮度
                                 */
                                if (deltaY > 0) {
                                    /**
                                     * 降低亮度
                                     */
                                    Log.e(TAG, "onTouch: 减低亮度" + deltaY);
                                } else {
                                    /**
                                     * 升高亮度
                                     */
                                    Log.e(TAG, "onTouch: 升高亮度" + deltaY);
                                }
                                changeBrightness(-deltaY);

                            } else {

                                if (deltaY > 0) {
                                    /**
                                     * 减小声音
                                     */
                                    Log.e(TAG, "onTouch: 减小声音" + deltaY);
                                } else {
                                    /**
                                     * 增大声音
                                     */
                                    Log.e(TAG, "onTouch: 增大声音" + deltaY);

                                }
                                changeVolume(-deltaY);
                            }
                        }
                        lastX = x;
                        lastY = y;

                        break;
                    /**
                     * 手指离开屏幕那一刻(只调用一次)
                     */
                    case MotionEvent.ACTION_UP:
                        lastX = 0;
                        lastY = 0;

                        break;
                }

                return true;  //返回true,手势生效
            }
        });
    }


    /**
     * 调节音量
     *
     * @param deltaY
     */
    private void changeVolume(float deltaY) {
        //获取到最大音量值及当前音量值
        int max = mAudioManager.getStreamMaxVolume(mAudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(mAudioManager.STREAM_MUSIC);

        int index = (int) (deltaY / screenHeight * max * 3);//强化偏移量

        int volume = Math.max(current + index, 0);

        mAudioManager.setStreamVolume(mAudioManager.STREAM_MUSIC, volume, 0);

        volumeSeekBar.setProgress(volume);
    }

    /**
     * 调节亮度
     *
     * @param deltaY
     */
    private void changeBrightness(float deltaY) {
        WindowManager.LayoutParams attr = getWindow().getAttributes();
        mBrightness = attr.screenBrightness;
        float index = deltaY / screenHeight / 3;//弱化偏移量
        mBrightness += index;

        /**
         * 临界值判断
         */
        if (mBrightness > 1.0f) {
            mBrightness = 1.0f;
        }

        if (mBrightness < 0.01f) {
            mBrightness = 0.01f;
        }

        attr.screenBrightness = mBrightness;
        getWindow().setAttributes(attr);

    }

    private void initPlayer() {

        Log.e(TAG, "initPlayer: 视频路径" + path);
        /**
         * 本地视频播放
         */
        mVideoView.setVideoPath(path);

        /**
         * 网络视频播放
         */
//        mVideoView.setVideoURI(Uri.parse(""));

        /**
         * 使用MediaController控制视频播放
         */
        mController = new MediaController(this);

        /**
         * 设置VideoView与MediaController建立关联
         */
        mVideoView.setMediaController(mController);

        /**
         * 设置MediaController与VideoView建立关联
         */
        mController.setMediaPlayer(mVideoView);

        Log.e(TAG, "initPlayer: 视频总时长 " + mVideoView.getDuration());
        mVideoView.start();
    }

    /**
     * 格式化时间显示
     *
     * @param textView
     * @param millisecond
     */
    private void updateWithTimeFormat(TextView textView, int millisecond) {
        int second = millisecond / 1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;

        String str;

        if (hh != 0) {
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }

        textView.setText(str);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == UPDATE_UI) {
                //获取视频当前进度时间
                int currentTime = mVideoView.getCurrentPosition();
                //获取视频总时长
                int totalTime = mVideoView.getDuration();

                //格式化视频播放时间
                updateWithTimeFormat(currentTimeTv, currentTime);
                updateWithTimeFormat(totalTimeTv, totalTime);

                videoSeekBar.setMax(totalTime);
                videoSeekBar.setProgress(currentTime);

                //持续刷新更新进度
                mHandler.sendEmptyMessageDelayed(UPDATE_UI, 500);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause_img:

                if (mVideoView.isPlaying()) {
                    pauseImg.setImageResource(R.drawable.play_black);
                    //暂停播放
                    mVideoView.pause();

                    mHandler.removeMessages(UPDATE_UI);
                } else {
                    pauseImg.setImageResource(R.drawable.play_white);
                    //继续播放
                    mVideoView.start();

                    //重新开启刷新
                    mHandler.sendEmptyMessage(UPDATE_UI);
                }

                break;
            case R.id.screen_img:

                /**
                 * 根据当前屏幕状态切换
                 */
                if (isFullScreen) {

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//切换为竖屏

                } else {

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//切换为横屏

                }

                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(UPDATE_UI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    /**
     * 动态改变视频播放框的大小
     *
     * @param width
     * @param height
     */
    private void setVideoViewScale(int width, int height) {
        ViewGroup.LayoutParams params = mVideoView.getLayoutParams();
        params.width = width;
        params.height = height;
        mVideoView.setLayoutParams(params);

        ViewGroup.LayoutParams params2 = videoLayout.getLayoutParams();
        params2.width = width;
        params2.height = height;
        videoLayout.setLayoutParams(params2);
    }

    /**
     * 监听屏幕方向的改变
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.e(TAG, "onConfigurationChanged: ScreenWidth: " + screenWidth + "  ScreenHeight: " + screenHeight);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /**
             * 屏幕方向为横屏
             */
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            /**
             * 设置音量标识的显示
             */
            volumeSeekBar.setVisibility(View.VISIBLE);
            volumeImg.setVisibility(View.VISIBLE);

            isFullScreen = true;

            //当屏幕方向为横屏时引导清除半屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            Log.e(TAG, "onConfigurationChanged: ORIENTATION_LANDSCAPE  VideoViewWidth: " + mVideoView.getWidth() + " VideoViewHeight: " + mVideoView.getHeight());

        } else {
            /**
             * 屏幕方向为竖屏
             */
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtils.dp2px(MainActivity.this, 240));
            /**
             * 设置音量标识的隐藏
             */
            volumeSeekBar.setVisibility(View.GONE);
            volumeImg.setVisibility(View.GONE);

            isFullScreen = false;

            //当屏幕方向为竖屏时引导强制全屏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);


            Log.e(TAG, "onConfigurationChanged:  ORIENTATION_PORTRAIT  VideoViewWidth: " + mVideoView.getWidth() + " VideoViewHeight: " + mVideoView.getHeight());
        }
    }
}
