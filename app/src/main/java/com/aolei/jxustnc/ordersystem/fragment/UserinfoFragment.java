package com.aolei.jxustnc.ordersystem.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.activity.HistoryOrderActivity;
import com.aolei.jxustnc.ordersystem.activity.LoginActivity;
import com.aolei.jxustnc.ordersystem.activity.ShopMainActivity;
import com.aolei.jxustnc.ordersystem.entity.Store;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.httputil.NetworkUtil;
import com.aolei.jxustnc.ordersystem.util.ImageUtils;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.aolei.jxustnc.ordersystem.util.Utils;
import com.bumptech.glide.Glide;
import com.easemob.chat.EMChatManager;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 商家信息
 */
public class UserinfoFragment extends Fragment implements View.OnClickListener {

    private View view;

    private TextView mTrueName, mPwd, mPhone, mStoreName;
    private TextView tv_edit_storename, tv_change_image, tv_ic_history, tv_history_next, tv_ic_account, tv_ic_phone, tv_ic_pwd, tv_pwd_edit;
    private PercentRelativeLayout relative_layout_image, layout_history_order;
    private Button mLoginOut;
    private List<Store> mList;
    private SharedPreferences mSharedPreferences;
    private ImageView image_shop_info;
    private BmobHttp bmobHttp;
    private String old_pwd, new_pwd1, new_pwd2, new_store_name, storeId, imagePath, savePhotoPath;
    private static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    private String TAG = "UserinfoFragment";

    public UserinfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_info, container, false);
        mSharedPreferences = getActivity().getSharedPreferences("user_info", Context.MODE_APPEND);
        bmobHttp = new BmobHttp(getActivity());
        initView();
        initEvent();
        queryInfo();
        return view;
    }

    private void initView() {
        relative_layout_image = (PercentRelativeLayout) view.findViewById(R.id.relative_layout_show);
        layout_history_order = (PercentRelativeLayout) view.findViewById(R.id.layout_history_order);
        relative_layout_image.getBackground().setAlpha(100);
        tv_pwd_edit = (TextView) view.findViewById(R.id.tv_pwd_edit);
        tv_edit_storename = (TextView) view.findViewById(R.id.tv_edit_storename);
        tv_change_image = (TextView) view.findViewById(R.id.tv_change_image);
        image_shop_info = (ImageView) view.findViewById(R.id.image_shop_info);
        tv_history_next = (TextView) view.findViewById(R.id.tv_history_next);
        tv_ic_history = (TextView) view.findViewById(R.id.tv_ic_history);
        tv_ic_account = (TextView) view.findViewById(R.id.tv_ic_account);
        tv_ic_phone = (TextView) view.findViewById(R.id.tv_ic_phone);
        tv_ic_pwd = (TextView) view.findViewById(R.id.tv_ic_pwd);
        mTrueName = (TextView) view.findViewById(R.id.info_truename);
        mPwd = (TextView) view.findViewById(R.id.info_pwd);
        mPhone = (TextView) view.findViewById(R.id.info_phone);
        mStoreName = (TextView) view.findViewById(R.id.info_storename);

        mLoginOut = (Button) view.findViewById(R.id.loginout);
        initTypeFont();
    }

    private void initTypeFont() {
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "iconfont.ttf");
        tv_pwd_edit.setTypeface(typeface);
        tv_edit_storename.setTypeface(typeface);
        tv_change_image.setTypeface(typeface);
        tv_history_next.setTypeface(typeface);
        tv_ic_history.setTypeface(typeface);
        tv_ic_account.setTypeface(typeface);
        tv_ic_phone.setTypeface(typeface);
        tv_ic_pwd.setTypeface(typeface);
    }

    private void initEvent() {
        tv_pwd_edit.setOnClickListener(this);
        tv_edit_storename.setOnClickListener(this);
        tv_change_image.setOnClickListener(this);
        mLoginOut.setOnClickListener(this);
        layout_history_order.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginout:
                if (BmobUser.getCurrentUser(getContext()) != null) {
                    signOut();
                }
                break;
            case R.id.tv_pwd_edit:
                showUpdatePwdDialog();
                break;
            case R.id.tv_edit_storename:
                showUpdateStoreNameDialog();
                break;
            case R.id.tv_change_image:
                showChooseDialog();
                break;
            case R.id.layout_history_order:
                startActivity(new Intent(getActivity(), HistoryOrderActivity.class));
                break;
        }
    }

    /**
     * 修改商家用户信息
     *
     * @param user
     */
    private void updateUserInfo(User user) {
        bmobHttp.updateUserinfo(user, new UpdateListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    /**
     * 修改商家信息
     *
     * @param store
     */
    private void updateStoreInfo(final Store store) {
        bmobHttp.updateStoreinfo(store, storeId, new UpdateListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(getActivity(), "修改成功!");
                mStoreName.setText(store.getStore_name());
                Utils.closeProgressDialog();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(getActivity(), "修改失败!");
                Log.d("NewOrin", "修改失败" + s);
                Utils.closeProgressDialog();
            }
        });
    }

    /**
     * 查询数据
     */
    private void queryInfo() {
        if (NetworkUtil.isConnected(getContext())) {
            bmobHttp.queryStore(BmobUser.getCurrentUser(getActivity()).getObjectId(), new FindListener<Store>() {
                @Override
                public void onSuccess(List<Store> list) {
                    mList = list;
                    setViewData();
                }

                @Override
                public void onError(int i, String s) {
                    Snackbar.make(getView(), "查询失败" + s, Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            Snackbar.make(getView(), "网络未连接", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * 填充数据
     */
    private void setViewData() {
        if (!mList.isEmpty() && mList != null) {
            Store store = mList.get(0);
            mTrueName.setText(store.getUser().getUsername());
            mPhone.setText(store.getUser().getMobilePhoneNumber());
            mStoreName.setText(store.getStore_name());
            Glide.with(getActivity()).load(store.getStore_pic()).into(image_shop_info);
            storeId = store.getObjectId();
        }
    }

    /**
     * 退出登录
     */
    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("确认").setMessage("退出登录?").setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("username", "");
                editor.putString("userpwd", "");
                editor.putBoolean("status", false);
                editor.commit();
                EMChatManager.getInstance().logout();//退出环信登录
                BmobUser.logOut(getActivity());
                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * 显示修改密码对话框
     */
    private void showUpdatePwdDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.update_pwd, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et_old_pwd = (EditText) view.findViewById(R.id.et_old_pwd);
        final EditText et_new_pwd1 = (EditText) view.findViewById(R.id.et_new_pwd1);
        final EditText et_new_pwd2 = (EditText) view.findViewById(R.id.et_new_pwd2);
        builder.setTitle("修改密码");
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                old_pwd = et_old_pwd.getText().toString();
                new_pwd1 = et_new_pwd1.getText().toString();
                new_pwd2 = et_new_pwd2.getText().toString();
                if ("".equals(old_pwd) && "".equals(new_pwd1) && "".equals(new_pwd2)) {
                    ToastUtil.showShort(getActivity(), "密码不能为空");
                } else {
                    if (new_pwd1.equals(new_pwd2)) {
                        updatePwd(old_pwd, new_pwd1);
                        dialog.dismiss();
                    } else {
                        ToastUtil.showShort(getActivity(), "两次输入密码不一致");
                    }
                }
                Utils.closeProgressDialog();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 修改密码
     *
     * @param oldPwd
     * @param newPwd
     */
    private void updatePwd(String oldPwd, final String newPwd) {
        bmobHttp.updatePwd(oldPwd, newPwd, new UpdateListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(getActivity(), "修改密码成功!");
                mPwd.setText(newPwd);
                Utils.closeProgressDialog();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(getActivity(), "修改密码失败!");
                Utils.closeProgressDialog();
            }
        });
    }

    /**
     * 显示修改店铺名对话框
     */
    private void showUpdateStoreNameDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_edittext, null);
        final EditText et_store_name = (EditText) view.findViewById(R.id.et_info);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("修改店铺名");
        builder.setView(view);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new_store_name = et_store_name.getText().toString();
                Store store = new Store();
                store.setStore_name(new_store_name);
                updateStoreInfo(store);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 上传图片
     */
    private void uploadImage() {
        savePhotoPath = ImageUtils.saveBitmap(BitmapFactory.decodeFile(imagePath));
        if (savePhotoPath.equals("")) {
            ToastUtil.showShort(getActivity(), "您尚未选择图片");
        } else {
            Log.d(TAG, "图片路经:" + savePhotoPath);
            final BmobFile bmobFile = new BmobFile(new File(savePhotoPath));
            bmobFile.uploadblock(getActivity(), new UploadFileListener() {
                @Override
                public void onSuccess() {
                    image_shop_info.setImageBitmap(ImageUtils.zoomBitmap(BitmapFactory.decodeFile(savePhotoPath), 640, 480));
                    Store store = new Store();
                    store.setStore_pic(bmobFile.getFileUrl(getActivity()));
                    updateStoreInfo(store);
                }

                @Override
                public void onFailure(int i, String s) {
                    Utils.closeProgressDialog();
                    ToastUtil.showShort(getActivity(), "图片上传失败，请重试" + s);
                    Log.d(TAG, "图片上传失败：" + s);
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO && resultCode == getActivity().RESULT_OK) {
            Uri uri;
            if (data == null) {
                uri = imageUri;
            } else {
                uri = data.getData();
            }
            Crop.of(uri, imageUri).withAspect(640, 480).start(getActivity(), ShopMainActivity.userinfoFragment);
            /**
             imagePath = ImageUtils.getPath(getActivity(), uri);
             Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
             image_shop_info.setImageBitmap(ImageUtils.zoomBitmap(bitmap, 640, 480));
             showAlerDialog();
             Log.d("NewOrin", "图片路经---" + imagePath); **/
        }
        if (requestCode == Crop.REQUEST_PICK && resultCode == getActivity().RESULT_OK) {
            Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));
            Crop.of(data.getData(), destination).withAspect(640, 480).start(getActivity(), ShopMainActivity.userinfoFragment);
        }
        if (requestCode == Crop.REQUEST_CROP && resultCode == getActivity().RESULT_OK) {
            imagePath = ImageUtils.getPath(getActivity(), Crop.getOutput(data));
            showAlerDialog();
        }
    }

    /**
     * 显示选择Dialog
     */
    private void showChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("请选择");
        builder.setItems(new String[]{"相册获取", "拍照获取"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
//                    selectFromGallery();
                    Crop.pickImage(getActivity(), ShopMainActivity.userinfoFragment);
                } else if (which == 1) {
                    selectFromCamera();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 从相册获取图片
     */
    private void selectFromGallery() {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), TAKE_PHOTO);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, TAKE_PHOTO);
        }
    }

    /**
     * 拍照选择图片
     */
    private void selectFromCamera() {
        // 图片名称 时间命名
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        String filename = format.format(date);
        File path = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path, filename + ".jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 将File对象转换为Uri并启动照相程序
        imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); // 照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 指定图片输出地址
        startActivityForResult(intent, TAKE_PHOTO); // 启动照相
        // 拍完照startActivityForResult() 结果返回onActivityResult()函数
    }

    private void showAlerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("确认上传该照片?");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.showProgressDialog(getActivity());
                uploadImage();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

}