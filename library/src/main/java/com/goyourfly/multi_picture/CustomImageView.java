package com.goyourfly.multi_picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;


/**
 * @author 诸葛不亮
 * @version 1.0
 * @description
 */

public class CustomImageView extends ImageView {

    private float startX;
    private float startY;
    private float moveX;
    private float moveY;
    private long startTime;
    private final int CLICK_ACTION_THRESHHOLD = 20;
    private boolean isTouching;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect deleteRect = new Rect();
    private final int index;
    private final Bitmap bitmap;
    private final MultiPictureView.DeleteClickCallback deleteCallback;

    public CustomImageView(Context context, int index, Bitmap bitmap, MultiPictureView.DeleteClickCallback deleteCallback) {
        super(context);
        this.index = index;
        this.bitmap = bitmap;
        this.deleteCallback = deleteCallback;
    }

    protected void onDraw(@NotNull Canvas canvas) {
        super.onDraw(canvas);
        if (this.index >= 0 && this.bitmap != null && !this.bitmap.isRecycled()) {
            this.deleteRect.set(this.getWidth() - this.bitmap.getWidth(), 0, this.getWidth(), this.bitmap.getHeight());
            canvas.drawBitmap(this.bitmap, (float) (this.getWidth() - this.bitmap.getWidth()), 0.0F, this.paint);
        }
    }

    public boolean performClick() {
        return super.performClick();
    }

    public boolean onTouchEvent(@NotNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.isTouching = true;
                this.startX = event.getX();
                this.startY = event.getY();
                this.moveX = this.startX;
                this.moveY = this.startY;
                this.startTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                if (this.isTouching && this.isAClick(this.startX, endX, this.startY, endY)) {
                    if (this.deleteRect.contains((int) event.getX(), (int) event.getY())) {
                        if (deleteCallback != null) {
                            deleteCallback.onDeleted(CustomImageView.this, index);
                        }
                    } else {
                        this.performClick();
                    }
                    return false;
                }

                this.isTouching = false;
                break;
            case MotionEvent.ACTION_MOVE:
                this.moveX = event.getX();
                this.moveY = event.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
                this.isTouching = false;
        }

        return true;
    }

    private final boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return differenceX <= (float) this.CLICK_ACTION_THRESHHOLD && differenceY <= (float) this.CLICK_ACTION_THRESHHOLD;
    }

    public void requestLayout() {
        if (Build.VERSION.SDK_INT >= 18 && !this.isInLayout()) {
            super.requestLayout();
        }

    }

}
