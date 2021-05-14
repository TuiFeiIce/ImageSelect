package com.yhyy.imageselect;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.animators.AnimationType;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnItemClickListener;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.tools.ScreenUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;
import com.yhyy.imageselect.adapter.Adapter_Glide;
import com.yhyy.imageselect.bean.ImgBean;
import com.yhyy.imageselect.inter.OnCallBackListener;
import com.yhyy.imageselect.utils.GlideEngine;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Main2Activity extends BaseActivity {

    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    Adapter_Glide adapterGlide;
    private ArrayList<ImgBean> imgBeanArrayList = new ArrayList<>();//从前面界面传递过来的图片
    private ArrayList<LocalMedia> localMediaList = new ArrayList<>();//当前网格显示出来的图片
    private WeakReference<Adapter_Glide> mAdapterWeakReference;
    private ArrayList<String> mAllImageArrayList = new ArrayList<>();//所有的图片的路径
    private ArrayList<String> mNetImageArrayList = new ArrayList<>();//9张前面传过来的图片的路径
    private ArrayList<String> mNetOtherImageArrayList = new ArrayList<>();//前面传过来的图片其余的图片路径
    private ArrayList<String> mLocalImageArrayList = new ArrayList<>();//本地还没上传的图片的路径
    private ArrayList<String> mUpLoadImageUrlList = new ArrayList<>();//获取图片上传成功后的路径
    private ArrayList<String> mBackImageArrayList = new ArrayList<>();//传回前面界面的所有的图片的路径
    private int netsize = 0;//网络图片数量
    private int localsize = 0;//本地图片数量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        initToolBar();
        initListener();
    }

    public void initListener() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false);
        recyclerview.setLayoutManager(gridLayoutManager);
        recyclerview.addItemDecoration(new GridSpacingItemDecoration(3,
                ScreenUtils.dip2px(this, 6), false));
        adapterGlide = new Adapter_Glide(context, R.drawable.recy_image_add, R.drawable.recy_image_del, onAddPicClickListener, new OnCallBackListener() {
            @Override
            public void OnCallBack(View view, Integer integer) {
                mAllImageArrayList.remove(mAllImageArrayList.get(integer));
                if (integer < netsize) {
                    mNetImageArrayList.remove(mNetImageArrayList.get(integer));
                    netsize = netsize - 1;
                } else if ((integer - netsize) < localsize) {
                    mLocalImageArrayList.remove(mLocalImageArrayList.get(integer - netsize));
                    localsize = localsize - 1;
                } else {
                    Toast.makeText(context, "无效操作，请退出重新进入", Toast.LENGTH_SHORT).show();
                }
            }
        });
        adapterGlide.setSelectMax(9);
        recyclerview.setAdapter(adapterGlide);
        mAdapterWeakReference = new WeakReference<>(adapterGlide);
        mAdapterWeakReference.get().setList(localMediaList);
        mAdapterWeakReference.get().notifyDataSetChanged();
        adapterGlide.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                List<LocalMedia> selectList = adapterGlide.getData();
                if (selectList.size() > 0) {
                    PictureSelector.create(context)
                            .themeStyle(R.style.picture_default_style) // xml设置主题
                            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                            .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                            //.bindCustomPlayVideoCallback(new MyVideoSelectedPlayCallback(getContext()))// 自定义播放回调控制，用户可以使用自己的视频播放界面
                            .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                            .openExternalPreview(position, selectList);
                }
            }
        });
    }

    public Adapter_Glide.onAddPicClickListener onAddPicClickListener = new Adapter_Glide.onAddPicClickListener() {
        @Override
        public void onAddPicClick() {
            // 进入相册 以下是例子：不需要的api可以不写
            PictureSelector.create(context)
                    .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                    .theme(R.style.picture_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style v2.3.3后 建议使用setPictureStyle()动态方式
                    .isWeChatStyle(false)// 是否开启微信图片选择风格
                    .isUseCustomCamera(false)// 是否使用自定义相机
                    .setPictureWindowAnimationStyle(new PictureWindowAnimationStyle())// 自定义相册启动退出动画
                    .setRecyclerAnimationMode(AnimationType.DEFAULT_ANIMATION)// 列表动画效果
                    .isWithVideoImage(false)// 图片和视频是否可以同选,只在ofAll模式下有效
                    .isMaxSelectEnabledMask(true)// 选择数到了最大阀值列表是否启用蒙层效果
                    .maxSelectNum(3)// 最大图片选择数量
                    .minSelectNum(1)// 最小选择数量
                    .maxVideoSelectNum(1) // 视频最大选择数量
                    .imageSpanCount(3)// 每行显示个数
                    .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                    .closeAndroidQChangeWH(true)//如果图片有旋转角度则对换宽高,默认为true
                    .closeAndroidQChangeVideoWH(!SdkVersionUtils.checkedAndroid_Q())// 如果视频有旋转角度则对换宽高,默认为false
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                    .isOriginalImageControl(false)// 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
                    .selectionMode(2)// 多选 or 单选
                    .isPreviewImage(true)// 是否可预览图片
                    .isPreviewVideo(false)// 是否可预览视频
                    .isEnablePreviewAudio(false) // 是否可播放音频
                    .isCamera(true)// 是否显示拍照按钮
                    .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                    .isCompress(true)// 是否压缩
                    .synOrAsy(true)//同步true或异步false 压缩 默认同步
                    .isGif(false)// 是否显示gif图片
                    .compressQuality(90)// 图片压缩后输出质量
                    .minimumCompressSize(1024)// 小于多少kb的图片不压缩
                    .isOpenClickSound(false)// 是否开启点击声音
                    .selectionData(adapterGlide.getData())// 是否传入已选图片
                    .forResult(new MyResultCallback(adapterGlide));
        }
    };

    /**
     * 返回结果回调
     */
    public class MyResultCallback implements OnResultCallbackListener<LocalMedia> {
        private WeakReference<Adapter_Glide> mAdapterWeakReference;

        public MyResultCallback(Adapter_Glide adapter) {
            super();
            this.mAdapterWeakReference = new WeakReference<>(adapter);
        }

        @Override
        public void onResult(List<LocalMedia> result) {
            if (mAdapterWeakReference.get() != null) {
                localMediaList.clear();
                localMediaList.addAll(result);
                mAdapterWeakReference.get().setList(localMediaList);
                mAllImageArrayList.clear();
                for (int i = 0; i < result.size(); i++) {
                    if (!(i < netsize)) {
                        if (result.get(i).getCompressPath() == null || result.get(i).getCompressPath().isEmpty()) {
                            mAllImageArrayList.add(result.get(i).getRealPath());
                            mLocalImageArrayList.add(result.get(i).getRealPath());
                        } else {
                            mAllImageArrayList.add(result.get(i).getCompressPath());
                            mLocalImageArrayList.add(result.get(i).getCompressPath());
                        }
                    } else {
                        mAllImageArrayList.add(result.get(i).getPath());
                    }
                }
                mAdapterWeakReference.get().notifyDataSetChanged();
            }
        }

        @Override
        public void onCancel() {
        }
    }

    private void initToolBar() {
    }

    private void initData() {
        imgBeanArrayList.clear();
        if ((List<ImgBean>) getIntent().getSerializableExtra("Img") != null) {
            imgBeanArrayList.addAll((List<ImgBean>) getIntent().getSerializableExtra("Img"));
        }
        if (imgBeanArrayList.size() > 9) {
            netsize = 9;
        } else {
            netsize = imgBeanArrayList.size();
        }
        localMediaList.clear();
        mNetImageArrayList.clear();
        mNetOtherImageArrayList.clear();
        for (int i = 0; i < imgBeanArrayList.size(); i++) {
            if (i < 9) {
                LocalMedia localMedia = new LocalMedia();
                localMedia.setPath(imgBeanArrayList.get(i).getImg_url());
                localMedia.setCut(false);
                localMedia.setCompressed(false);
                localMediaList.add(localMedia);
                mNetImageArrayList.add(imgBeanArrayList.get(i).getImg_url());
            } else {
                mNetOtherImageArrayList.add(imgBeanArrayList.get(i).getImg_url());
            }
        }
        mAllImageArrayList.addAll(mNetImageArrayList);
    }



    //上传图片mLocalImageArrayList，返回的地址加入mUpLoadImageUrlList
    private void initBundle() {
        mBackImageArrayList.clear();
        mBackImageArrayList.addAll(mNetImageArrayList);
        mBackImageArrayList.addAll(mNetOtherImageArrayList);
        mBackImageArrayList.addAll(mUpLoadImageUrlList);
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        bundle.putSerializable("Img", (Serializable) mBackImageArrayList); //将计算的值回传回去
        intent.putExtras(bundle);
        // 通过intent对象返回结果，必须要调用一个setResult方法，
        setResult(7777, intent);//第一个参数表示结果返回码，一般只要大于1就可以
        finish(); //结束当前的activity的生命周期
    }
}