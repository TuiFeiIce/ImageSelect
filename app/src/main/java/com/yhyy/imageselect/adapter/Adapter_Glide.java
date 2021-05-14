package com.yhyy.imageselect.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnItemClickListener;
import com.yhyy.imageselect.R;
import com.yhyy.imageselect.inter.OnCallBackListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Adapter_Glide extends RecyclerView.Adapter {
    public OnItemClickListener mOnItemClickListener;
    public OnCallBackListener onCallBackListener;
    public LayoutInflater layoutInflater;
    public Context context;
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_PICTURE = 2;
    private List<LocalMedia> list = new ArrayList<>();
    private int selectMax = 9;
    private int contentDraw;
    private int delDraw;
    /**
     * 点击添加图片跳转
     */
    public onAddPicClickListener mOnAddPicClickListener;

    public interface onAddPicClickListener {
        void onAddPicClick();
    }

    /**
     * 删除
     */
    public void delete(int position) {
        try {
            if (position != RecyclerView.NO_POSITION && list.size() > position) {
                list.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, list.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Adapter_Glide(Context context, int contentDraw, int delDraw, onAddPicClickListener mOnAddPicClickListener, OnCallBackListener onCallBackListener) {
        this.layoutInflater = LayoutInflater.from(context);
        this.mOnAddPicClickListener = mOnAddPicClickListener;
        this.onCallBackListener = onCallBackListener;
        this.contentDraw = contentDraw;
        this.delDraw = delDraw;
    }

    public void setSelectMax(int selectMax) {
        this.selectMax = selectMax;
    }

    public void setList(List<LocalMedia> list) {
        this.list = list;
    }

    public List<LocalMedia> getData() {
        return list == null ? new ArrayList<>() : list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivContent;
        ImageView ivDel;

        public ViewHolder(View view) {
            super(view);
            ivContent = view.findViewById(R.id.iv_content);
            ivDel = view.findViewById(R.id.iv_del);
        }
    }

    @Override
    public int getItemCount() {
        if (list.size() < selectMax) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowAddItem(position)) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PICTURE;
        }
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.recy_image_select, parent, false));
    }

    private boolean isShowAddItem(int position) {
        int size = list.size() == 0 ? 0 : list.size();
        return position == size;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    /**
     * 设置值
     */

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        //少于8张，显示继续添加的图标
        if (getItemViewType(position) == TYPE_CAMERA) {
            viewHolder.ivContent.setImageResource(contentDraw);
            viewHolder.ivContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnAddPicClickListener.onAddPicClick();
                }
            });
            viewHolder.ivDel.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.ivDel.setImageResource(delDraw);
            viewHolder.ivDel.setVisibility(View.VISIBLE);
            viewHolder.ivDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = viewHolder.getBindingAdapterPosition();
                    // 这里有时会返回-1造成数据下标越界,具体可参考getAdapterPosition()源码，
                    // 通过源码分析应该是bindViewHolder()暂未绘制完成导致，知道原因的也可联系我~感谢
                    onCallBackListener.OnCallBack(view, index);
                    delete(index);
                }
            });
            LocalMedia media = list.get(position);
            if (media == null
                    || TextUtils.isEmpty(media.getPath())) {
                return;
            }
            String path;
            if (media.isCut() && !media.isCompressed()) {
                // 裁剪过
                path = media.getCutPath();
            } else if (media.isCompressed() || (media.isCut() && media.isCompressed())) {
                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                path = media.getCompressPath();
            } else {
                // 原图
                path = media.getPath();
            }

            Log.i("CY", "原图地址::" + media.getPath());

            if (media.isCut()) {
                Log.i("CY", "裁剪地址::" + media.getCutPath());
            }
            if (media.isCompressed()) {
                Log.i("CY", "压缩地址::" + media.getCompressPath());
                Log.i("CY", "压缩后文件大小::" + new File(media.getCompressPath()).length() / 1024 + "k");
            }
            if (!TextUtils.isEmpty(media.getAndroidQToPath())) {
                Log.i("CY", "Android Q特有地址::" + media.getAndroidQToPath());
            }
            if (media.isOriginal()) {
                Log.i("CY", "是否开启原图功能::" + true);
                Log.i("CY", "开启原图功能后地址::" + media.getOriginalPath());
            }
            Glide.with(viewHolder.itemView.getContext())
                    .load(PictureMimeType.isContent(path) && !media.isCut() && !media.isCompressed() ? Uri.parse(path)
                            : path)
                    .centerCrop()
                    .placeholder(R.color.gray_bg)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.ivContent);
            //itemView 的点击事件
            if (mOnItemClickListener != null) {
                viewHolder.itemView.setOnClickListener(v -> {
                    int adapterPosition = viewHolder.getBindingAdapterPosition();
                    mOnItemClickListener.onItemClick(v, adapterPosition);
                });
            }
        }
    }
}
