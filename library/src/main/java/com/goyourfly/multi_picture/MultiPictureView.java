package com.goyourfly.multi_picture;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.goyourfly.multiple_image.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class MultiPictureView extends FrameLayout {

    public MultiPictureView(Context context) {
        this(context, null);
    }

    public MultiPictureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiPictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.MultiPictureView, defStyleAttr, 0);
        try {
            span = typeArray.getInteger(R.styleable.MultiPictureView_span, span);
            ratio = typeArray.getFloat(R.styleable.MultiPictureView_ratio, ratio);
            space = (int) typeArray.getDimension(R.styleable.MultiPictureView_space, space);
            imageLayoutMode = typeArray.getInteger(R.styleable.MultiPictureView_imageLayoutMode, imageLayoutMode);
            max = typeArray.getInteger(R.styleable.MultiPictureView_max, max);
            editable = typeArray.getBoolean(R.styleable.MultiPictureView_editable, editable);
            deleteDrawableId = typeArray.getResourceId(R.styleable.MultiPictureView_deleteDrawable, deleteDrawableId);
            addDrawableId = typeArray.getResourceId(R.styleable.MultiPictureView_addDrawable, addDrawableId);
        } finally {
            typeArray.recycle();
        }

        setDeleteResource(deleteDrawableId);
        refresh();
    }

    public interface ItemClickCallback {
        void onItemClicked(View view, int index, ArrayList<Uri> uris);
    }

    public interface AddClickCallback {
        void onAddClick(View view);
    }

    public interface DeleteClickCallback {
        void onDeleted(View view, int index);
    }

    /**
     * 共两种布局方式
     */
    public interface ImageLayoutMode {
        int DYNAMIC = 1;
        int STATIC = 2;
    }

    public static void setImageLoader(ImageLoader imageLoader) {
        Instance.imageLoader = imageLoader;
    }

    private int space = toPx(8);

    // 每行最多显示多少张
    private int span = 3;

    // 布局方式，动态和固定
    private int imageLayoutMode = ImageLayoutMode.STATIC;

    // 最多显示图片个数
    private int max = 9;

    // 横纵比
    private float ratio = 1F;


    // 删除图标
    private int deleteDrawableId = R.drawable.ic_multiple_image_view_delete;

    // 添加图标
    private int addDrawableId = R.drawable.ic_multiple_image_view_add;


    ItemClickCallback itemClickCallback;

    DeleteClickCallback deleteClickCallback = new DeleteClickCallback() {
        @Override
        public void onDeleted(View view, int index) {
            if (editable) {
                removeItem(index);
            }
        }
    };


    AddClickCallback addClickCallback;


    private ArrayList<Uri> imageList = new ArrayList<>();
    // 测量后实际要显示的行
    private int columnMeasure = 0;
    private int rowMeasure = 0;
    private float imageWidthMeasure = 0F;
    private float imageHeightMeasure = 0F;
    // 图片的Padding
    private int imagePaddingMeasure = 0;

    // 是否可编辑
    private boolean editable = false;

    private Bitmap deleteBitmap;

    /**
     * 将所有的图片添加到FrameLayout中
     */
    void setupView() {
        removeAllViews();

        for (int i = 0; i < getNeedViewCount(); i++) {
            final ImageView image = generateImage(i);
            addView(image);
            final int index = i;
            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Uri> arrayList = new ArrayList<Uri>();
                    arrayList.addAll(imageList);
                    if (itemClickCallback != null) {
                        itemClickCallback.onItemClicked(image, index, arrayList);
                    }
                }
            });
        }
        if (shouldDrawAddView()) {
            final ImageView image = generateImage(-1);
            image.setImageResource(addDrawableId);
            addView(image);
            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (addClickCallback != null) {
                        addClickCallback.onAddClick(image);
                    }
                }
            });
        }
    }


    ImageView generateImage(int index) {
        final Bitmap bitmap;
        if (editable)
            bitmap = deleteBitmap;
        else
            bitmap = null;
        CustomImageView image = new CustomImageView(getContext(), index, bitmap, deleteClickCallback);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (editable) {
            image.setPadding(imagePaddingMeasure, imagePaddingMeasure, imagePaddingMeasure, imagePaddingMeasure);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                image.setCropToPadding(true);
            }
        }
        return image;
    }

    void measureImageSize(int width) {
        int imageSize = 0;
        if (imageLayoutMode == ImageLayoutMode.STATIC) {
            columnMeasure = span;
        } else {
            // 取一个合理的column
            columnMeasure = (int) Math.min(Math.ceil(Math.sqrt(getChildCount())), span);
        }
        int flag;
        if (getChildCount() % columnMeasure == 0) flag = 0;
        else flag = 1;
        rowMeasure = getChildCount() / columnMeasure + flag;
        imageSize = (width - space * (columnMeasure - 1)) / columnMeasure;
        imageWidthMeasure = imageSize;
        imageHeightMeasure = imageWidthMeasure / ratio;
    }

    private int getNeedViewCount() {
        return Math.min(getCount(), max);
    }

    private boolean shouldDrawAddView() {
        return editable && getCount() < max;
    }


    public void setDeleteResource(int id) {
        this.deleteDrawableId = id;
        try {
            this.deleteBitmap = drawableToBitmap(getResources().getDrawable(id));
            if (deleteBitmap != null) {
                imagePaddingMeasure = deleteBitmap.getWidth() / 2;
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            this.deleteBitmap = null;
        }
    }

    public void setAddResource(int id) {
        this.addDrawableId = id;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        setupView();
        requestLayout();
    }

    public boolean isEditable() {
        return editable;
    }

    public ArrayList<Uri> getList() {
        return imageList;
    }

    public void refresh() {
        setupView();
        requestLayout();
    }

    public void setList(List<Uri> list) {
        imageList.clear();
        imageList.addAll(list);
        refresh();
    }

    public int getCount() {
        return imageList.size();
    }

    public void addItem(Uri uri) {
        addItem(uri, true);
    }

    public void addItem(Uri uri, boolean refresh) {
        imageList.add(uri);
        if (refresh)
            refresh();
    }

    public void addItem(List<Uri> uri) {
        imageList.addAll(uri);
        refresh();
    }

    public void clearItem() {
        imageList.clear();
        refresh();
    }

    public void removeItem(int index, boolean refresh) {
        imageList.remove(index);
        if (refresh)
            refresh();
    }

    public void removeItem(int index) {
        removeItem(index, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (isInEditMode()) {
            setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), toPx(60));
            return;
        }
        if (getChildCount() == 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        measureImageSize(MeasureSpec.getSize(widthMeasureSpec));
        final int width = (int) (imageWidthMeasure * columnMeasure + (columnMeasure - 1) * space);
        final int height = (int) (imageHeightMeasure * rowMeasure + (rowMeasure - 1) * space);
        setMeasuredDimension(width, height);

        for (int i = 0; i < getChildCount(); i++) {
            final ImageView child = (ImageView) getChildAt(i);
            if (child.getVisibility() == GONE)
                return;
            final int measureWidth = MeasureSpec.makeMeasureSpec((int) imageWidthMeasure, MeasureSpec.EXACTLY);
            final int measureHeight = MeasureSpec.makeMeasureSpec((int) imageHeightMeasure, MeasureSpec.EXACTLY);
            child.measure(measureWidth, measureHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                return;

            final int horizontalIndex = i % columnMeasure;
            final int verticalIndex = i / columnMeasure;
            // 左边距
            final int l = (int) (horizontalIndex * imageWidthMeasure + horizontalIndex * space);
            // 上边距
            final int t = (int) (verticalIndex * imageHeightMeasure + verticalIndex * space);

            // 右边
            final int r = (int) (l + imageWidthMeasure);
            // 下边
            final int b = (int) (t + imageHeightMeasure);

            child.layout(l, t, r, b);

            if (i < imageList.size()) {
                bindImage((ImageView) child, imageList.get(i));
            }
        }
    }

    Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null)
            return null;
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 将DP转换为PX
     */
    private int toPx(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private final String KEY_IMAGES = "key_images";
    private final String KEY_DEFAULT = "key_default";

    private void bindImage(ImageView imageView, Uri uri) {
        if (Instance.imageLoader == null)
            throw new NullPointerException("Please call MultipleImageView.setImageLoader(...) at least one times ");
        Instance.imageLoader.loadImage(imageView, uri);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEFAULT, super.onSaveInstanceState());
        if (imageList.size() > 0) {
            bundle.putParcelableArrayList(KEY_IMAGES, imageList);
        }
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            imageList.clear();
            ArrayList<Uri> list = ((Bundle) state).getParcelableArrayList(KEY_IMAGES);
            imageList.addAll(list);
            super.onRestoreInstanceState(((Bundle) state).getParcelable(KEY_DEFAULT));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void requestLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!isInLayout()) {
                super.requestLayout();
            }
            return;
        }
        super.requestLayout();
    }

    public void setAddClickCallback(AddClickCallback addClickCallback) {
        this.addClickCallback = addClickCallback;
    }

    public AddClickCallback getAddClickCallback() {
        return addClickCallback;
    }

    public void setItemClickCallback(ItemClickCallback itemClickCallback) {
        this.itemClickCallback = itemClickCallback;
    }

    public ItemClickCallback getItemClickCallback() {
        return itemClickCallback;
    }

    public void setDeleteClickCallback(DeleteClickCallback deleteClickCallback) {
        this.deleteClickCallback = deleteClickCallback;
    }

    public DeleteClickCallback getDeleteClickCallback() {
        return deleteClickCallback;
    }


}
