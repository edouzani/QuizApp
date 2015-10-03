package com.almadev.znaniesila;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
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
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.Constants;
import com.astuetz.PagerSlidingTabStrip;

import java.util.List;

public class KnowledgeActivity extends Activity implements View.OnClickListener {

    private RecyclerView         mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Category             mCategory;
    private ViewPager            mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private List<Question>       mQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        Intent intent = getIntent();
        mCategory = (Category) intent.getSerializableExtra(Constants.CATEGORY);
        mQuestions = QuizHolder.getInstance(this).getQuiz(mCategory.getCategory_id()).getQuestions();
        ((TextView) findViewById(R.id.catname)).setText(mCategory.getCategory_name());

        findViewById(R.id.home).setOnClickListener(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), this);

        mViewPager = (ViewPager) findViewById(R.id.containerPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);
        tabs.setOnPageChangeListener(new PagerChangeListener());

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
                    for (int i = mQuestions.size(); i > position; i--) {
                        if (mQuestions.get(i).getState() == QuestionState.CORRECT) {
                            mViewPager.setCurrentItem(i);
                            return;
                        }
                    }
                }
            } else {
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

        public SectionsPagerAdapter(FragmentManager fm, Context pContext) {
            super(fm);
            context = pContext;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            String story = mQuestions.get(position).getStory();
            return KnowledgeFragment.newInstance(story == null ? mQuestions.get(position).getCorrect_ans_explanation() : story, "");
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
            if (mQuestions.get(position).getState() != QuestionState.CORRECT) {
                tab.findViewById(R.id.psts_tab_title).setAlpha(0.2f);
                tab.setEnabled(false);
            }
            return tab;
//            View root = inflater.inflate(R.layout.fragment_knowledge, parent, false);
//            return root;
        }

        @Override
        public void tabSelected(final View tab) {
            tab.findViewById(R.id.hexagon).setVisibility(View.VISIBLE);
            ((TextView)tab.findViewById(R.id.psts_tab_title)).setTextSize(40);
        }

        @Override
        public void tabUnselected(final View tab) {
            tab.findViewById(R.id.hexagon).setVisibility(View.INVISIBLE);
            ((TextView)tab.findViewById(R.id.psts_tab_title)).setTextSize(20);
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
