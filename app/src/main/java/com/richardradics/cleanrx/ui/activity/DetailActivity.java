package com.richardradics.cleanrx.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.view.ViewHelper;
import com.richardradics.cleanrx.R;
import com.richardradics.cleanrx.app.BaseActivity;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.InjectView;

public class DetailActivity extends BaseActivity implements ObservableScrollViewCallbacks, Palette.PaletteAsyncListener {

    public static final String EXTRA_IMAGE = "DetailActivity:image";
    public static final String EXTRA_TITLE = "DetailActivity:title";
    private static boolean TOOLBAR_IS_STICKY = false;
    private static float MAX_TEXT_SCALE_DELTA = 0.3f;


    int mFlexibleSpaceImageHeight;

    @InjectView(R.id.image)
    ImageView image;


    int mToolBarColor;

    @InjectView(R.id.overlay)
    View mOverlayView;

    @InjectView(R.id.scroll)
    ObservableScrollView mScollView;

    @InjectView(R.id.body)
    TextView bodyTextview;

    Integer mActionBarSize;

    private Target bitmapImageViewTarget;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        mActionBarSize = getActionBarSize();

        if (!TOOLBAR_IS_STICKY) {
            toolbar.setBackgroundColor(Color.TRANSPARENT);
        }

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mToolBarColor = getResources().getColor(R.color.primary);
        image = (ImageView) findViewById(R.id.image);
        ViewCompat.setTransitionName(image, EXTRA_IMAGE);
        titleTextView.setText(getIntent().getStringExtra(EXTRA_TITLE));
        bitmapImageViewTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Palette.generateAsync(bitmap, DetailActivity.this);
                image.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                //place your code here
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //place your code here
            }
        };
        Picasso.with(this).load(getIntent().getStringExtra(EXTRA_IMAGE)).into(bitmapImageViewTarget);

        setTitle(null);
        mScollView.setScrollViewCallbacks(this);

        ScrollUtils.addOnGlobalLayoutListener(mScollView, () -> {
            mScollView.scrollTo(0, mFlexibleSpaceImageHeight - mActionBarSize);
            mScollView.scrollTo(0, 1);
        });

    }


    public static void launch(BaseActivity activity, View transitionView, String url, String title) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, EXTRA_IMAGE);
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(EXTRA_IMAGE, url);
        intent.putExtra(EXTRA_TITLE, title);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        float flexibleRange = (float) mFlexibleSpaceImageHeight - mActionBarSize;
        float minOverLayTransitionY = mActionBarSize - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mOverlayView, ScrollUtils.getFloat(-scrollY, minOverLayTransitionY, 0F));
        ViewHelper.setTranslationY(image, ScrollUtils.getFloat(-scrollY / 2, minOverLayTransitionY, 0F));

        // Change alpha of overlay
        ViewHelper.setAlpha(mOverlayView, ScrollUtils.getFloat(scrollY / flexibleRange, 0F, 1F));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0F, MAX_TEXT_SCALE_DELTA);
        ViewHelper.setPivotX(titleTextView, 0F);
        ViewHelper.setPivotY(titleTextView, 0F);
        ViewHelper.setScaleX(titleTextView, scale);
        ViewHelper.setScaleY(titleTextView, scale);

        // Translate title text
        float maxTitleTranslationY = mFlexibleSpaceImageHeight - titleTextView.getHeight() * scale;
        float titleTranslationY = maxTitleTranslationY - scrollY;
        if (TOOLBAR_IS_STICKY) {
            titleTranslationY = Math.max(0, titleTranslationY);
        }
        ViewHelper.setTranslationY(titleTextView, titleTranslationY);

        if (TOOLBAR_IS_STICKY) {
            // Change alpha of toolbar background
            if (-scrollY + mFlexibleSpaceImageHeight <= mActionBarSize) {
                toolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(1F, mToolBarColor));
            } else {
                toolbar.setBackgroundColor(ScrollUtils.getColorWithAlpha(0F, mToolBarColor));
            }
        } else {
            // Translate Toolbar
            if (scrollY < mFlexibleSpaceImageHeight) {
                ViewHelper.setTranslationY(toolbar, 0F);
            } else {
                ViewHelper.setTranslationY(toolbar, -scrollY);
            }
        }

    }


    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public void onGenerated(Palette palette) {
        try {
            if (palette != null) {
                final Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                final Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
                final Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
                final Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
                final Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();

                final Palette.Swatch backgroundAndContentColors = (darkVibrantSwatch != null)
                        ? darkVibrantSwatch : darkMutedSwatch;

                final Palette.Swatch titleAndFabColors = (darkVibrantSwatch != null)
                        ? lightVibrantSwatch : lightMutedSwatch;

                mToolBarColor = backgroundAndContentColors.getRgb();
                bodyTextview.setBackgroundColor(backgroundAndContentColors.getRgb());
                titleTextView.setTextColor(titleAndFabColors.getRgb());
            }
        } catch (Exception e) {

        }
    }
}
