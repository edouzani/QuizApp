package com.almadev.znaniesila;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.Constants;

public class KnowledgeActivity extends Activity implements View.OnClickListener {

    private RecyclerView         mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge);

        Intent intent = getIntent();
        mCategory = (Category) intent.getSerializableExtra(Constants.CATEGORY);
        ((TextView)findViewById(R.id.catname)).setText(mCategory.getCategory_name());
        initRecycle();
        findViewById(R.id.home).setOnClickListener(this);
    }

    private void initRecycle() {
        mRecyclerView = (RecyclerView) findViewById(R.id.knowledgeList);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new KnowledgeAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new OverlapDecoration(this));
    }

    @Override
    public void onClick(final View pView) {
        switch(pView.getId()) {
            case R.id.home:
                finish();
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
