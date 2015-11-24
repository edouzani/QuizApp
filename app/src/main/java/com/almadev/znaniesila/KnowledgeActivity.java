package com.almadev.znaniesila;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.almadev.znaniesila.fragments.KnowledgeFragment;
import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Question;
import com.almadev.znaniesila.model.QuestionState;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.Constants;
import com.astuetz.PagerSlidingTabStrip;

import java.util.List;

public class KnowledgeActivity extends FragmentActivity implements View.OnClickListener {

    private static final String KNOWLEDGE_IS_NOT_OPEN_YET = "Знание еще не открыто";

    private RecyclerView         mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Category             mCategory;
    private ViewPager            mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private List<Question>       mQuestions;
    private PagerSlidingTabStrip mTabs;
    private Quiz                 mQuiz;

    @Override
    protected void onStop() {
        super.onStop();
        QuizHolder.getInstance(this).saveQuiz(mQuiz);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        Intent intent = getIntent();
        mCategory = (Category) intent.getSerializableExtra(Constants.CATEGORY);
        mQuiz = QuizHolder.getInstance(this).getQuiz(mCategory.getCategory_id());
        mQuestions = mQuiz.getQuestions();
        ((TextView) findViewById(R.id.catname)).setText(mCategory.getCategory_name());

        findViewById(R.id.home).setOnClickListener(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        mViewPager = (ViewPager) findViewById(R.id.containerPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mTabs.setViewPager(mViewPager);
        mTabs.setOnPageChangeListener(new PagerChangeListener());

        initRecycle();

        findViewById(R.id.home_fragment).setOnClickListener(this);

        findViewById(R.id.description_fragment).setVisibility(View.GONE);
    }

    class PagerChangeListener implements ViewPager.OnPageChangeListener {
        private boolean isForwardScroll = true;

        @Override
        public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            isForwardScroll = position >= mViewPager.getCurrentItem();
        }

        @Override
        public void onPageSelected(final int position) {
            if (mQuestions.get(position).getState() != QuestionState.CORRECT) {
                if (isForwardScroll) {
                    for (int i = position + 1; i < mQuestions.size(); i++) {
                        if (mQuestions.get(i).getState() == QuestionState.CORRECT) {
                            mViewPager.setCurrentItem(i);
                            return;
                        }
                    }
                    for (int i = 0; i < position; i++) {
                        if (mQuestions.get(i).getState() == QuestionState.CORRECT) {
                            mViewPager.setCurrentItem(i);
                            return;
                        }
                    }
                } else {
                    for (int i = position - 1; i > 0; i--) {
                        if (mQuestions.get(i).getState() == QuestionState.CORRECT) {
                            mViewPager.setCurrentItem(i);
                            return;
                        }
                    }
                    for (int i = mQuestions.size() - 1; i > position; i--) {
                        if (mQuestions.get(i).getState() == QuestionState.CORRECT) {
                            mViewPager.setCurrentItem(i);
                            return;
                        }
                    }
                }
            } else {
                if (mQuestions.get(position).getState() == QuestionState.CORRECT) {
                    mQuestions.get(position).setIsStoryViewed(true);
                }
                mViewPager.setCurrentItem(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
//            int k = 0;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.CustomTabProvider{
        private Context context;
        private int previousColor = -1;

        public SectionsPagerAdapter(FragmentManager fm, Context pContext) {
            super(fm);
            context = pContext;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below)
            //
            boolean isOpened = mQuestions.get(position).getState() == QuestionState.CORRECT;

            String story = KNOWLEDGE_IS_NOT_OPEN_YET;
            String imgUrl = null;
            if (isOpened) {
                story = mQuestions.get(position).getStory();
                imgUrl = mQuestions.get(position).getImage_url();
            }
            return KnowledgeFragment.newInstance(story == null || story.isEmpty() ? mQuestions.get(position).getCorrect_ans_explanation() : story, imgUrl);
        }

        @Override
        public int getCount() {
            return mQuestions.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int id = mQuestions.get(position).getLocal_id() + 1;
            return "" + (id < 10 ? "0" + id : id);
        }

        @Override
        public View getCustomTabView(final ViewGroup parent, final int position) {
            View tab = LayoutInflater.from(context).inflate(R.layout.knowledge_tab, parent, false);
            Question question = mQuestions.get(position);
            TextView tabTitle = (TextView)tab.findViewById(R.id.tab_title);
            tabTitle.setText(getPageTitle(position));

            if (question.getState() != QuestionState.CORRECT) {
                tabTitle.setAlpha(0.2f);
                tab.setEnabled(false);
            }
            if (question.isStoryViewed()) {
                tabTitle.setTextColor(getResources().getColor(R.color.gold));
            }

            return tab;
        }

        @Override
        public void tabSelected(final View tab) {
            tab.findViewById(R.id.hexagon).setVisibility(View.VISIBLE);
            ((TextView)tab.findViewById(R.id.tab_title)).setTextSize(35);
            previousColor = ((TextView)tab.findViewById(R.id.tab_title)).getCurrentTextColor();
            ((TextView)tab.findViewById(R.id.tab_title)).setTextColor(getResources().getColor(R.color.gold));
        }

        @Override
        public void tabUnselected(final View tab) {
            tab.findViewById(R.id.hexagon).setVisibility(View.INVISIBLE);
            ((TextView)tab.findViewById(R.id.tab_title)).setTextSize(16);
            ((TextView)tab.findViewById(R.id.tab_title)).setTextColor(previousColor);
        }
    }

    private void initRecycle() {
        mRecyclerView = (RecyclerView) findViewById(R.id.knowledgeList);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new KnowledgeAdapter(mQuestions, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new OverlapDecoration(this));
    }

    public void openCellInfo(Question pQuestion) {

        findViewById(R.id.description_fragment).setVisibility(View.VISIBLE);
        mViewPager.setCurrentItem(pQuestion.getLocal_id(), true);
    }

    @Override
    public void onClick(final View pView) {
        switch(pView.getId()) {
            case R.id.home:
                finish();
                break;
            case R.id.home_fragment:
                findViewById(R.id.description_fragment).setVisibility(View.GONE);
                break;
        }
    }

    public class OverlapDecoration extends RecyclerView.ItemDecoration {

        private int vertOverlap = -20;

        public OverlapDecoration(Context context) {
            Resources r = context.getResources();
            vertOverlap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                          r.getDimension(R.dimen.knowledge_overlap),
                                                          r.getDisplayMetrics());
        }

        @Override
        public void getItemOffsets (Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            Boolean tag = null;
            tag = (Boolean) view.getTag();
            if (tag == null) {
                outRect.set(0, vertOverlap, 0, 0);
            }

        }
    }
}
