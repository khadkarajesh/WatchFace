package com.rajesh.watchfacetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by rajesh on 3/4/16.
 */
public class MyView extends View {
    Paint paint;

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        Paint myPaint = new Paint();
        myPaint.setColor(Color.YELLOW);
        myPaint.setStrokeWidth(10);
        myPaint.setStyle(Paint.Style.FILL);
        canvas.drawColor(Color.RED);
        canvas.drawRect(100, 100, 300, 300, myPaint);

        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.rajesh), 200, 200, false);
        canvas.drawBitmap(bitmap, 100, 100, null);
        //Path path=
        //canvas.clipPath()

        //canvas.drawColor(Color.RED);
    }
}
