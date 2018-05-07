package com.iindicar.indicar.b2_community.boardDetail;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iindicar.indicar.R;
import com.iindicar.indicar.BaseRecyclerViewAdapter;
import com.iindicar.indicar.data.vo.BoardDetailVO;
import com.iindicar.indicar.data.vo.BoardFileVO;
import com.iindicar.indicar.databinding.BoardDetailHeaderBinding;
import com.iindicar.indicar.databinding.BoardDetailItemBinding;

/**
 * Created by yeseul on 2018-04-13.
 */

public class BoardDetailAdapter extends BaseRecyclerViewAdapter<BoardFileVO, RecyclerView.ViewHolder> {

    private static final String TAG = BoardDetailAdapter.class.getSimpleName();

    private BoardDetailVO header;

    private static final int BOARD_HEADER = 0;
    private static final int BOARD_ITEM = 1;
    private OnMenuClickListener onMenuClickListener;

//    private OnItemUpdateClickListener onItemUpdateClickListener;

    public BoardDetailAdapter(Context context){
        super(context);
    }

    public void setHeader(BoardDetailVO header) {
        this.header = header;
        notifyItemChanged(BOARD_HEADER);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return BOARD_HEADER;

        else
            return BOARD_ITEM;
    }

    @Override
    protected void onBindView(final RecyclerView.ViewHolder holder, int position) {

        final int pos = position;

        if(holder instanceof BoardItemViewHolder){

            ((BoardItemViewHolder)holder).binding.setBoardItem(itemList.get(position));

        } else if(holder instanceof BoardHeaderViewHolder){

            ((BoardHeaderViewHolder) holder).binding.setBoard(header);
            ((BoardHeaderViewHolder) holder).binding.buttonMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onMenuClickListener != null){
                        onMenuClickListener.onMenuClick(view, pos);
                    }
                }
            });

            if(itemList != null && itemList.size() > 0) {
                ((BoardHeaderViewHolder) holder).binding.setItem(itemList.get(0));
            }
        }
    }

    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener){
        this.onMenuClickListener = onMenuClickListener;
    }

    public interface OnMenuClickListener{

        void onMenuClick(View view, int position);
    }

/*
    사진마다 "|글수정" 넣을때 사용
    public void setOnItemUpdateClickListener(OnItemUpdateClickListener onItemUpdateClickListener){
        this.onItemUpdateClickListener = onItemUpdateClickListener;
    }

    public interface OnItemUpdateClickListener {

        void onUpdateClick(View view, int position);
    }
*/

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == BOARD_HEADER){
            View view = LayoutInflater.from(context).inflate(R.layout.board_detail_header, null);
            return new BoardHeaderViewHolder(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.board_detail_item, null);
            return new BoardItemViewHolder(view);

        }
    }

    public class BoardItemViewHolder extends RecyclerView.ViewHolder {

        BoardDetailItemBinding binding;

        public BoardItemViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    public class BoardHeaderViewHolder extends RecyclerView.ViewHolder {

        BoardDetailHeaderBinding binding;

        public BoardHeaderViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

}
