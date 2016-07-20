package com.aolei.jxustnc.ordersystem.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.entity.Food;
import com.aolei.jxustnc.ordersystem.entity.Store;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.util.ImageUtils;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.aolei.jxustnc.ordersystem.util.Utils;
import com.bumptech.glide.Glide;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class AddFoodActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mImageView;
    private EditText mFoodPrice;
    private EditText mFoodName;
    private Button mSubmit;
    private Button mSelectPic, mTakePic, mCancel;
    private Toolbar add_food_toolbar;
    private View view;
    private Uri imageUri;
    private String imagePath, foodPrice, foodName;
    private Store store;
    private static final int TAKE_PHOTO = 1;
    private Dialog dialog;
    private BmobHttp bmobHttp;
    private Food updateFood;
    private PercentRelativeLayout relativ_layout_choose;
    private TextView tv_add_hint;
    /**
     * 是否为添加食物
     * true为添加
     * false为编辑
     */
    private boolean isAdd = true;
    private Food food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        view = getLayoutInflater().inflate(R.layout.photo_choose_dialog, null);
        Bundle bundle = getIntent().getExtras();
        if (!bundle.getBoolean("flag")) {
            food = (Food) bundle.getSerializable("food");
            isAdd = false;
        }
        initView();
    }

    protected void initView() {
        bmobHttp = new BmobHttp(this);
        tv_add_hint = (TextView) findViewById(R.id.tv_add_hint);
        relativ_layout_choose = (PercentRelativeLayout) findViewById(R.id.relativ_layout_choose);
        add_food_toolbar = (Toolbar) findViewById(R.id.add_food_toolbar);
        add_food_toolbar.setTitle("发布新菜品");
        add_food_toolbar.setTitleTextColor(Color.WHITE);
        mImageView = (ImageView) findViewById(R.id.food_photo);
        mFoodName = (EditText) findViewById(R.id.food_foodname);
        mFoodPrice = (EditText) findViewById(R.id.et_food_price);
        mSelectPic = (Button) view.findViewById(R.id.select_pic);
        mTakePic = (Button) view.findViewById(R.id.take_pic);
        mCancel = (Button) view.findViewById(R.id.cancel);
        mSubmit = (Button) findViewById(R.id.submit);
        mCancel.setOnClickListener(this);
        mSelectPic.setOnClickListener(this);
        mTakePic.setOnClickListener(this);
        mSubmit.setOnClickListener(this);
        relativ_layout_choose.setOnClickListener(this);
        setSupportActionBar(add_food_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!isAdd) {
            setData();
        }
    }

    /**
     * 初始化值
     */
    private void setData() {
        add_food_toolbar.setTitle("更新菜品");
        mFoodName.setText(food.getName());
        mFoodPrice.setText(food.getPrice());
        Glide.with(this).load(food.getImg_url()).into(mImageView);
        mSubmit.setText("提交");
        imagePath = food.getImg_url();
        Log.d("AddFood", "food_image_url" + food.getImg_url());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relativ_layout_choose:
                showDialog();
                break;
            case R.id.submit:
                Utils.showProgressDialog(AddFoodActivity.this);
                foodName = mFoodName.getText().toString();
                foodPrice = mFoodPrice.getText().toString();
                if (isAdd) {
                    getStoreInfo();
                } else {
                    if (imagePath.equals(food.getImg_url())) {
                        Log.d("AddFood", "imagePath:" + imagePath + "不需要上传图片");
                        updateFood = new Food();
                        updateFood.setName(foodName);
                        updateFood.setPrice(foodPrice);
                        updateFoodInfo(updateFood);
                    } else {
                        Log.d("AddFood", "imagePath:" + imagePath + "需要上传图片");
                        uploadImage();
                    }
                }
                break;
            case R.id.select_pic:
//                selectFromGallery();
                Crop.pickImage(this);
                if (dialog != null) {
                    dialog.dismiss();
                }
                break;
            case R.id.take_pic:
                selectFromCamera();
                if (dialog != null) {
                    dialog.dismiss();
                }
                break;
            case R.id.cancel:
                if (dialog != null) {
                    dialog.dismiss();
                }
                break;
        }
    }

    /**
     * 上传食物
     */
    private void getStoreInfo() {
        bmobHttp.queryStore(BmobUser.getCurrentUser(this).getObjectId(), new FindListener<Store>() {
            @Override
            public void onSuccess(List<Store> list) {
                store = list.get(0);
                uploadImage();
            }

            @Override
            public void onError(int i, String s) {
                Utils.closeProgressDialog();
                ToastUtil.showShort(AddFoodActivity.this, "获取信息失败，请重试" + s);
            }
        });
    }

    /**
     * 上传图片
     */
    private void uploadImage() {
        if (imagePath.equals("")) {
            ToastUtil.showShort(AddFoodActivity.this, "请选择图片");
        } else {
            final BmobFile bmobFile = new BmobFile(new File(imagePath));
            bmobFile.uploadblock(this, new UploadFileListener() {
                @Override
                public void onSuccess() {
                    if (isAdd) {
                        uploadFood(bmobFile.getFileUrl(AddFoodActivity.this));
                    } else {
                        updateFood = new Food();
                        updateFood.setName(foodName);
                        updateFood.setPrice(foodPrice);
                        updateFood.setImg_url(bmobFile.getFileUrl(AddFoodActivity.this));
                        updateFoodInfo(updateFood);
                    }
                }

                @Override
                public void onFailure(int i, String s) {
                    Utils.closeProgressDialog();
                    ToastUtil.showShort(AddFoodActivity.this, "图片上传失败，请重试" + s);
                }
            });
        }

    }

    /**
     * 更新食物
     *
     * @param updateFood
     */
    private void updateFoodInfo(Food updateFood) {
        BmobHttp bmobHttp = new BmobHttp(this);
        bmobHttp.updateFoodInfo(updateFood, food.getObjectId(), new UpdateListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(AddFoodActivity.this, "更新成功!");
                Utils.closeProgressDialog();
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(AddFoodActivity.this, "更新失败!请重试!" + s);
                Utils.closeProgressDialog();
            }
        });
    }

    /**
     * 上传食物
     *
     * @param fileUrl
     */
    private void uploadFood(String fileUrl) {
        if (!foodName.equals("") && foodName != null) {
            if (!foodPrice.equals("") && foodPrice != null) {
                Food food = new Food();
                food.setUser(BmobUser.getCurrentUser(this, User.class));
                food.setImg_url(fileUrl);
                food.setName(foodName);
                food.setPrice(foodPrice);
                food.setStore(store);
                food.setSold_count(0);
                food.save(this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        ToastUtil.showShort(AddFoodActivity.this, "食物上传成功!");
                        Utils.closeProgressDialog();
                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ToastUtil.showShort(AddFoodActivity.this, "食物上传失败!请重试" + s);
                        Utils.closeProgressDialog();
                    }
                });
            } else {
                ToastUtil.showShort(this, "请输入食物价格");
            }
        } else {
            ToastUtil.showShort(this, "请输入食物名称");
        }
        Utils.closeProgressDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示选择Dialog
     */
    private void showDialog() {
        dialog = new Dialog(this, R.style.transparentFrameWindowStyle);
        ViewGroup parent = (ViewGroup) view.getParent();//获取父类的View
        //防止View重复被实例化，如果已经被实例化了，就移除被实例化的View
        if (parent != null) {
            parent.removeAllViews();
        }
        dialog.setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            Uri uri;
            if (data == null) {
                uri = imageUri;
            } else {
                uri = data.getData();
            }
//            doCropPhoto(uri);
            Crop.of(uri, imageUri).withAspect(640, 480).start(this);
            /**
             imagePath = ImageUtils.getPath(this, uri);
             Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
             imageview_store.setImageBitmap(ImageUtils.zoomBitmap(bitmap, 640, 480));
             //			imageview.setImageBitmap(bitmap);
             Log.d("NewOrin", "图片路经---" + imagePath); **/

        }
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
            Crop.of(data.getData(), destination).withAspect(640, 480).start(this);
        }
        if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                imagePath = ImageUtils.getPath(this, Crop.getOutput(data));
                imagePath = ImageUtils.saveBitmap(BitmapFactory.decodeFile(imagePath));
                mImageView.setImageBitmap(ImageUtils.zoomBitmap(BitmapFactory.decodeFile(imagePath), 640, 480));
            } else if (resultCode == Crop.RESULT_ERROR) {
                Toast.makeText(this, Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
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
}