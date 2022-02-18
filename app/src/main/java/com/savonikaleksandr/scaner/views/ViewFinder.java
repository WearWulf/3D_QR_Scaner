package com.savonikaleksandr.scaner.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.Size;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.List;

public class ViewFinder extends ViewfinderView{

    public ViewFinder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {

        /* Определяем размеры, которые переданы в AttributeSet */
        refreshSizes();

        /* Проверяем чтобы все параметры хранили значение */
        if (framingRect == null || previewSize == null) {
            /* Прекращаем дальнейшее выполнение метода */
            return;
        }

        /* Инициализируем frame и previewFrame для отрисовки рамок */

        final Rect frame = framingRect;
        final Size previewSize = this.previewSize;

        final int width = getWidth();
        final int height = getHeight();

        /* Инициализируем width и height для отрисовки рамок по размерам экрана */


        /* Определяем цвет внешней области для дальнейшей отрисовки */
        paint.setColor(resultBitmap != null ? resultColor : maskColor);

        /* Отрисовываем внешнюю область (та, которая за границами прямоугольника) */
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        /* Определяем цвет внутреней рамки для дальнейшей отрисовки */
        paint.setColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));

        /* Определяем значения переменных, которые нужны для отрисовки внутреней рамки */
        int distance = 70, thickness = 5;

        /* Отрисовываем внутренюю рамку (та, которая находиться внутри внешней области) */
        canvas.drawRect(frame.left - thickness, frame.top - thickness, distance + frame.left, frame.top, paint);
        canvas.drawRect(frame.left - thickness, frame.top, frame.left, distance + frame.top, paint);
        canvas.drawRect(frame.right - distance, frame.top - thickness, frame.right + thickness, frame.top, paint);
        canvas.drawRect(frame.right, frame.top, frame.right + thickness, distance + frame.top, paint);
        canvas.drawRect(frame.left - thickness, frame.bottom, distance + frame.left, frame.bottom + thickness, paint);
        canvas.drawRect(frame.left - thickness, frame.bottom - distance, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right - distance, frame.bottom, frame.right + thickness, frame.bottom + thickness, paint);
        canvas.drawRect(frame.right, frame.bottom - distance, frame.right + thickness, frame.bottom, paint);

        if (resultBitmap != null) {
            /* Выводим переданный Bitmap поверх области сканирования (ZXing) */
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            /* Определяем финализированные переменные для вывода возможных точек */
            final float scaleX = frame.width() / (float) frame.width();
            final float scaleY = frame.height() / (float) frame.height();

            /* Определяем финализированные переменные для хранения сторон рамки */
            final int frameLeft = frame.left;
            final int frameTop = frame.top;

            /* Отрисовываем последние точки возможного сканирования */
            if (!lastPossibleResultPoints.isEmpty()) {
                float radius = POINT_SIZE / 2.0f;

                paint.setAlpha(CURRENT_POINT_OPACITY / 2);
                paint.setColor(resultPointColor);

                for (final ResultPoint point : lastPossibleResultPoints) {
                    canvas.drawCircle(
                            (int) (point.getX() * scaleX),
                            (int) (point.getY() * scaleY),
                            radius, paint);
                }

                lastPossibleResultPoints.clear();
            }

            /* Отрисовываем текущие точки возможного сканирования */
            if (!possibleResultPoints.isEmpty()) {
                paint.setAlpha(CURRENT_POINT_OPACITY);
                paint.setColor(resultPointColor);

                for (final ResultPoint point : possibleResultPoints) {
                    canvas.drawCircle(
                            (int) (point.getX() * scaleX),
                            (int) (point.getY() * scaleY),
                            POINT_SIZE, paint);
                }

                /* Перемещаем и очищаем буфер точек возможного сканирования */
                final List<ResultPoint> temp = possibleResultPoints;
                possibleResultPoints = lastPossibleResultPoints;
                lastPossibleResultPoints = temp;
                possibleResultPoints.clear();
            }

            /* Устанавливаем интервал для обновления точек возможного сканирования */
            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY,
                    frame.left - POINT_SIZE,
                    frame.top - POINT_SIZE,
                    frame.right + POINT_SIZE,
                    frame.bottom + POINT_SIZE);
        }
    }
    public void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }
}