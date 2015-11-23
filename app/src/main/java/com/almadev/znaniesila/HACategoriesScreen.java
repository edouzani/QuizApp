package com.almadev.znaniesila;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.almadev.znaniesila.events.NeedUpdateQuizesEvent;
import com.almadev.znaniesila.model.Category;
import com.almadev.znaniesila.model.Quiz;
import com.almadev.znaniesila.model.QuizHolder;
import com.almadev.znaniesila.utils.Constants;

import de.greenrobot.event.EventBus;
import it.gmariotti.recyclerview.itemanimator.SlideInOutLeftItemAnimator;

public class HACategoriesScreen extends Activity implements View.OnClickListener, CategoryRecycleAdapter.CategoryClickListener {

    private static final String TAG = "CATEGORIES_SCREEN";
    private IabHelper mHelper;
    public static final int BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7;

    private CategoryRecycleAdapter   mAdapter;
    private SharedPreferences mPrefsmanager;
    private Boolean           adsDisabledAfterPurchase;
    private Boolean           adSupportEnabled;
    private boolean           isKnowledgeCats;
    private List<Category>    mListItems;
    private TextView          mTitle;
    private List<String>      additionalSkuList;
    private Context           pContext;
    private boolean haveCategories = true;
    private RecyclerView mRecyclerView;

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

        findViewById(R.id.home).setOnClickListener(this);
        adSupportEnabled = mPrefsmanager.getBoolean(Constants.AD_SUPPORT_NEEDED, false);
        adsDisabledAfterPurchase = mPrefsmanager.getBoolean(Constants.ADS_DISABLED_AFTER_PURCHASE, false);

        if (QuizHolder.getInstance(this).getCategories() == null ||
                QuizHolder.getInstance(this).getCategories().getCategories() == null) {
            Toast.makeText(this, "Ошибка при загрузке категорий. Возможно отсутствует интернет. Включите интернет и перезапустите приложение.", Toast.LENGTH_LONG).show();
            haveCategories = false;
            return;
        }
        fetchCategories();
        initRecycle();


        if (QuizHolder.getInstance(this).getPassedCategories().size() == 0) {
            findViewById(R.id.passed).setVisibility(View.GONE);
        }
        findViewById(R.id.purchasable_cats).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.passed).setOnClickListener(this);
        findViewById(R.id.restore).setOnClickListener(this);

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText(R.string.cats_choose_title);
    }

    private void fetchCategories() {
        mListItems = new LinkedList<>();

//        QuizHolder.getInstance(this).deleteQuiz("3");

        for (Category c : QuizHolder.getInstance(this).getUnPassedCategories()) {
            if (c.getProductIdentifier() == null || c.getProductIdentifier().isEmpty() ||
                    c.isPurchased()) {

                if (c == null ||
                        QuizHolder.getInstance(this).getQuiz(c.getCategory_id()) == null ||
                        QuizHolder.getInstance(this).getQuiz(c.getCategory_id()).getQuestions().size() == 0) {
                    if (c != null) {
                        QuizHolder.getInstance(this).deleteQuiz(c.getCategory_id());
                    }
                    EventBus.getDefault().post(new NeedUpdateQuizesEvent(QuizHolder.getQuizVersion()));
                } else {
                    mListItems.add(c);
                }
            }
        }

        Collections.sort(mListItems, new CategoryComparator());
    }


    private void initRecycle() {
        mRecyclerView = (RecyclerView) findViewById(R.id.catsList);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mAdapter = new CategoryRecycleAdapter(new WeakReference<Context>(this), mListItems);
        mAdapter.addClickListener(new WeakReference<CategoryRecycleAdapter.CategoryClickListener>(this));
        mRecyclerView.setItemAnimator(new SlideInOutLeftItemAnimator(mRecyclerView));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (haveCategories) {
            initIAB(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(final Category category, final boolean payCat) {
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
                mAdapter.setPayCats(true);
                mAdapter.setItems(QuizHolder.getInstance(this).getPurchasableCategories());
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
                mAdapter.setPayCats(false);
                mAdapter.setItems(mListItems);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.passed:
                mTitle.setText(R.string.cats_passed_title);
                findViewById(R.id.passed).setVisibility(View.INVISIBLE);
                findViewById(R.id.home).setVisibility(View.INVISIBLE);
                findViewById(R.id.back).setVisibility(View.VISIBLE);
                mAdapter.setPayCats(false);
                mAdapter.setItems(QuizHolder.getInstance(this).getPassedCategories());
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

                        return;
                    } else {
                        // Hooray, IAB is fully set up!
                        Log.d(TAG, "Setup is sucessful " + result);

                        additionalSkuList = new ArrayList<>();
                        final List<Category> payCats = new LinkedList<Category>();

                        if (pContext == null || QuizHolder.getInstance(pContext) == null ||
                                QuizHolder.getInstance(pContext).getCategories() == null ||
                                QuizHolder.getInstance(pContext).getCategories().getCategories() == null) {
                            return;
                        }
                        for (Category cat : QuizHolder.getInstance(pContext).getCategories().getCategories()) {
                            if (cat.getProductIdentifier() != null && !cat.getProductIdentifier().isEmpty()) {
                                additionalSkuList.add(cat.getProductIdentifier());
                                payCats.add(cat);
                            }
                        }

                        if (mHelper == null) {
                            return;
                        }

                        mHelper.queryInventoryAsync(true, additionalSkuList, new IabHelper.QueryInventoryFinishedListener() {
                            @Override
                            public void onQueryInventoryFinished(final IabResult result, final Inventory inv) {
                                Log.e(TAG, result.getMessage());
                                if (inv == null) {
                                    return;
                                }
                                for (Category c : payCats) {
                                    if (inv.hasDetails(c.getProductIdentifier())) {
                                        String newPrice = inv.getSkuDetails(c.getProductIdentifier()).getPrice().replaceAll("\\s", "\n");
                                        c.setPrice(newPrice);
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