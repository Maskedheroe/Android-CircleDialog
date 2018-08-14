package com.mylhyl.circledialog.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mylhyl.circledialog.Controller;
import com.mylhyl.circledialog.callback.CircleItemLabel;
import com.mylhyl.circledialog.params.DialogParams;
import com.mylhyl.circledialog.params.ItemsParams;
import com.mylhyl.circledialog.res.drawable.CircleDrawableSelector;
import com.mylhyl.circledialog.res.values.CircleColor;
import com.mylhyl.circledialog.view.listener.ItemsView;
import com.mylhyl.circledialog.view.listener.OnRvItemClickListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hupei on 2018/4/18.
 */

class BodyRecyclerView extends RecyclerView implements Controller.OnClickListener, ItemsView {
    protected Context mContext;
    protected DialogParams mDialogParams;
    private ItemsParams mItemsParams;
    private OnRvItemClickListener mOnRvItemClickListener;
    private Adapter mAdapter;

    public BodyRecyclerView(Context context) {
        super(context);
    }

    public BodyRecyclerView(Context context, ItemsParams itemsParams, DialogParams dialogParams
            , OnRvItemClickListener listener) {
        super(context);
        init(context, itemsParams, dialogParams, listener);
    }

    public void init(Context context, ItemsParams itemsParams, DialogParams dialogParams
            , OnRvItemClickListener listener) {
        this.mContext = context;
        this.mItemsParams = itemsParams;
        this.mDialogParams = dialogParams;
        this.mOnRvItemClickListener = listener;
        configBackground();
        createLayoutManager();
        createItemDecoration();
        createAdapter();
    }

    protected void configBackground() {
        //如果没有背景色，则使用默认色
        int backgroundColor = mItemsParams.backgroundColor != 0
                ? mItemsParams.backgroundColor : mDialogParams.backgroundColor;
        setBackgroundColor(backgroundColor);
    }

    private void createLayoutManager() {
        if (mItemsParams.layoutManager == null) {
            mItemsParams.layoutManager = new LinearLayoutManager(getContext()
                    , mItemsParams.linearLayoutManagerOrientation, false);
        } else if (mItemsParams.layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) mItemsParams.layoutManager;
            if (gridLayoutManager.getSpanCount() == 1) {
                mItemsParams.layoutManager = new LinearLayoutManager(getContext()
                        , mItemsParams.linearLayoutManagerOrientation, false);
            }
        }
        setLayoutManager(mItemsParams.layoutManager);
    }

    private void createItemDecoration() {
        if (mItemsParams.dividerHeight > 0) {
            if (mItemsParams.layoutManager instanceof GridLayoutManager
                    && mItemsParams.itemDecoration == null) {
                mItemsParams.itemDecoration = new GridItemDecoration(new ColorDrawable(CircleColor.divider)
                        , mItemsParams.dividerHeight);
            } else if (mItemsParams.layoutManager instanceof LinearLayoutManager
                    && mItemsParams.itemDecoration == null) {
                int orientation = ((LinearLayoutManager) mItemsParams.layoutManager).getOrientation();
                mItemsParams.itemDecoration = new LinearItemDecoration(new ColorDrawable(CircleColor.divider)
                        , mItemsParams.dividerHeight, orientation);
            }
            addItemDecoration(mItemsParams.itemDecoration);
        }
    }

    private void createAdapter() {
        mAdapter = mItemsParams.adapterRv;
        if (mAdapter == null) {
            mAdapter = new ItemsAdapter(mContext, mItemsParams, mDialogParams);
            if (mItemsParams.layoutManager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) mItemsParams.layoutManager;
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        int itemCount = mAdapter.getItemCount();
                        int spanCount = gridLayoutManager.getSpanCount();
                        int mod = itemCount % spanCount;
                        if (mod == 0 || position < itemCount - 1) {
                            return 1;
                        } else {
                            return spanCount - mod + 1;
                        }
                    }
                });
            }
        }
        setAdapter(mAdapter);
    }

    @Override
    public void refreshItems() {
        post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void regOnItemClickListener(AdapterView.OnItemClickListener listener) {

    }

    @Override
    public void regOnItemClickListener(OnRvItemClickListener listener) {
        if (mAdapter != null && mAdapter instanceof ItemsAdapter) {
            ((ItemsAdapter) mAdapter).setOnItemClickListener(listener);
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onClick(View view, int which) {
        if (mOnRvItemClickListener != null) {
            mOnRvItemClickListener.onItemClick(view, which);
        }
    }

    static class ItemsAdapter<T> extends Adapter<ItemsAdapter.Holder> {
        private OnRvItemClickListener mItemClickListener;
        private Context mContext;
        private List<T> mItems;
        private ItemsParams mItemsParams;
        private int mBackgroundColorPress;

        public ItemsAdapter(Context context, ItemsParams itemsParams, DialogParams dialogParams) {
            this.mContext = context;
            this.mItemsParams = itemsParams;
            mBackgroundColorPress = itemsParams.backgroundColorPress != 0
                    ? itemsParams.backgroundColorPress : dialogParams.backgroundColorPress;
            Object entity = itemsParams.items;
            if (entity != null && entity instanceof Iterable) {
                this.mItems = (List<T>) entity;
            } else if (entity != null && entity.getClass().isArray()) {
                this.mItems = Arrays.asList((T[]) entity);
            } else {
                throw new IllegalArgumentException("entity must be an Array or an Iterable.");
            }
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            ScaleTextView textView = new ScaleTextView(mContext);
            LayoutManager layoutManager = mItemsParams.layoutManager;
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    textView.setLayoutParams(new LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    textView.setPadding(10, 0, 10, 0);
                } else {
                    textView.setLayoutParams(new LayoutParams(
                            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                }
            } else {
                textView.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            }
            textView.setTextSize(mItemsParams.textSize);
            textView.setTextColor(mItemsParams.textColor);
            textView.setHeight(mItemsParams.itemHeight);
            Holder holder = new Holder(textView, mItemClickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.item.setBackground(new CircleDrawableSelector(Color.TRANSPARENT, mBackgroundColorPress));
            } else {
                holder.item.setBackgroundDrawable(new CircleDrawableSelector(Color.TRANSPARENT, mBackgroundColorPress));
            }
            String label;
            T item = mItems.get(position);
            if (item instanceof CircleItemLabel) {
                label = ((CircleItemLabel) item).getItemLabel();
            } else {
                label = item.toString();
            }
            holder.item.setText(String.valueOf(label));
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }

        public void setOnItemClickListener(OnRvItemClickListener listener) {
            this.mItemClickListener = listener;
        }

        static class Holder extends RecyclerView.ViewHolder implements OnClickListener {
            OnRvItemClickListener mOnRvItemClickListener;
            TextView item;

            public Holder(TextView itemView, OnRvItemClickListener listener) {
                super(itemView);
                item = itemView;
                mOnRvItemClickListener = listener;
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (mOnRvItemClickListener != null) {
                    mOnRvItemClickListener.onItemClick(v, getAdapterPosition());
                }
            }
        }
    }

    static class GridItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable mDivider;
        private int mDividerHeight;

        public GridItemDecoration(Drawable divider, int dividerHeight) {
            mDivider = divider;
            mDividerHeight = dividerHeight;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);
        }

        private void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int top = child.getBottom() + params.bottomMargin;
                final int right = child.getRight() + params.rightMargin + mDividerHeight;
                final int bottom = top + mDividerHeight;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private void drawVertical(Canvas c, RecyclerView parent) {
            final int childCount = parent.getChildCount() - 1;
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int top = child.getTop() - params.topMargin;
                final int right = left + mDividerHeight;
                final int bottom = child.getBottom() + params.bottomMargin;

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
            // 如果是最后一行，则不需要绘制底部
            if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
                outRect.set(0, 0, mDividerHeight, 0);
            }
            // 如果是最后一列，则不需要绘制右边
            else if (isLastColumn(parent, itemPosition, spanCount, childCount)) {
                outRect.set(0, 0, 0, mDividerHeight);
            } else {
                outRect.set(0, 0, mDividerHeight, mDividerHeight);
            }
        }

        private static boolean isLastColumn(RecyclerView parent, int pos, int spanCount, int childCount) {
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                // 如果是最后一列，则不需要绘制右边
                if ((pos + 1) % spanCount == 0) {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    // 如果是最后一列，则不需要绘制右边
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                } else {
                    childCount = childCount - childCount % spanCount;
                    // 如果是最后一列，则不需要绘制右边
                    if (pos >= childCount) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                childCount = childCount - childCount % spanCount;
                // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount) {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
                // StaggeredGridLayoutManager 且纵向滚动
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    childCount = childCount - childCount % spanCount;
                    // 如果是最后一行，则不需要绘制底部
                    if (pos >= childCount) {
                        return true;
                    }
                }
                // StaggeredGridLayoutManager 且横向滚动
                else {
                    // 如果是最后一行，则不需要绘制底部
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static int getSpanCount(RecyclerView parent) {
            // 列数
            int spanCount;
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
            } else {
                spanCount = layoutManager.getItemCount();
            }
            return spanCount;
        }
    }

    static class LinearItemDecoration extends RecyclerView.ItemDecoration {

        private Drawable mDivider;
        private int mDividerHeight;
        private int mOrientation;

        public LinearItemDecoration(Drawable divider, int dividerHeight, int orientation) {
            this.mDivider = divider;
            this.mDividerHeight = dividerHeight;
            this.mOrientation = orientation;
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == LinearLayoutManager.VERTICAL)
                outRect.set(0, 0, 0, mDividerHeight);
            else {
                //最后一列不画
                if (itemPosition != parent.getAdapter().getItemCount() - 1) {
                    outRect.set(0, 0, mDividerHeight, 0);
                }
            }
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == LinearLayoutManager.VERTICAL) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }

        private void drawVertical(Canvas c, RecyclerView parent) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            int childCount = parent.getChildCount() - 1;
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDividerHeight;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDividerHeight;
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}