package com.karthik.fbchatheadexample;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class FBChatHeadActivityFragment extends Fragment implements View.OnClickListener{

    private Context mContext;
    private RelativeLayout chatheadView, removeView;
    private ImageView chatheadImg, removeImg;
    private Point szWindow = new Point();
    private int x_init_cord, y_init_cord, x_init_margin, y_init_margin,x_remove,y_remove;
    private WindowManager windowManager;


    public FBChatHeadActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fbchat_head, container, false);
        mContext = getActivity();
        windowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        setupClickListners(rootView);
        return rootView;
    }


    private void setupClickListners(View rootview){
        Button start = (Button) rootview.findViewById(R.id.start_floating);
        Button stop = (Button) rootview.findViewById(R.id.stop_floating);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_floating:
                displayButton();
                break;
            case R.id.stop_floating:
                //remove the chat head
                if(chatheadView!=null){
                    windowManager.removeView(chatheadView);
                }
                break;
        }
    }


    private void displayButton(){

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        removeView = (RelativeLayout)inflater.inflate(R.layout.remove, null);

        WindowManager.LayoutParams paramRemove = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        paramRemove.gravity = Gravity.BOTTOM | Gravity.CENTER;

        removeView.setVisibility(View.GONE);
        removeImg = (ImageView)removeView.findViewById(R.id.remove_img);
        windowManager.addView(removeView, paramRemove);


        chatheadView = (RelativeLayout) inflater.inflate(R.layout.fbchathead, null);
        chatheadImg = (ImageView)chatheadView.findViewById(R.id.chathead_img);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            windowManager.getDefaultDisplay().getSize(szWindow);
        } else {
            int w = windowManager.getDefaultDisplay().getWidth();
            int h = windowManager.getDefaultDisplay().getHeight();
            szWindow.set(w, h);
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        windowManager.addView(chatheadView, params);
        chatheadView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) chatheadView.getLayoutParams();

                int x_cord = (int) event.getRawX();
                int y_cord = (int) event.getRawY();
                int x_cord_Destination, y_cord_Destination;
                switch (event.getAction()) {


                    case MotionEvent.ACTION_DOWN:

                        handler_longClick.post(runnable_longClick);

                        remove_img_width = removeImg.getLayoutParams().width;
                        remove_img_height = removeImg.getLayoutParams().height;

                        x_init_cord = x_cord;
                        y_init_cord = y_cord;

                        x_init_margin = layoutParams.x;
                        y_init_margin = layoutParams.y;
                        break;

                    //when the user is dragging the bubble calculate the offset
                    case MotionEvent.ACTION_MOVE:

                        int x_diff_move = x_cord - x_init_cord;
                        int y_diff_move = y_cord - y_init_cord;

                        x_cord_Destination = x_init_margin + x_diff_move;
                        y_cord_Destination = y_init_margin + y_diff_move;


                        layoutParams.x = x_cord_Destination;
                        layoutParams.y = y_cord_Destination;

                        windowManager.updateViewLayout(chatheadView, layoutParams);
                        break;

                    //when user lifts his hand update the current position
                    case MotionEvent.ACTION_UP:
                        isLongclick = false;
                        removeView.setVisibility(View.GONE);
                        removeImg.getLayoutParams().height = remove_img_height;
                        removeImg.getLayoutParams().width = remove_img_width;
                        handler_longClick.removeCallbacks(runnable_longClick);

                        if (inBounded) {
                            inBounded = false;
                            break;
                        }
                        if(isViewContains(layoutParams.x,layoutParams.y)){
                            Toast.makeText(mContext,"BINGO",Toast.LENGTH_SHORT).show();
                        }

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
                        inBounded = false;

                        break;
                }
                return true;

            }

            boolean isLongclick = false, inBounded = false;
            int remove_img_width = 0, remove_img_height = 0;

            Handler handler_longClick = new Handler();
            Runnable runnable_longClick = new Runnable() {

                @Override
                public void run() {
                    isLongclick = true;
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

    private boolean isViewContains(int x,int y) {
        Rect rect = new Rect(x_remove,y_remove, x_remove+removeView.getWidth(),y_remove+removeView.getHeight());
        Rect rect1 = new Rect(x,y, x+removeView.getWidth(),y+removeView.getHeight());
        return rect.intersect(rect1);
    }
}
