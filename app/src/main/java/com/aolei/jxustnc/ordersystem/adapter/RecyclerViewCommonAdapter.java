package com.aolei.jxustnc.ordersystem.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * RecyclerView通用适配器
 * Created by NewOr on 2016/4/19.
 */
public abstract class RecyclerViewCommonAdapter<T> extends RecyclerView.Adapter<RecyclerViewViewHolder> {

    protected Context mContext;
    protected int mLayoutId;
    private List<T> mDatas;
    protected LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;
    private onItemLongClickLisener mOnItemLongClickLisener;
    private int mPosition;

    /**
     * 点击事件接口
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface onItemLongClickLisener {
        void onItemLongClick(View view, int position);
    }

    public void setmOnItemLongClickLisener(RecyclerViewCommonAdapter.onItemLongClickLisener mOnItemLongClickLisener) {
        this.mOnItemLongClickLisener = mOnItemLongClickLisener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public RecyclerViewCommonAdapter(Context context, int layoutId, List<T> datas) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutId = layoutId;
        mDatas = datas;
    }

    /**
     * 获取Position
     *
     * @param viewHolder
     * @return
     */
    public int getPosition(RecyclerView.ViewHolder viewHolder) {
        return viewHolder.getAdapterPosition();
    }

    @Override
    public RecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final RecyclerViewViewHolder viewHolder = RecyclerViewViewHolder.get(mContext, parent, mLayoutId);
        viewHolder.getConvertView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, getPosition(viewHolder));
            }
        });
        viewHolder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnItemLongClickLisener.onItemLongClick(v, getPosition(viewHolder));
                return false;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewViewHolder holder, int position) {
        convert(holder, mDatas.get(position));

    }

    public void deleteItem(int position) {
        mDatas.remove(position);
        notifyDataSetChanged();
    }

    public abstract void convert(RecyclerViewViewHolder holder, T t);

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void notifyData(List<T> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }
}
