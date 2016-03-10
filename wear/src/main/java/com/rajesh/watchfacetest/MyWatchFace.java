package com.rajesh.watchfacetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class MyWatchFace extends CanvasWatchFaceService {

    private static final String TAG = MyWatchFace.class.getSimpleName();
    private static final int PERCENTAGE_TO_DRAW_LINE = 40;

    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    private class MyEngine extends CanvasWatchFaceService.Engine {
        private static final String DATE_FORMAT = "EEEE d MMMM yyyy";
        private static final int STROKE_WIDTH = 5;
        private static final int STROKE_TIME = 2;
        private static final int STROKE_WIDTH_DATE = 1;

        private static final int COLON_MESSAGE = 1;

        private static final String COLON = ":";

        Paint paint, paintWhite, paintBlack,
                paintDate, messagePaint, messageTextPaint,
                messageLabelTextPaint, weatherImageIconPaint, rectFillerPaint, timePaint;

        int pointX, pointY, pointX1, pointY1;
        int backgroundColor;
        private Bitmap bitmapMessage, bitmapVisibility, bitmapWeather;
        private boolean colonVisibility = true;
        private String currentDate = "";
        boolean receiverRegister = false;
        private TimeZoneChangeReceiver timeZoneChangeReceiver;

        private Date date;

        Calendar calendar;

        int width, height;
        private GoogleApiClient googleApiClient;

        private MyHandler handler;
        private boolean is24Hour;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            createGoogleApiClient();

            timeZoneChangeReceiver = new TimeZoneChangeReceiver();
            handler = new MyHandler();

            initFormat();

            bitmapMessage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_message_black_18dp), 30, 30, false);
            bitmapVisibility = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_visibility_black_18dp), 30, 30, false);
            bitmapWeather = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.art_clear), 30, 30, false);


            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.light_grey));
            paint.setStyle(Paint.Style.STROKE);
            paint.setTextSize(30);

            timePaint = new Paint();
            timePaint.setAntiAlias(true);
            timePaint.setColor(Color.WHITE);
            timePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            timePaint.setStrokeWidth(STROKE_TIME);
            timePaint.setTextSize(30);

            paintDate = new Paint();
            paintDate.setAntiAlias(true);
            paintDate.setColor(Color.WHITE);
            paintDate.setStyle(Paint.Style.FILL_AND_STROKE);
            paintDate.setStrokeWidth(STROKE_WIDTH_DATE);
            paintDate.setTextSize(20);


            paintWhite = new Paint();
            paintWhite.setColor(Color.BLUE);
            paintWhite.setAntiAlias(true);
            paintWhite.setStyle(Paint.Style.STROKE);
            paintWhite.setStrokeWidth(STROKE_WIDTH);

            paintBlack = new Paint();
            paintBlack.setColor(Color.RED);
            paintBlack.setAntiAlias(true);
            paintBlack.setStyle(Paint.Style.STROKE);
            paintBlack.setStrokeWidth(STROKE_WIDTH);

            messagePaint = new Paint();
            messagePaint.setColor(Color.WHITE);
            messagePaint.setAntiAlias(true);
            messagePaint.setStyle(Paint.Style.FILL);
            messagePaint.setTextSize(10);

            messageTextPaint = new Paint();
            messageTextPaint.setColor(Color.BLACK);
            messageTextPaint.setAntiAlias(true);
            messageTextPaint.setTextSize(10);


            messageLabelTextPaint = new Paint();
            messageLabelTextPaint.setColor(Color.BLACK);
            messageLabelTextPaint.setAntiAlias(true);
            messageLabelTextPaint.setTextSize(10);

            weatherImageIconPaint = new Paint();
            weatherImageIconPaint.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP));
            weatherImageIconPaint.setAntiAlias(true);

            rectFillerPaint = new Paint();
            rectFillerPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.watchFaceColor));
            rectFillerPaint.setAntiAlias(true);
            rectFillerPaint.setStyle(Paint.Style.FILL);


            backgroundColor = ContextCompat.getColor(getBaseContext(), R.color.colorPrimary);

        }

        private void createGoogleApiClient() {
            googleApiClient = new GoogleApiClient.Builder(MyWatchFace.this).addApi(Wearable.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {

                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                }
            }).build();
        }

        private void initFormat() {
            calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            date = new Date();
            date.setTime(System.currentTimeMillis());
            calendar.setTime(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            currentDate = simpleDateFormat.format(date);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            calendar.setTimeInMillis(System.currentTimeMillis());
            if (isInAmbientMode()) {
                backgroundColor = ContextCompat.getColor(MyWatchFace.this, R.color.black);
            }

            canvas.drawColor(backgroundColor);

            if (!isInAmbientMode()) {
                //setPaintAntialias();
                Log.d(TAG, "onDraw: not in ambient mode");
                backgroundColor = ContextCompat.getColor(MyWatchFace.this, R.color.white);


                calendar.setTimeInMillis(System.currentTimeMillis());
                date.setTime(System.currentTimeMillis());

                is24Hour = DateFormat.is24HourFormat(MyWatchFace.this);

                Path path = new Path();

                width = bounds.width();
                height = bounds.height();


                pointX = getPercentageOfDimension(width, PERCENTAGE_TO_DRAW_LINE);
                pointY = height / 2;

                pointX1 = width - getPercentageOfDimension(width, PERCENTAGE_TO_DRAW_LINE);
                pointY1 = height / 2;


                RectF rectFiller = new RectF();
                rectFiller.set(0, 0, width, height / 2);
                canvas.drawRect(rectFiller, rectFillerPaint);

                //draw first horizantal line to center  left
                path.moveTo(0, height / 2);
                path.lineTo(pointX, pointY + 2);
                paint.setStrokeWidth(2);
                canvas.drawPath(path, paint);

                //draw second horizantal line to center right
                path.moveTo(width, height / 2);
                path.lineTo(pointX1, pointY1 + 2);
                paint.setStrokeWidth(2);
                canvas.drawPath(path, paint);

                paint.setStrokeWidth(STROKE_WIDTH);
                path.moveTo(pointX - 2.8f, pointY);
                path.cubicTo(pointX - 2.8f, pointY, width / 2, height / 2 + getPercentageOfDimension(height, 70), pointX1 + 2.8f, pointY1);

                Path path1 = new Path();
                path1.moveTo(pointX - 2.8f, pointY);
                path1.cubicTo(pointX - 2.8f, pointY, width / 2, height / 2 + getPercentageOfDimension(height, 68), pointX1 + 2.8f, pointY1);
                canvas.drawPath(path1, rectFillerPaint);


                drawMessageRect(canvas, width, height);

                showDistanceTravelled(canvas, width, height);

                canvas.drawPath(path, paint);
            }
            showTime(canvas, width, height);
            showDate(canvas, width, height);
        }

        private void setPaintAntialias() {
            boolean inAmbientMode = !isInAmbientMode();
            paint.setAntiAlias(inAmbientMode);
            paintWhite.setAntiAlias(inAmbientMode);
            paintBlack.setAntiAlias(inAmbientMode);
            paintDate.setAntiAlias(inAmbientMode);
            messagePaint.setAntiAlias(inAmbientMode);
            messageTextPaint.setAntiAlias(inAmbientMode);
            messageLabelTextPaint.setAntiAlias(inAmbientMode);
            weatherImageIconPaint.setAntiAlias(inAmbientMode);
            rectFillerPaint.setAntiAlias(inAmbientMode);
            timePaint.setAntiAlias(inAmbientMode);
        }

        private void drawMessageRect(Canvas canvas, int width, int height) {
            RectF messageRectF = new RectF();

            int messageRectFLeft = getPercentageOfDimension(width, 20);
            int messageRectFTop = height / 2 + getPercentageOfDimension(height, 18);

            messageRectF.set(messageRectFLeft, messageRectFTop, messageRectFLeft + 50, messageRectFTop + 50);

            canvas.drawRect(messageRectF, messagePaint);
            canvas.drawBitmap(bitmapMessage, messageRectF.centerX() - 15, messageRectFTop, null);

            drawWeatherRect(canvas, messageRectF, messageRectFLeft);
        }

        private void drawWeatherRect(Canvas canvas, RectF messageRectF, int messageRectFLeft) {
            String messageCount = "23";
            float messageCountSize = messageTextPaint.measureText(messageCount);

            String messageLabel = "Messages";
            float messageLabelWidth = messageTextPaint.measureText(messageLabel);

            canvas.drawText(messageCount, messageRectF.centerX() - messageCountSize / 2, messageRectF.centerY() + 10, messageTextPaint);
            canvas.drawText(messageLabel, messageRectF.centerX() - messageLabelWidth / 2, messageRectF.centerY() + 20, messageLabelTextPaint);

            currentWeatherRect(canvas, messageRectF, messageRectFLeft, messageLabel);
        }

        private void currentWeatherRect(Canvas canvas, RectF messageRectF, int messageRectFLeft, String messageLabel) {
            RectF weatherRectF = new RectF();
            weatherRectF.set(messageRectFLeft + 50 + 10, messageRectF.bottom - 10, messageRectFLeft + 50 + 50 + 10, messageRectF.bottom + 40);
            canvas.drawRect(weatherRectF, messagePaint);

            canvas.drawBitmap(bitmapWeather, weatherRectF.centerX() - 15, weatherRectF.top, weatherImageIconPaint);


            String weatherTemp = "23";
            float weatherTempLength = messageTextPaint.measureText(weatherTemp);

            String weatherLabel = "Sunny";
            float weatherLabelWidth = messageLabelTextPaint.measureText(messageLabel);

            // Log.d(TAG, "drawMessageRect: weather rect width :: " + weatherRectF.width() + "  height::" + weatherRectF.height());

            canvas.drawText(weatherTemp, weatherRectF.centerX() - weatherTempLength / 2, weatherRectF.centerY() + 10, messageTextPaint);
            canvas.drawText(weatherLabel, weatherRectF.centerX() - weatherLabelWidth / 2, weatherRectF.centerY() + 20, messageLabelTextPaint);
        }

        private void showDistanceTravelled(Canvas canvas, int width, int height) {
            RectF messageRectF = new RectF();

            int messageRectFLeft = width - getPercentageOfDimension(width, 20) - 50;
            int messageRectFTop = height / 2 + getPercentageOfDimension(height, 18);

            messageRectF.set(messageRectFLeft, messageRectFTop, messageRectFLeft + 50, messageRectFTop + 50);

            canvas.drawRect(messageRectF, messagePaint);
            canvas.drawBitmap(bitmapMessage, messageRectF.centerX() - 15, messageRectFTop, null);

            String messageCount = "23";
            String messageLabel = "Messages";

            float messageCountSize = messageTextPaint.measureText(messageCount);
            float messageLabelWidth = messageTextPaint.measureText(messageLabel);

            canvas.drawText(messageCount, messageRectF.centerX() - messageCountSize / 2, messageRectF.centerY() + 10, messageTextPaint);
            canvas.drawText(messageLabel, messageRectF.centerX() - messageLabelWidth / 2, messageRectF.centerY() + 20, messageLabelTextPaint);

            distanceMoveRect(canvas, messageRectF, messageRectFLeft);
        }

        private void distanceMoveRect(Canvas canvas, RectF messageRectF, int messageRectFLeft) {
            RectF planeRect = new RectF();
            planeRect.set(messageRectF.bottom - 10, messageRectF.bottom - 10, messageRectFLeft - 60, messageRectF.bottom + 40);
            canvas.drawRect(planeRect, messagePaint);

            canvas.drawBitmap(bitmapVisibility, planeRect.centerX() - 15, planeRect.top, null);

            String planeString = "20";
            String planeLabel = "KM/h";

            float planeStringWidth = messageTextPaint.measureText(planeString);
            float planeLabelWidth = messageTextPaint.measureText(planeLabel);

            canvas.drawText(planeString, planeRect.centerX() - planeStringWidth / 2, planeRect.centerY() + 10, messageTextPaint);
            canvas.drawText(planeLabel, planeRect.centerX() - planeLabelWidth / 2, planeRect.centerY() + 20, messageLabelTextPaint);
        }


        private void showDate(Canvas canvas, int width, int height) {
            float dateSize = paintDate.measureText(currentDate);
            canvas.drawText(currentDate, width / 2 - dateSize / 2, getPercentageOfDimension(height, 80), paintDate);
        }

        private void showTime(Canvas canvas, int width, int height) {
            String hhString = is24Hour ? "" + calendar.get(Calendar.HOUR) : "" + calendar.get(Calendar.HOUR_OF_DAY);
            if (hhString.equals("0")) {
                hhString = hhString.replace("0", "12");
            }
            Log.d(TAG, "showTime: hour " + hhString);
            //hhString = hhString.length() == 1 ? "0" + hhString : hhString;
            float hhWidth = timePaint.measureText(hhString);

            String mmString = "" + calendar.get(Calendar.MINUTE);
            mmString = mmString.length() == 1 ? "0" + mmString : mmString;
            float colonWidth = timePaint.measureText(COLON);

            canvas.drawText(hhString, width / 2 - hhWidth - colonWidth / 2 - 10, getPercentageOfDimension(height, 60), timePaint);
            canvas.drawText(mmString, width / 2 + colonWidth / 2 + 10, getPercentageOfDimension(height, 60), timePaint);
            if (colonVisibility) {
                canvas.drawText(COLON, width / 2 - colonWidth / 2, getPercentageOfDimension(height, 60), timePaint);
            }
            if (!isInAmbientMode()) {
                handler.sendEmptyMessageDelayed(COLON_MESSAGE, 500);
            } else {
                handler.removeMessages(COLON_MESSAGE);
                colonVisibility = true;
                invalidate();
            }
        }


        private int getPercentageOfDimension(int dimen, int percent) {
            return ((dimen / 2) * percent / 100);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            //Log.d(TAG, "onVisibilityChanged: called :: " + visible);
            if (visible) {
                receiverRegister = true;
                registerReceiver();
            } else {
                receiverRegister = false;
                unRegisterReceiver();
            }
        }

        private void registerReceiver() {
            if (receiverRegister) {
                return;
            }
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
            MyWatchFace.this.registerReceiver(timeZoneChangeReceiver, intentFilter);
        }

        private void unRegisterReceiver() {
            if (!receiverRegister) {
                return;
            }
            MyWatchFace.this.unregisterReceiver(timeZoneChangeReceiver);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            invalidate();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
            Log.d(TAG, "onTimeTick:");
            //called in every minute when it is in ambient mode/intreactive mode
        }


        public class MyHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == COLON_MESSAGE) {
                    colonVisibility = colonVisibility ? false : true;
                    invalidate();
                }
            }
        }


        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            if (insets.isRound()) {
                //Log.d(TAG, "onApplyWindowInsets: round");
            } else {
                //Log.d(TAG, "onApplyWindowInsets: square");
            }
        }

        public class TimeZoneChangeReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                initFormat();
                invalidate();
                //Log.d(TAG, "onReceive: called ::" + calendar.getTimeZone());
            }
        }


    }

}
