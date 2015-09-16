package com.chnword.chnword.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.chnword.chnword.R;
import com.chnword.chnword.activity.FaqActivity;
import com.chnword.chnword.activity.FeedbackActivity;
import com.chnword.chnword.activity.InfoListActivity;
import com.chnword.chnword.activity.KnowledgeActivity;
import com.chnword.chnword.activity.PhoneBindActivity;
import com.chnword.chnword.activity.TabActivity;
import com.chnword.chnword.adapter.SettingAdapter;
import com.chnword.chnword.beans.TabuserItem;
import com.chnword.chnword.net.NetConf;
import com.chnword.chnword.store.LocalStore;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by khtc on 15/4/23.
 */
public class TabUser extends Fragment {

    private EditText registPhoneNumber;

    private ListView settingListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragtab_user, container, false);


        LocalStore store = new LocalStore(getActivity());
        settingListView = (ListView) view.findViewById(R.id.settingListView);

        List<TabuserItem> info = prepareData();


        SettingAdapter adapter = new SettingAdapter(this.getActivity(), info);
        settingListView.setAdapter(adapter);
        settingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:

                        //显示常见问题
                        Intent intent1 = new Intent(getActivity(), FaqActivity.class);
                        startActivity(intent1);

                        break;

                    case 1:

                        //显示建议反馈的界面。
                        Intent feedback = new Intent(getActivity(), FeedbackActivity.class);
                        startActivity(feedback);

                        break;

                    case 2:

                        //手机捆绑
                        Intent bindIntent = new Intent(getActivity(), PhoneBindActivity.class);
                        startActivity(bindIntent);

                        break;

                    case 3:
                        //进行分享
                        ((TabActivity) getActivity()).openUmeng();

                        break;

                    case 4:

                        //有偿推广
                        Intent knowledgeIntent = new Intent(getActivity(), KnowledgeActivity.class);
                        startActivity(knowledgeIntent);

                        break;

                    default:
                        break;
                }

            }
        });

        return view;
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
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void onInforListButtonClicked(View v) {
        Intent infolistIntent = new Intent(getActivity(), InfoListActivity.class);
        startActivity(infolistIntent);
    }

    public void onShareButtonClicked(View v) {
        ((TabActivity) getActivity()).openUmeng();
    }


    private List<TabuserItem> prepareData() {
        List<TabuserItem> info = new ArrayList<TabuserItem>();
        TabuserItem item = new TabuserItem();
        item.setResourceId(R.drawable.uicon_1);
        item.setTitle("用户FAQ");
        info.add(item);

        item = new TabuserItem();
        item.setResourceId(R.drawable.uicon_2);
        item.setTitle("信息反馈");
        info.add(item);

        item = new TabuserItem();
        item.setResourceId(R.drawable.uicon_3);
        item.setTitle("手机绑定");
        info.add(item);

        item = new TabuserItem();
        item.setResourceId(R.drawable.uicon_4);
        item.setTitle("分享好友");
        info.add(item);

        item = new TabuserItem();
        item.setResourceId(R.drawable.uicon_5);
        item.setTitle("有偿推广");
        info.add(item);

        return info;
    }


}