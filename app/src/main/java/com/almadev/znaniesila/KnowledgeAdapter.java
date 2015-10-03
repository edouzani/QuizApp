package com.almadev.znaniesila;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.QuestionState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aleksey on 02.10.2015.
 */
public class KnowledgeAdapter extends android.support.v7.widget.RecyclerView.Adapter {

    private static final int FIRST_LINE = 1;
    private static final int ODD_LINE   = 2;
    private static final int EVEN_LINE  = 3;

    private List<Question> mQuestions;

    private LayoutInflater getLayoutInflater(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext());
    }

    public KnowledgeAdapter(List<Question> questions) {
        mQuestions = questions;
    }

    @Override
    public int getItemViewType(final int position) {
//        return position % 7 > 3 ? EVEN_LINE : ODD_LINE;
        if (position == 0) {
            return  FIRST_LINE;
        }
        return position % 2 == 1 ? EVEN_LINE : ODD_LINE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater inflater = getLayoutInflater(parent);
        View line = null;
        line = inflater.inflate(R.layout.knowledge_line2, parent, false);

        if (viewType == FIRST_LINE) {
            line.setTag(new Boolean(true));
        }
        return new KnowledgeViewHolder(line);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        KnowledgeViewHolder mKnowledgeViewHolder = (KnowledgeViewHolder) holder;
//        int itemType = getItemViewType(position);
        mKnowledgeViewHolder.render(mQuestions.subList(position, position + 7 > mQuestions.size() ? mQuestions.size() : position + 7));
    }

    @Override
    public int getItemCount() {
        int fullGroups = mQuestions.size() / 7;
//        int leftItems = mQuestions.size() - fullGroups * 7;
//        if (leftItems  == 0) {
//            return fullGroups * 2;
//        }
//        if (leftItems % 7 > 4 || leftItems % 7 == 0) {
//            return fullGroups * 2 + 2;
//        } else {
//            return  fullGroups * 2 + 1;
//        }
        return mQuestions.size() % 7 > 0 ? fullGroups + 1 : fullGroups;
    }

    class KnowledgeViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout img1;
        private FrameLayout img2;
        private FrameLayout img3;
        private FrameLayout img4;
        private FrameLayout img5;
        private FrameLayout img6;
        private FrameLayout img7;
        private List<FrameLayout> imgs = new ArrayList<>();
        private View mItem;

        public KnowledgeViewHolder(final View itemView) {
            super(itemView);
            img1 = (FrameLayout) itemView.findViewById(R.id.first_elem);
            img2 = (FrameLayout) itemView.findViewById(R.id.second_elem);
            img3 = (FrameLayout) itemView.findViewById(R.id.third_elem);
            img4 = (FrameLayout) itemView.findViewById(R.id.forth_elem);
            img5 = (FrameLayout) itemView.findViewById(R.id.fifth_elem);
            img6 = (FrameLayout) itemView.findViewById(R.id.six_elem);
            img7 = (FrameLayout) itemView.findViewById(R.id.seven_elem);

            imgs.add(img1);
            imgs.add(img2);
            imgs.add(img3);
            imgs.add(img4);
            imgs.add(img5);
            imgs.add(img6);
            imgs.add(img7);

            mItem = itemView;
        }

        public void render(List<Question> pQuestions) {
            int i = 0;
            for (i = 0; i < pQuestions.size(); i++) {
                if (pQuestions.get(i).getState() == QuestionState.CORRECT) {
                    ((ImageView)imgs.get(i).findViewById(R.id.img)).setImageDrawable(
                            mItem.getContext().getResources().getDrawable(R.drawable.hexagon));
                    ((TextView)imgs.get(i).findViewById(R.id.text)).setText(pQuestions.get(i).getStory_order_id());
                    ((TextView)imgs.get(i).findViewById(R.id.text)).setVisibility(View.VISIBLE);
                } else {
                    ((ImageView)imgs.get(i).findViewById(R.id.img)).setImageDrawable(
                            mItem.getContext().getResources().getDrawable(R.drawable.hexagon_locked));
                    ((TextView)imgs.get(i).findViewById(R.id.text)).setVisibility(View.INVISIBLE);
                }
            }

            for (int k = i; i < imgs.size(); k++) {
                imgs.get(k).setVisibility(View.INVISIBLE);
            }
//            switch (pQuestions.size()) {
//                case 7:
//                    break;
//                case 6:
//                    img7.setVisibility(View.INVISIBLE);
//                    break;
//                case 5:
//                    img6.setVisibility(View.INVISIBLE);
//                    img7.setVisibility(View.INVISIBLE);
//                    break;
//                case 4:
//                    img5.setVisibility(View.INVISIBLE);
//                    img6.setVisibility(View.INVISIBLE);
//                    img7.setVisibility(View.INVISIBLE);
//                    break;
//                case 3:
//                    img5.setVisibility(View.INVISIBLE);
//                    img4.setVisibility(View.INVISIBLE);
//                    img3.setVisibility(View.INVISIBLE);
//                    img2.setVisibility(View.INVISIBLE);
//                    break;
//                case 2:
//                    img7.setVisibility(View.INVISIBLE);
//                    img6.setVisibility(View.INVISIBLE);
//                    img5.setVisibility(View.INVISIBLE);
//                    img4.setVisibility(View.INVISIBLE);
//                    img3.setVisibility(View.INVISIBLE);
//                    break;
//                case 1:
//                    img7.setVisibility(View.INVISIBLE);
//                    img6.setVisibility(View.INVISIBLE);
//                    img5.setVisibility(View.INVISIBLE);
//                    img4.setVisibility(View.INVISIBLE);
//                    img3.setVisibility(View.INVISIBLE);
//                    img2.setVisibility(View.INVISIBLE);
//                    break;
//            }
        }
    }
}
