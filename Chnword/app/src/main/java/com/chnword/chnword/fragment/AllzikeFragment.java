package com.chnword.chnword.fragment;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.chnword.chnword.R;
import com.chnword.chnword.net.NetConf;

/**
 * Created by khtc on 15/9/15.
 */
public class AllzikeFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_allzike, container, false);

        ImageButton orderButton = (ImageButton) view.findViewById(R.id.orderButton);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转
                Uri uri = Uri.parse(NetConf.URL_BUY);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
