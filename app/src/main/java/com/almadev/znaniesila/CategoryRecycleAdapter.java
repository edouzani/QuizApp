package com.almadev.znaniesila;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.almadev.znaniesila.events.NeedUpdateQuizesEvent;
import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.QuestionState;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Aleksey on 20.11.2015.
 */
public class CategoryRecycleAdapter extends android.support.v7.widget.RecyclerView.Adapter {

    interface CategoryClickListener {
        void onClick(Category category, boolean payCat);
    }

    private List<Category>         mCategories;
    private WeakReference<Context> wContext;
    private boolean                isPayCats;
    private int lastPosition = -1;
    private List<WeakReference<CategoryClickListener>> listeners = new LinkedList<>();
    private boolean isBrokenContent = false;

    public CategoryRecycleAdapter(WeakReference<Context> wContext, List<Category> pCategories) {
        mCategories = pCategories;
        this.wContext = wContext;
        isBrokenContent = false;
    }

    public void addClickListener(WeakReference<CategoryClickListener> listener) {
        listeners.add(listener);
    }

    private LayoutInflater getLayoutInflater(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater inflater = getLayoutInflater(parent);
        View line = null;
        line = inflater.inflate(R.layout.quiz_category_item, parent, false);

        return new CategoryViewHolder(line);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        CategoryViewHolder mCategoryViewHolder = (CategoryViewHolder) holder;
        mCategoryViewHolder.render(mCategories.get(position), isPayCats);
//        setAnimation(mCategoryViewHolder.getRoot(), position);
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        if (viewToAnimate == null) {
            return;
        }
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(wContext.get(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void setPayCats(boolean value) {
        this.isPayCats = value;
    }

    public void setItems(List<Category> items) {
        mCategories = items;
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private ImageView   img;
        private TextView    name;
        private TextView    description;
        private TextView    record;
        private TextView    recordText;
        private TextView    numberAnswered;
        private TextView    price;
        private View        root;
        private ProgressBar progressBar;

        public View getRoot() {
            return root;
        }

        public CategoryViewHolder(final View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.category_item_layout);
            img = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            description = (TextView) itemView.findViewById(R.id.description);
            record = (TextView) itemView.findViewById(R.id.record_value);
            recordText = (TextView) itemView.findViewById(R.id.record_text_view);
            price = (TextView) itemView.findViewById(R.id.price);
            numberAnswered = (TextView) itemView.findViewById(R.id.answeredQuestions);
            progressBar = (ProgressBar) root.findViewById(R.id.category_progress_bar);

        }

        public void render(final Category category, final boolean isPayCat) {
            if (img.getDrawable() != null) {
                img.getDrawable().setCallback(null);
            }

            Quiz qQuiz = QuizHolder.getInstance(root.getContext()).getQuiz(category.getCategory_id());
            if (qQuiz == null) {
                return;
            }

            root.setBackgroundColor(Color.parseColor("#" + category.getCategory_color().trim()));
            name.setText(category.getCategory_name());

            if (!isPayCat) {
                recordText.setVisibility(View.VISIBLE);
                price.setVisibility(View.INVISIBLE);
                record.setVisibility(View.VISIBLE);
                SharedPreferences preferences = root.getContext().getSharedPreferences(HAFinalScreen.HIGH_SCORES, Context.MODE_PRIVATE);
                int recordScore = preferences.getInt(category.getCategory_id(), 0);
                record.setText("" + recordScore);
            } else {
                recordText.setVisibility(View.INVISIBLE);
                record.setVisibility(View.INVISIBLE);
                price.setVisibility(View.VISIBLE);
                price.setText(category.getPrice());
            }

            numberAnswered.setText(qQuiz.getAnsweredQuestions() + "/" + qQuiz.getQuestions().size());
            description.setText(category.getCategory_description());

            progressBar.setMax(qQuiz.getQuestions().size());
            progressBar.setProgress(qQuiz.getAnsweredQuestions());

            try {
                Drawable d = Drawable.createFromStream(wContext.get().getAssets().open(
                        category.getCategory_image_path()), null);
                img.setImageDrawable(d);

            } catch (Exception e) {
                e.printStackTrace();
            }

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View pView) {
                    for (WeakReference<CategoryClickListener> wListener : listeners) {
                        wListener.get().onClick(category, isPayCat);
                    }
                }
            });
        }
    }
}
