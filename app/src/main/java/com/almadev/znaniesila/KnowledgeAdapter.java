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

    private static final int FIRST_LINE = -10;

    // private static final int ODD_LINE   = 2;
    // private static final int EVEN_LINE  = 3;

    private List<Question> mQuestions;
    private KnowledgeActivity mActivity;

    private LayoutInflater getLayoutInflater(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext());
    }

    public KnowledgeAdapter(List<Question> questions, KnowledgeActivity activity) {
        mQuestions = questions;
        mActivity = activity;
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == 0) {
            return FIRST_LINE;
        }
        return position;
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
        mKnowledgeViewHolder.render(mQuestions.subList(position * 7, 
                (position * 7) + 7 > mQuestions.size() ? mQuestions.size() : (position * 7 ) + 7));
    }

    @Override
    public int getItemCount() {
        int fullGroups = mQuestions.size() / 7;
        return mQuestions.size() % 7 > 0 ? fullGroups + 1 : fullGroups;
    }

    class CellClickListener implements View.OnClickListener {
        private Question mQuestion;

        public CellClickListener(Question pQuestion) {
            mQuestion = pQuestion;
        }

        @Override
        public void onClick(final View pView) {
            mActivity.openCellInfo(mQuestion);
        }
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

        public void render(final List<Question> pQuestions) {
            int i = 0;
            for (i = 0; i < pQuestions.size(); i++) {
                imgs.get(i).setVisibility(View.VISIBLE);

                if (pQuestions.get(i).getState() == QuestionState.CORRECT) {
                    ((ImageView) imgs.get(i).findViewById(R.id.img)).setImageDrawable(
                            mItem.getContext().getResources().getDrawable(R.drawable.hexagon));
                    ((TextView) imgs.get(i).findViewById(R.id.text)).setText("" + (pQuestions.get(i).getLocal_id() + 1));
                    ((TextView) imgs.get(i).findViewById(R.id.text)).setVisibility(View.VISIBLE);
                    imgs.get(i).setOnClickListener(new CellClickListener(pQuestions.get(i)));
                } else {
                    ((ImageView) imgs.get(i).findViewById(R.id.img)).setImageDrawable(
                            mItem.getContext().getResources().getDrawable(R.drawable.hexagon_locked));
                    ((TextView) imgs.get(i).findViewById(R.id.text)).setVisibility(View.INVISIBLE);
                    imgs.get(i).setOnClickListener(null);
                }
            }

            for (int k = i; k < imgs.size(); k++) {
                imgs.get(k).setVisibility(View.INVISIBLE);
            }
        }
    }
}
