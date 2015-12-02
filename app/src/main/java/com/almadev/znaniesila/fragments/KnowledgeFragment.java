package com.almadev.znaniesila.fragments;


import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.almadev.znaniesila.KnowledgeActivity;
import com.almadev.znaniesila.R;
import com.almadev.znaniesila.utils.social.SocialController;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KnowledgeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KnowledgeFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_STORY = "param1";
    private static final String ARG_IMG   = "param2";
    private static final String ARG_FM    = "fm";

    // TODO: Rename and change types of parameters
    private String          mStory;
    private String          mImgUrl;


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
        Picasso.with(root.getContext()).load(mImgUrl)
                .placeholder(getResources().getDrawable(R.drawable.plate_txt))
                .centerCrop()
                .fit()
                .into((ImageView) root.findViewById(R.id.story_img));

        root.findViewById(R.id.share_fb).setOnClickListener(this);
        root.findViewById(R.id.share_vk).setOnClickListener(this);
        root.findViewById(R.id.share_ok).setOnClickListener(this);
        return root;
    }


    @Override
    public void onClick(final View pView) {
        String comments = mStory;
        switch(pView.getId()) {
            case R.id.share_fb:
                Intent shareIntentFb = new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse("https://www.facebook.com/sharer/sharer.php?u=http://www.znanie.tv"));
                startActivity(shareIntentFb);
                break;
            case R.id.share_vk:
//                Intent shareIntentVk = new Intent(Intent.ACTION_VIEW,
//                                                  Uri.parse("http://vk.com/share.php?url=http://www.znanie.tv/&title=Знание-сила!" +
//                                                                    "&description=" + comments + "&image=http://www.znanie.tv/zshare.jpg&noparse=true"));
//                startActivity(shareIntentVk);
                SocialController.vkShare(getActivity(), getActivity().getSupportFragmentManager(), null, comments);
                break;
            case R.id.share_ok:
                Intent shareIntentOk = new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse("http://www.odnoklassniki.ru/dk?st.cmd=addShare&st.s=1&"
                                                                    + "st.comments=" + comments + "&st._surl=http://www.znanie.tv/"));
                startActivity(shareIntentOk);
                break;
        }
    }
}
