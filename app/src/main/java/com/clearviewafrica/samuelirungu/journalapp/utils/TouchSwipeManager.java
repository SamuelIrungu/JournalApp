package com.clearviewafrica.samuelirungu.journalapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class TouchSwipeManager extends ItemTouchHelper.SimpleCallback {


    private int swipePosition = -1;
    private float swipeThreshold = 0.5f;
    private Context context;
    private Map<Integer, List<behindButton>> buttonsBuffer;
    private Queue<Integer> recoverQueue;
    private static final int BTN_WIDTH = 400;
    private RecyclerView recyclerView;
    private List<behindButton> behindButtons;
    private GestureDetector gestureDetector;

    @SuppressLint({"ClickableViewAccessibility", "UseSparseArrays"})
    protected TouchSwipeManager(Context context, final RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
        this.behindButtons = new ArrayList<>();
        this.context = context;
        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                for (behindButton button : behindButtons) {
                    if (button.manageClick(e.getX(), e.getY()))
                        break;
                }
                return true;
            }
        };
        this.gestureDetector = new GestureDetector(context, gestureListener);
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                if (swipePosition < 0) return false;
                Point point = new Point((int) e.getRawX(), (int) e.getRawY());

                RecyclerView.ViewHolder swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipePosition);
                View swipedItem = swipedViewHolder.itemView;
                Rect rect = new Rect();
                swipedItem.getGlobalVisibleRect(rect);

                if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rect.top < point.y && rect.bottom > point.y)
                        gestureDetector.onTouchEvent(e);
                    else {
                        recoverQueue.add(swipePosition);
                        swipePosition = -1;
                        restoreSwipedItem();
                    }
                }
                return false;
            }
        };
        this.recyclerView.setOnTouchListener(onTouchListener);
        buttonsBuffer = new HashMap<>();
        recoverQueue = new LinkedList<Integer>(){
            @Override
            public boolean add(Integer o) {
                return !contains(o) && super.add(o);
            }
        };
        connectSwipe();
    }


    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();

        if (swipePosition != pos)
            recoverQueue.add(swipePosition);

        swipePosition = pos;

        if (buttonsBuffer.containsKey(swipePosition))
            behindButtons = buttonsBuffer.get(swipePosition);
        else
            behindButtons.clear();

        buttonsBuffer.clear();
        swipeThreshold = 0.5f * behindButtons.size() * BTN_WIDTH;
        restoreSwipedItem();
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return swipeThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 0.1f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if (pos < 0){
            swipePosition = pos;
            return;
        }

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            if(dX < 0) {
                List<behindButton> buffer = new ArrayList<>();

                if (!buttonsBuffer.containsKey(pos)){
                    initBehindButton(viewHolder, buffer);
                    buttonsBuffer.put(pos, buffer);
                }
                else {
                    buffer = buttonsBuffer.get(pos);
                }

                translationX = dX * buffer.size() * BTN_WIDTH / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private synchronized void restoreSwipedItem(){
        while (!recoverQueue.isEmpty()){
            int position = recoverQueue.poll();
            if (position > -1) {
                recyclerView.getAdapter().notifyItemChanged(position);
            }
        }
    }

    private void drawButtons(Canvas c, View itemView, List<behindButton> buffer, int pos, float dX){
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();

        for (behindButton button : buffer) {
            float left = right - dButtonWidth;
            button.manageDrawing(
                    c,
                    new RectF(
                            left,
                            itemView.getTop(),
                            right,
                            itemView.getBottom()
                    ),
                    pos
            );

            right = left;
        }
    }

    private void connectSwipe(){
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public abstract void initBehindButton(RecyclerView.ViewHolder viewHolder, List<behindButton> behindButtons);

    public static class behindButton {
        private String text;
        private int imageResId;
        private int color;
        private int pos;
        private RectF clickRegion;
        private UnderlayButtonClickListener clickListener;

        public behindButton(String text, int imageResId, int color, UnderlayButtonClickListener clickListener) {
            this.text = text;
            this.imageResId = imageResId;
            this.color = color;
            this.clickListener = clickListener;
        }

        public boolean manageClick(float x, float y){
            if (clickRegion != null && clickRegion.contains(x, y)){
                clickListener.onClick(pos);
                return true;
            }

            return false;
        }

        void manageDrawing(Canvas canvas, RectF rectF, int position){
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawRect(rectF, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            Rect rect = new Rect();
            float height = rectF.height();
            float width = rectF.width();
            paint.setTextAlign(Paint.Align.LEFT);
            paint.getTextBounds(text, 0, text.length(), rect);
            float x = width / 2f - rect.width() / 2f - rect.left;
            float y = height / 2f + rect.height() / 2f - rect.bottom;
            canvas.drawText(text, rectF.left + x, rectF.top + y, paint);

            clickRegion = rectF;
            this.pos = position;
        }
    }

    public interface UnderlayButtonClickListener {
        void onClick(int pos);
    }
}