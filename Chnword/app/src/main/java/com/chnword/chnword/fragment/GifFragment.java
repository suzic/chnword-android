package com.chnword.chnword.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.MediaController;

import com.chnword.chnword.R;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageButton;

/**
 * Created by khtc on 15/5/31.
 */
public class GifFragment extends Fragment {

    private Context mContext;

    MediaController mc;

    private Uri uri;

    private LinearLayout gifViewContainer;

    GifImageButton gib;

    View self ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif, container, false);

        gifViewContainer = (LinearLayout) view.findViewById(R.id.gifViewContainer);

        mContext = getActivity().getApplicationContext();

        self = view;

        initWithGifView(view);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void initWithGifView(View view) {
        System.gc();
        gib = new GifImageButton( getActivity() );

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        gifViewContainer.addView(gib, param);
        gib.setBackgroundColor(0x00000000);

//        gib.setImageResource(R.drawable.videoloading);
//        gib.setImageURI(uri);

        GifDrawable gif = (GifDrawable) gib.getDrawable();

        mc = new MediaController( getActivity() );

        final MediaController mc = new MediaController( getActivity() );

        //判断gif类型
//        if (gib.getDrawable() instanceof pl.droidsonroids.gif.GifDrawable) {
////            mc.setMediaPlayer( (GifDrawable) gib.getDrawable());
////            mc.setAnchorView(gib );
////            mc.show();
//            gib.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mc.show();
//                }
//            });
//        }

    }

    // getter and setter

    public Uri getUri() {
        return uri;
    }


    public void setUri(final Uri theUri) {
        Log.e("GIFFRAGMENT URL", "theUri");
        uri = theUri;
//        Message msg = new Message();
//        msg.what = 1;
//        handler.handleMessage(msg);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    gib.setImageURI(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("GIFFRAGMENT URL", "error");
                }
            }
        });

    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            try {
                gib.setImageURI(uri);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("GIFFRAGMENT URL", "error");
            }

            super.handleMessage(msg);
        }
    };

    public int getLength() {
        GifDrawable gif = (GifDrawable) gib.getDrawable();
        int i = gif.getNumberOfFrames();
        return i - 1;
    }

    public int getCurrentProgress() {
        GifDrawable gif = (GifDrawable) gib.getDrawable();
        return gif.getCurrentFrameIndex();
    }

    public void updateState(int index) {
        GifDrawable gif = (GifDrawable) gib.getDrawable();
        try {
            gif.stop();
            gif.seekToFrame(index);
            gif.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void next() {
        GifDrawable gif = (GifDrawable) gib.getDrawable();
//        int i = gif.getNumberOfFrames();
        int i = gif.getCurrentFrameIndex();
        try {
            gif.stop();
            gif.seekToFrame(i + 5);
            gif.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void privous() {
        GifDrawable gif = (GifDrawable) gib.getDrawable();
        int i = gif.getCurrentFrameIndex();
        try {
            gif.stop();
            gif.seekToFrame(i - 5);
            gif.start();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
