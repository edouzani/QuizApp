package com.almadev.znaniesila;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.almadev.znaniesila.billing.utils.IabHelper;
import com.almadev.znaniesila.billing.utils.IabResult;
import com.almadev.znaniesila.billing.utils.Inventory;
import com.almadev.znaniesila.billing.utils.Purchase;
import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.Constants;
import com.chartboost.sdk.Chartboost;

public class HACategoriesScreen extends ListActivity implements View.OnClickListener {

    private static final String TAG = "CATEGORIES_SCREEN";
    private IabHelper mHelper;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;
    private String SKU_REMOVE_ADS;

    private CategoryAdapter   mAdapter;
    private SharedPreferences mPrefsmanager;
    private Chartboost        cb;
    private Boolean           adsDisabledAfterPurchase;
    private Boolean           adSupportEnabled;
    private boolean           isKnowledgeCats;
    private List<Category>    mListItems;
    private TextView          mTitle;
    private List<String>      additionalSkuList;
    private Context pContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        pContext = this;
        isKnowledgeCats = intent.getBooleanExtra(Constants.CATEGORY_FOR_KNOWLEDGE, false);
        setContentView(R.layout.quiz_categories_layout);

        if (isKnowledgeCats) {
            findViewById(R.id.purchasable_cats).setVisibility(View.GONE);
        }

        mPrefsmanager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        fetchCategories();
        mAdapter = new CategoryAdapter(new WeakReference<Context>(this), mListItems);
        setListAdapter(mAdapter);

        adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED, false);
        adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, false);

        findViewById(R.id.home).setOnClickListener(this);
        findViewById(R.id.purchasable_cats).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.passed).setOnClickListener(this);
        findViewById(R.id.restore).setOnClickListener(this);

        mTitle = (TextView) findViewById(R.id.title);
    }

    private void fetchCategories() {
        mListItems = new LinkedList<>();
        for (Category c : QuizHolder.getInstance(this).getCategories().getCategories()) {
            if (c.getProductIdentifier() == null || c.getProductIdentifier().isEmpty() ||
                    c.isPurchased()) {
                mListItems.add(c);
            }
        }
        Collections.sort(mListItems, new CategoryComparator());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
            this.cb.onStart(this);
            this.cb.startSession();
        }
        initIAB(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
            this.cb.onStop(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase) {
            this.cb.onDestroy(this);
        }
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    public void onBackPressed() {
        if (this.cb != null && this.cb.onBackPressed())
            return;
        else
            super.onBackPressed();
    }

    public void onMoreButtonClick(View view) {
        if (adSupportEnabled && this.cb != null && !adsDisabledAfterPurchase)
            this.cb.showMoreApps();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Category category = mAdapter.getItem(position);
        Intent intent;
        if (isKnowledgeCats) {
            intent = new Intent(this, KnowledgeActivity.class);
            intent.putExtra(Constants.CATEGORY, category);
        } else {
            if (category.isPurchased() == false && category.getProductIdentifier() != null &&
                    !category.getProductIdentifier().isEmpty()) {

                mHelper.launchPurchaseFlow(this, category.getProductIdentifier(), 10001,
                                           mPurchaseFinishedListener, "");
                return;
            }
            intent = new Intent(this, HAQuizScreen.class);
            intent.putExtra(Constants.CATEGORY_ID, category.getCategory_id());
        }

        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(final View pView) {
        switch (pView.getId()) {
            case R.id.home:
                finish();
                break;
            case R.id.purchasable_cats:
                mTitle.setText(R.string.cats_purchasable_title);
                findViewById(R.id.purchasable_cats).setVisibility(View.GONE);
                findViewById(R.id.home).setVisibility(View.INVISIBLE);
                findViewById(R.id.back).setVisibility(View.VISIBLE);
                findViewById(R.id.passed).setVisibility(View.INVISIBLE);
                findViewById(R.id.restore).setVisibility(View.VISIBLE);
                mAdapter.setItems(true, QuizHolder.getInstance(this).getPurchasableCategories());
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.back:
                mTitle.setText(R.string.cats_choose_title);
                findViewById(R.id.purchasable_cats).setVisibility(View.VISIBLE);
                findViewById(R.id.home).setVisibility(View.VISIBLE);
                findViewById(R.id.back).setVisibility(View.GONE);
                findViewById(R.id.passed).setVisibility(View.VISIBLE);
                findViewById(R.id.restore).setVisibility(View.GONE);
                fetchCategories();
                mAdapter.setItems(false, mListItems);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.passed:
                mTitle.setText(R.string.cats_passed_title);
                findViewById(R.id.passed).setVisibility(View.INVISIBLE);
                findViewById(R.id.home).setVisibility(View.INVISIBLE);
                findViewById(R.id.back).setVisibility(View.VISIBLE);
                mAdapter.setItems(false, QuizHolder.getInstance(this).getPassedCategories());
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.restore:
                try {
                    mHelper.queryInventoryAsync(true, additionalSkuList,
                                                mQueryFinishedListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private static class CategoryAdapter extends BaseAdapter {

        private List<Category>         mData;
        private LayoutInflater         sInflater;
        private WeakReference<Context> wContext;
        private boolean                payCats;

        public CategoryAdapter(WeakReference<Context> context, List<Category> data) {
            wContext = context;
            mData = data;
            sInflater = (LayoutInflater) context.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            payCats = false;
        }

        public void setItems(boolean isPayable, List<Category> data) {
            mData = data;
            payCats = isPayable;
        }

        @Override
        public int getCount() {
            return mData != null ? mData.size() : 0;
        }

        @Override
        public Category getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertiView, ViewGroup parent) {
            if (convertiView == null) {
                convertiView = sInflater.inflate(R.layout.quiz_category_item, null);
            }
            View root = convertiView.findViewById(R.id.category_item_layout);

            TextView name = (TextView) convertiView.findViewById(R.id.name);
            TextView description = (TextView) convertiView.findViewById(R.id.description);
            TextView numberAnswered = (TextView) convertiView.findViewById(R.id.answeredQuestions);
            ImageView image = (ImageView) convertiView.findViewById(R.id.image);
            if (image.getDrawable() != null) {
                image.getDrawable().setCallback(null);
            }

            Category qCategory = getItem(position);
            Quiz qQuiz = QuizHolder.getInstance(convertiView.getContext()).getQuiz(qCategory.getCategory_id());
            root.setBackgroundColor(Color.parseColor("#" + qCategory.getCategory_color().trim()));
            name.setText(qCategory.getCategory_name());

            TextView record_value = (TextView) convertiView.findViewById(R.id.record_value);
            if (!payCats) {
                root.findViewById(R.id.record_text_view).setVisibility(View.VISIBLE);
                root.findViewById(R.id.price).setVisibility(View.INVISIBLE);
                record_value.setVisibility(View.VISIBLE);
                SharedPreferences preferences = convertiView.getContext().getSharedPreferences(HAFinalScreen.HIGH_SCORES, MODE_PRIVATE);
                int recordScore = preferences.getInt(qCategory.getCategory_id(), 0);
                record_value.setText("" + recordScore);
            } else {
                root.findViewById(R.id.record_text_view).setVisibility(View.INVISIBLE);
                record_value.setVisibility(View.INVISIBLE);
                root.findViewById(R.id.price).setVisibility(View.VISIBLE);
                ((TextView) root.findViewById(R.id.price)).setText(qCategory.getPrice());
            }

            numberAnswered.setText(qQuiz.getAnsweredQuestions() + "/" + qQuiz.getQuestions().size());
            description.setText(qCategory.getCategory_description());

            ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.category_progress_bar);
            progressBar.setMax(qQuiz.getQuestions().size());
            progressBar.setProgress(qQuiz.getAnsweredQuestions());

            try {
                Drawable d = Drawable.createFromStream(wContext.get().getAssets().open(
                        qCategory.getCategory_image_path()), null);
                image.setImageDrawable(d);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertiView;
        }
    }

    private class CategoryComparator implements Comparator<Category> {

        @Override
        public int compare(Category lhs, Category rhs) {
            if (Integer.valueOf(lhs.getCategory_id()) > Integer.valueOf(rhs.getCategory_id()))
                return 1;
            else
                return -1;
        }

    }

    //In-App billing related stuff

    private void initIAB(final Context pContext) {
        try {
            mHelper = new IabHelper(this, mPrefsmanager.getString(Constants.APPKEY_64BIT, ""));
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    } else {
                        // Hooray, IAB is fully set up!
                        Log.d(TAG, "Setup is sucessful " + result);

                        additionalSkuList = new ArrayList<>();
                        final List<Category> payCats = new LinkedList<Category>();

                        for (Category cat : QuizHolder.getInstance(pContext).getCategories().getCategories()) {
                            if (cat.getProductIdentifier() != null && !cat.getProductIdentifier().isEmpty()) {
                                additionalSkuList.add(cat.getProductIdentifier());
                                payCats.add(cat);
                            }
                        }

                        mHelper.queryInventoryAsync(true, additionalSkuList, new IabHelper.QueryInventoryFinishedListener() {
                            @Override
                            public void onQueryInventoryFinished(final IabResult result, final Inventory inv) {
                                Log.e(TAG, result.getMessage());

                                for (Category c : payCats) {
                                    if (inv.hasDetails(c.getProductIdentifier())) {
                                        String[] priceArray = inv.getSkuDetails(c.getProductIdentifier()).getPrice().split("\\s");
                                        c.setPrice(priceArray[0] + "\n" + priceArray[1]);
                                    }
                                    if (inv.hasPurchase(c.getProductIdentifier())) {
                                        c.setIsPurchased(true);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "In-App billing error : Please note you cannot test inapp billing in emulator and check if you have signed the apk", Toast.LENGTH_SHORT).show();
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing********************* " + result);
                if (result.getResponse() == BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                    Log.d(TAG, "Already purchased " + result);
                    openCategory(purchase.getSku());
                }
            } else {
                Log.d(TAG, "Item purchase Done********************* " + result);
                Toast.makeText(pContext, "Покупка совершена", Toast.LENGTH_LONG).show();
//                disableAds();
                openCategory(purchase.getSku());
            }
        }
    };

    private void openCategory(String sku) {
        for (Category c : QuizHolder.getInstance(pContext).getPurchasableCategories()) {
            if (c.getProductIdentifier().equals(sku)) {
                c.setIsPurchased(true);
                c.setPrice("");
            }
        }
    }

    IabHelper.QueryInventoryFinishedListener
            mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // handle error
                return;
            }
            for (String sku : additionalSkuList) {
                if (inventory.getPurchase(sku) != null) {
                    openCategory(sku);
                    //Temp
                    mHelper.consumeAsync(inventory.getPurchase(sku), mConsumeFinishedListener);
                }
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        Log.d(TAG, requestCode + "==" + resultCode);
        if (mHelper == null || !mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isSuccess()) {
                        Log.d(TAG, "The " + purchase.getSku() + " has been consumed");
                    } else {
                        // handle error
                    }
                }
            };

}