package com.karthik.fbchatheadexample;


import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by karthikrk on 04/09/15.
 */
public class ChatHeadService extends Service{

    private WindowManager windowManager;
    private RelativeLayout removeView;
    /**our custom chat head view**/
    private ChatHead chatheadView;
    private Point szWindow = new Point();
    private float x_remove,y_remove, screenHeight, screenWidth,statusBarHeight;

    private Context mContext;
    private ImageView removeImg;
    private DisplayMetrics displayMetrics;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TAG","CHAT HEAD SERVICE STARTED");

        displayMetrics = getResources().getDisplayMetrics();
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        statusBarHeight = getStatusBarHeight();

        if(startId == Service.START_STICKY) {
            displayChatBubble();
            return super.onStartCommand(intent, flags, startId);
        }else{
            return  Service.START_NOT_STICKY;
        }
    }

    private void displayChatBubble() {

        mContext = this;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);


        //Adding the remove view
        removeView = (RelativeLayout) inflater.inflate(R.layout.remove, null);

        WindowManager.LayoutParams removeParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        //in order to add the remove exactly at the bottom of the screen
        removeParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        removeView.setVisibility(View.GONE);
        removeImg = (ImageView) removeView.findViewById(R.id.remove_img);
        windowManager.addView(removeView, removeParams);


        chatheadView  = new ChatHead(mContext);

        windowManager.getDefaultDisplay().getSize(szWindow);

        //use the same params as that of remove view just change the gravity
        WindowManager.LayoutParams chatHeadParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);


        windowManager.addView(chatheadView, chatHeadParams);

        chatheadView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float xPos = event.getRawX();
                float yPos = event.getRawY();
                switch (event.getAction()) {
                    //when the user has pressed the chat bubble
                    case MotionEvent.ACTION_DOWN:

                        handler_longClick.post(runnable_longClick);
                        remove_img_width = removeImg.getLayoutParams().width;
                        remove_img_height = removeImg.getLayoutParams().height;

                        break;

                    //when the user is dragging the chat bubble
                    case MotionEvent.ACTION_MOVE:

                        if(xPos+(chatheadView.mBitmap.getWidth())>screenWidth){
                            xPos = screenWidth-chatheadView.mBitmap.getWidth();
                        }
                        else if(xPos<0){
                            xPos=0;
                        }

                        if(yPos+(chatheadView.mBitmap.getHeight())+statusBarHeight>screenHeight){
                            yPos = screenHeight-statusBarHeight-chatheadView.mBitmap.getHeight();
                        }
                        else if(yPos<0){
                            yPos=0;
                        }

                        chatheadView.mXSprings.setEndValue(xPos);
                        chatheadView.mYSprings.setEndValue(yPos);

                        int normalRemoveHeight = getPixels();
                        if(yPos== screenHeight-statusBarHeight-chatheadView.mBitmap.getHeight() ||
                                (yPos< screenHeight -statusBarHeight-chatheadView.mBitmap.getHeight()
                                        && yPos>=y_remove-removeView.getHeight()-chatheadView.mBitmap.getHeight())){
                            if(checkViewIntersection((int)xPos)){
                                removeImg.getLayoutParams().height = (int) (remove_img_height * 1.5);
                                removeImg.getLayoutParams().width = (int) (remove_img_width * 1.5);
                                windowManager.updateViewLayout(removeView, removeView.getLayoutParams());
                                inBound = true;
                            }
                            //when x co-ordinate is not same
                            else{
                                removeImg.getLayoutParams().height =normalRemoveHeight ;
                                removeImg.getLayoutParams().width = normalRemoveHeight;
                                windowManager.updateViewLayout(removeView, removeView.getLayoutParams());
                                inBound = false;
                            }
                        }

                        //general case when x and y is not same
                        else{
                            //restore the screenHeight to the normal of the remove view
                            if(inBound){
                                removeImg.getLayoutParams().height = normalRemoveHeight;
                                removeImg.getLayoutParams().width =  normalRemoveHeight;
                                windowManager.updateViewLayout(removeView, removeView.getLayoutParams());
                                inBound = false;
                            }
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        removeView.setVisibility(View.GONE);
                        removeImg.getLayoutParams().height = remove_img_height;
                        removeImg.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        if(inBound && chatheadView!=null){
                            //remove the chathead view here
                            windowManager.removeView(chatheadView);
                            stopSelf();
                            chatheadView = null;
                        }
                        break;

                    default:
                        Log.e("TAG", "chatheadView.setOnTouchListener  -> event.getAction() : default");
                        break;
                }
                return true;
            }

            boolean inBound = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {

                @Override
                public void run() {
                    removeView.setVisibility(View.VISIBLE);
                    if(x_remove==0 || y_remove==0){
                        int[] pos = new int[2];
                        removeView.getLocationOnScreen(pos);
                        x_remove = pos[0];
                        y_remove = pos[1];
                    }
                }
            };


        });

    }

    private float getStatusBarHeight() {
        float result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private boolean checkViewIntersection(int x) {
       if(x>x_remove-removeView.getWidth() && x<x_remove)
           return true;
        return false;
    }

    private int getPixels(){
        final float scale = displayMetrics.density;
        return (int) (80 * scale + 0.5f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("TAG", "CHAT HEAD SERVICE DESTROYED");

        if(chatheadView != null){
            windowManager.removeView(chatheadView);
        }

        if(removeView != null){
            windowManager.removeView(removeView);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
