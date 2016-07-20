package com.aolei.jxustnc.ordersystem.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.entity.Store;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.util.ImageUtils;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.aolei.jxustnc.ordersystem.util.Utils;
import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;

public class RegistActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_register, btn_validate, mSelectPic, mTakePic, mCancel;
    private EditText et_register_username, et_register_pwd, et_register_phone, et_register_code, et_store_name, et_store_desc;
    private String username, password, phone_number, code, belong_canteen, store_name, store_desc, image_url;
    private String imagePath = "";
    private Spinner spinner_canteen;
    private ImageView imageview_store;
    private Toolbar register_toolbar;
    private TimeCount timeCount;
    private int millisInFuture = 6000;//倒计时秒数
    private int countDownInterval = 1000;//倒计时多少
    private String tag = "0";
    private Dialog dialog;
    private View view;
    private static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    private int CROP_PHOTO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        /**
         * 区分注册对象
         */
        int flag = getIntent().getIntExtra("flag", 0);
        if (flag == 0) {
            setContentView(R.layout.activity_normal_regist);
        } else {
            tag = "1";
            setContentView(R.layout.activity_shop_regist);
        }
        initView();
        initEvent();
    }

    private void initView() {
        register_toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        btn_register = (Button) findViewById(R.id.regist_btn);
        btn_validate = (Button) findViewById(R.id.get_code_btn);
        et_register_username = (EditText) findViewById(R.id.et_register_username);
        et_register_phone = (EditText) findViewById(R.id.et_register_phone);
        et_register_code = (EditText) findViewById(R.id.et_register_code);
        et_register_pwd = (EditText) findViewById(R.id.et_register_pwd);
        timeCount = new TimeCount(millisInFuture, countDownInterval);

        register_toolbar.setTitleTextColor(Color.WHITE);
        if (tag.equals("0")) {
            register_toolbar.setTitle("用户注册");
        } else {
            register_toolbar.setTitle("商家注册");
            initShopView();
        }
        setSupportActionBar(register_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initShopView() {
        view = getLayoutInflater().inflate(R.layout.photo_choose_dialog, null);
        mSelectPic = (Button) view.findViewById(R.id.select_pic);
        mTakePic = (Button) view.findViewById(R.id.take_pic);
        mCancel = (Button) view.findViewById(R.id.cancel);
        final List<String> list_spinner;
        ArrayAdapter<String> adapter;
        et_store_name = (EditText) findViewById(R.id.et_store_name);
        et_store_desc = (EditText) findViewById(R.id.et_store_desc);
        imageview_store = (ImageView) findViewById(R.id.imageview_store);
        spinner_canteen = (Spinner) findViewById(R.id.spinner_canteen);
        list_spinner = new ArrayList<>();
        list_spinner.add("第一食堂");
        list_spinner.add("第二食堂");
        list_spinner.add("第三食堂");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner_canteen.setAdapter(adapter);
        spinner_canteen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        belong_canteen = list_spinner.get(0);
                        break;
                    case 1:
                        belong_canteen = list_spinner.get(1);
                        break;
                    case 2:
                        belong_canteen = list_spinner.get(2);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                belong_canteen = "第一食堂";
            }
        });
        imageview_store.setOnClickListener(this);
        mSelectPic.setOnClickListener(this);
        mTakePic.setOnClickListener(this);
        mCancel.setOnClickListener(this);

    }

    private void initEvent() {
        btn_register.setOnClickListener(this);
        btn_validate.setOnClickListener(this);
        /*
        mUpwdInputLayout.getEditText().setOnFocusChangeListener(new MyTextChangeListener(mUpwdInputLayout, RegistActivity.this));
        mDormitoryInputLayout.getEditText().setOnFocusChangeListener(new MyTextChangeListener(mDormitoryInputLayout, RegistActivity.this));
        mPhoneNumInputLayout.getEditText().setOnFocusChangeListener(new MyTextChangeListener(mPhoneNumInputLayout, RegistActivity.this));*/
    }

    @Override
    public void onClick(View v) {
        phone_number = et_register_phone.getText().toString();
        switch (v.getId()) {
            case R.id.get_code_btn:
                if (!TextUtils.isEmpty(phone_number)) {
                    sendVerificaCode(phone_number);
                    Snackbar.make(btn_register, "验证码已发送到手机", Snackbar.LENGTH_SHORT).show();
                    timeCount.start();
                } else {
                    Snackbar.make(btn_register, "手机号不能为空", Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.regist_btn:
                Utils.showProgressDialog(RegistActivity.this);
                if (tag.equals("1")) {
                    store_name = et_store_name.getText().toString();
                    store_desc = et_store_desc.getText().toString();
                    if (imagePath.equals("")) {
                        Snackbar.make(btn_register, "请选择图片", Snackbar.LENGTH_SHORT).show();
                        Utils.closeProgressDialog();
                        return;
                    }
                    if (TextUtils.isEmpty(store_name) && TextUtils.isEmpty(store_desc)) {
                        Snackbar.make(btn_register, "请完善商家信息", Snackbar.LENGTH_SHORT).show();
                        Utils.closeProgressDialog();
                        return;
                    }
                }
                code = et_register_code.getText().toString();
                if (!TextUtils.isEmpty(phone_number) && !TextUtils.isEmpty(code)) {
                    validateCode(phone_number, code);
                } else {
                    Snackbar.make(btn_register, "手机号或验证码不能为空", Snackbar.LENGTH_SHORT).show();
                    Utils.closeProgressDialog();
                }
                break;
            case R.id.imageview_store:
                showDialog();
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
            case R.id.regist_test:
                /**
                 * 测试用
                 */
                store_name = et_store_name.getText().toString();
                store_desc = et_store_desc.getText().toString();
                username = et_register_username.getText().toString();
                password = et_register_pwd.getText().toString();
                registEaseMob();
                uploadImage();
                break;
        }
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
            Crop.of(uri, imageUri).withAspect(400, 200).start(this);

        }
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
            Crop.of(data.getData(), destination).withAspect(640, 480).start(this);
        }
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            imagePath = ImageUtils.getPath(this, Crop.getOutput(data));
            imageview_store.setImageBitmap(ImageUtils.zoomBitmap(BitmapFactory.decodeFile(imagePath), 640, 480));
            imagePath = ImageUtils.saveBitmap(BitmapFactory.decodeFile(imagePath));
        }
    }

    /**
     * 发送验证码
     *
     * @param phone
     */
    private void sendVerificaCode(String phone) {
        BmobSMS.requestSMSCode(RegistActivity.this, phone, "注册模板", new RequestSMSCodeListener() {
            @Override
            public void done(Integer smsId, BmobException e) {
                if (e == null) {//验证码发送成功
                    Log.e("NewOrin", "短信id：" + smsId);//用于查询本次短信发送详情
                }
            }
        });
    }

    /**
     * 校验验证码
     */
    private void validateCode(final String phone, String code) {
        Log.d("NewOrin", phone + "," + code);
        //开始请求后台校验验证码
        BmobSMS.verifySmsCode(RegistActivity.this, phone, code, new VerifySMSCodeListener() {
            @Override
            public void done(BmobException ex) {
                if (ex == null) {//短信验证码已验证成功
                    Log.d("NewOrin", "验证通过");
                    doRegister(phone);
                } else {
                    Snackbar.make(btn_register, "验证失败：code =" + ex.getErrorCode() + ",msg = " + ex.getLocalizedMessage(), Snackbar.LENGTH_SHORT).show();
                    Utils.closeProgressDialog();
                }
            }
        });
    }

    /**
     * 注册用户
     *
     * @param phone
     */
    private void doRegister(String phone) {
        username = et_register_username.getText().toString();
        password = et_register_pwd.getText().toString();
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setTag(tag);
        user.setEseamob_pwd(password);
        user.setMobilePhoneNumber(phone);
        user.signUp(this, new SaveListener() {
            @Override
            public void onSuccess() {
                registEaseMob();
                if (tag.equals("0")) {
                    Snackbar.make(btn_register, "注册成功!请重新登录!", Snackbar.LENGTH_SHORT).show();
                    Utils.closeProgressDialog();
                    finish();
                } else {
                    uploadImage();
                }
            }

            @Override
            public void onFailure(int i, String s) {
                Snackbar.make(btn_register, "注册失败!" + s, Snackbar.LENGTH_SHORT).show();
                Utils.closeProgressDialog();
            }
        });
    }

    /**
     * 保存商家用户信息
     */
    private void saveStoreInfo() {
        Store store = new Store();
        store.setUser(BmobUser.getCurrentUser(this, User.class));
        store.setBelong_cateen(belong_canteen);
        store.setStore_name(store_name);
        store.setStore_des(store_desc);
        store.setStore_pic(image_url);
        store.setPhone_number((String) BmobUser.getObjectByKey(this, "mobilePhoneNumber"));
        store.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                Log.d("Test", "商家信息保存成功");
                Snackbar.make(btn_register, "注册成功!请重新登录!", Snackbar.LENGTH_SHORT).show();
                Utils.closeProgressDialog();
                finish();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.d("Test", "商家信息保存failed" + s);
                Snackbar.make(btn_register, "注册失败!请重试", Snackbar.LENGTH_SHORT).show();
                Utils.closeProgressDialog();
            }
        });
    }

    /**
     * 上传图片
     */
    private void uploadImage() {
        final BmobFile bmobFile = new BmobFile(new File(imagePath));
        bmobFile.uploadblock(this, new UploadFileListener() {
            @Override
            public void onSuccess() {
                image_url = bmobFile.getFileUrl(RegistActivity.this);
                Log.d("Test", "照片上传成功" + image_url);
                saveStoreInfo();
            }

            @Override
            public void onFailure(int i, String s) {
                Utils.closeProgressDialog();
                Log.d("Test", "照片上传failed" + s);
                ToastUtil.showShort(RegistActivity.this, "图片上传失败，请重试" + s);
            }
        });

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

    private class TimeCount extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            btn_validate.setClickable(false);
            btn_validate.setTextColor(Color.WHITE);
            btn_validate.setText(millisUntilFinished / 1000 + "秒后重新获取");
        }

        @Override
        public void onFinish() {
            btn_validate.setTextColor(Color.BLACK);
            btn_validate.setText("获取验证码");
            btn_validate.setClickable(true);
        }
    }

    /**
     * 注册环信即时通讯
     */
    private void registEaseMob() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 调用sdk注册方法
                    EMChatManager.getInstance().createAccountOnServer(username, password);
                    Log.d("Test", "环信云注册成功");
                } catch (final EaseMobException e) {
                    //注册失败
                    Log.d("NewOrin", "环信云注册失败" + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 显示选择对话框
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
