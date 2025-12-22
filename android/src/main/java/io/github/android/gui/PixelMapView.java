package io.github.android.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import io.github.shared.data.enums_types.CellEffectType;
import io.github.shared.data.enums_types.CellType;
import io.github.shared.data.gameobject.Cell;
import io.github.shared.data.gameobject.Shape;

public class PixelMapView extends View {

    private Shape map;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Zoom & Pan
    private float offsetX = 0f, offsetY = 0f;
    private float scale = 1f;
    private final ScaleGestureDetector scaleDetector;

    // Dernier point pour pan
    private float lastTouchX = 0f, lastTouchY = 0f;
    private boolean isPanning = false;

    public interface OnCellClickListener {
        void onCellClick(int x, int y, Cell cell);
    }

    private OnCellClickListener cellClickListener;

    public PixelMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStyle(Paint.Style.FILL);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        // Pour recevoir les touch events
        setClickable(true);
        setFocusable(true);
    }

    public void setMap(Shape map) {
        this.map = map;
        invalidate();
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.cellClickListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (map == null || map.getTab_cells() == null) return;

        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);

        int w = map.getWidth();
        int h = map.getHeight();

        float cellW = getWidth() / (float) w;
        float cellH = getHeight() / (float) h;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Cell cell = map.getCells(x, y);
                paint.setColor(resolveColor(cell));
                canvas.drawRect(
                    x * cellW,
                    y * cellH,
                    (x + 1) * cellW,
                    (y + 1) * cellH,
                    paint
                );
            }
        }

        canvas.restore();
    }

    private int resolveColor(Cell cell) {
        if (cell == null) return Color.BLACK;

        int baseColor = terrainColor(cell.getCellType());

        if (cell.getNetId() != null) return brighten(baseColor);
        if (cell.getEffectType() != CellEffectType.NONE) return Color.MAGENTA;

        return baseColor;
    }

    private int terrainColor(CellType type) {
        if (type == null) return Color.BLACK;

        switch (type) {
            case VOID: return Color.BLACK;
            case WATER: return Color.rgb(0, 70, 130);
            case GRASS: return Color.rgb(50, 200, 50);
            case ROAD: return Color.DKGRAY;
            default: return Color.MAGENTA;
        }
    }

    private int brighten(int color) {
        int r = Math.min(255, (int)(Color.red(color) * 1.3f));
        int g = Math.min(255, (int)(Color.green(color) * 1.3f));
        int b = Math.min(255, (int)(Color.blue(color) * 1.3f));
        return Color.rgb(r,g,b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isPanning = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && !scaleDetector.isInProgress()) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    offsetX += dx;
                    offsetY += dy;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    isPanning = true;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isPanning && cellClickListener != null) {
                    int x = (int)((event.getX() - offsetX) / scale * map.getWidth() / getWidth());
                    int y = (int)((event.getY() - offsetY) / scale * map.getHeight() / getHeight());
                    if (map.isValidPosition(x, y)) {
                        cellClickListener.onCellClick(x, y, map.getCells(x, y));
                    }
                }
                break;
        }

        return true; // consomme l'événement pour éviter propagation
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(0.5f, Math.min(scale, 5f));
            invalidate();
            return true;
        }
    }
}
