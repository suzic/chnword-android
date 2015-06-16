package com.chnword.chnword.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.chnword.chnword.R;
import com.chnword.chnword.activity.ShowActivity;
import com.chnword.chnword.activity.WordActivity;
import com.chnword.chnword.adapter.ModuleAdapter;
import com.chnword.chnword.adapter.WordAdapter;
import com.chnword.chnword.beans.Module;
import com.chnword.chnword.beans.Word;

/**
 * Created by khtc on 15/6/16.
 * 二级分类模块
 */
public class WordFragment extends Fragment {

    ListView word_module_listView;
    GridView word_gridView;

    ModuleAdapter moduleAdapter;
    WordAdapter wordAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_word, container, false);

        word_module_listView = (ListView) view.findViewById(R.id.word_module_listView);
        word_gridView = (GridView) view.findViewById(R.id.word_gridView);

        moduleAdapter = new ModuleAdapter(getActivity(), ((WordActivity) getActivity()).getModules());
        wordAdapter = new WordAdapter(getActivity(), ((WordActivity) getActivity()).getWords());


        word_module_listView.setAdapter(moduleAdapter);
        word_module_listView.setOnItemClickListener(moduleItemClick);

        word_gridView.setAdapter(wordAdapter);
        word_gridView.setOnItemClickListener(wordItemClick);


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

    private AdapterView.OnItemClickListener moduleItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Module module = (Module)moduleAdapter.getItem(position);
            ((WordActivity) getActivity()).showWord(module);
        }
    };
    private AdapterView.OnItemClickListener wordItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Word word = (Word)moduleAdapter.getItem(position);
//            ((WordActivity) getActivity()).showWord(module);
            //开启word
            Intent intent = new Intent(getActivity(), ShowActivity.class);

            intent.putExtra("word", word.getWord());
            intent.putExtra("word_index", word.getWordIndex());


            startActivity(intent);

        }
    };


    public void updateData() {
        moduleAdapter.notifyDataSetChanged();
        wordAdapter.notifyDataSetChanged();
    }


}
