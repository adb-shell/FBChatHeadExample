package com.karthik.fbchatheadexample;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by karthikrk on 04/09/15.
 */
public class ChatBubbleService extends Service {

    private WindowManager windowManager;
    private RelativeLayout chatheadView, removeView;
    private Point szWindow = new Point();
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin,x_remove,y_remove;
    private Context mContext;
    private ImageView chatheadImg, removeImg;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TAG","CHAT HEAD SERVICE STARTED");
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


        //Adding the chathead view
        chatheadView = (RelativeLayout) inflater.inflate(R.layout.fbchathead, null);
        chatheadImg = (ImageView) chatheadView.findViewById(R.id.chathead_img);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(szWindow);
        } else {
            int w = windowManager.getDefaultDisplay().getWidth();
            int h = windowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }

        //use the same params as that of remove view just change the gravity
        WindowManager.LayoutParams chatHeadParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        chatHeadParams.gravity = Gravity.TOP | Gravity.LEFT;
        chatHeadParams.x = 0;
        chatHeadParams.y = 100;
        windowManager.addView(chatheadView, chatHeadParams);

        chatheadView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) chatheadView.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();
                int x_cord_Destination, y_cord_Destination;
                switch (event.getAction()) {
                    //when the user has pressed the chat bubble
                    case MotionEvent.ACTION_DOWN:

                        handler_longClick.post(runnable_longClick);
                        remove_img_width = removeImg.getLayoutParams().width;
                        remove_img_height = removeImg.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;
                        break;

                    //when the user is dragging the chat bubble
                    case MotionEvent.ACTION_MOVE:
                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        //make the remove view bigger
                        if(isViewIntersects(layoutParams.x, layoutParams.y)){
                            removeImg.getLayoutParams().height = (int) (remove_img_height * 1.5);
                            removeImg.getLayoutParams().width = (int) (remove_img_width * 1.5);
                            windowManager.updateViewLayout(removeView, removeView.getLayoutParams());
                            inBound = true;
                        }

                        else{
                            //restore the height to the normal of the remove view
                            if(inBound){
                                removeImg.getLayoutParams().height = getPixels();
                                removeImg.getLayoutParams().width = getPixels();
                                windowManager.updateViewLayout(removeView, removeView.getLayoutParams());
                                inBound = false;
                            }
                        }

                        windowManager.updateViewLayout(chatheadView, layoutParams);
                        break;

                    case MotionEvent.ACTION_UP:
                        removeView.setVisibility(View.GONE);
                        removeImg.getLayoutParams().height = remove_img_height;
                        removeImg.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        int x_diff = x_cord - x_init_cord;
                        int y_diff = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff;
                        y_cord_Destination = y_init_margin + y_diff;


                        int BarHeight = getStatusBarHeight();
                        if (y_cord_Destination < 0) {
                            y_cord_Destination = 0;
                        } else if (y_cord_Destination + (chatheadView.getHeight() + BarHeight) > szWindow.y) {
                            y_cord_Destination = szWindow.y - (chatheadView.getHeight() + BarHeight);
                        }
                        layoutParams.y = y_cord_Destination;

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

    private int getStatusBarHeight() {
        int statusBarHeight = (int) Math.ceil(25 * getResources().getDisplayMetrics().density);
        return statusBarHeight;
    }

    private boolean isViewIntersects(int x, int y) {
        Rect removeRect = new Rect(x_remove,y_remove, x_remove+removeView.getWidth(),y_remove+removeView.getHeight());
        Rect ChatHeadRect = new Rect(x,y, x+chatheadView.getWidth(),y+chatheadView.getHeight());
        return ChatHeadRect.intersect(removeRect);
    }

    private int getPixels(){
        final float scale = getResources().getDisplayMetrics().density;
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
