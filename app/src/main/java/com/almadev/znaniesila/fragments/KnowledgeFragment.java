package com.almadev.znaniesila.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.almadev.znaniesila.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KnowledgeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KnowledgeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STORY = "param1";
    private static final String ARG_IMG   = "param2";

    // TODO: Rename and change types of parameters
    private String mStory;
    private String mImgUrl;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param story Parameter 1.
     * @param imgUrl Parameter 2.
     * @return A new instance of fragment KnowledgeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KnowledgeFragment newInstance(String story, String imgUrl) {
        KnowledgeFragment fragment = new KnowledgeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STORY, story);
        args.putString(ARG_IMG, imgUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public KnowledgeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStory = getArguments().getString(ARG_STORY);
            mImgUrl = getArguments().getString(ARG_IMG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_knowledge, container, false);
        ((TextView)root.findViewById(R.id.story_text)).setText(mStory);
        return root;
    }


}
