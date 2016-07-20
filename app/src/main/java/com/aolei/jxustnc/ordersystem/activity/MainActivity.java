package com.aolei.jxustnc.ordersystem.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.entity.User;
import com.aolei.jxustnc.ordersystem.fragment.AboutUsFragment;
import com.aolei.jxustnc.ordersystem.fragment.CanteenFragment;
import com.aolei.jxustnc.ordersystem.fragment.HomeFragment;
import com.aolei.jxustnc.ordersystem.fragment.PurchaseFragment;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.util.ImageUtils;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;
import com.aolei.jxustnc.ordersystem.util.Utils;
import com.bumptech.glide.Glide;
import com.easemob.chat.EMChatManager;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class MainActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Fragment currentFragment;
    private NavigationView navigationView;
    private SharedPreferences mSharedPreferences;
    private ImageView img_nav_avatar;
    private TextView nav_username, ic_tv_account, tv_username, ic_tv_phone, tv_userphone, ic_tv_pwd, tv_userpassword, tv_ic_edit;

    private String imagePath, old_pwd, new_pwd1, new_pwd2;
    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences("user_info", Context.MODE_APPEND);
        initView();
        initEvent();
        typeface = Typeface.createFromAsset(getAssets(), "iconfont.ttf");
    }

    /**
     * 初始化监听事件
     */
    private void initEvent() {
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        img_nav_avatar.setOnClickListener(this);
        nav_username.setOnClickListener(this);
    }

    /**
     * 初始化控件
     */
    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("推荐");
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        img_nav_avatar = (ImageView) headerView.findViewById(R.id.img_nav_avatar);
        if (!img_nav_avatar.equals("")) {
            Glide.with(this).load(BmobUser.getObjectByKey(this, "avatar_url")).into(img_nav_avatar);
        }
        nav_username = (TextView) headerView.findViewById(R.id.nav_username);//通过heardLayout来获取TextView 控件
        if (mSharedPreferences.getBoolean("status", false) == true) {
            nav_username.setText(mSharedPreferences.getString("username", ""));
        }
        //显示第一个Fragment
        HomeFragment homeFragment = new HomeFragment();
        currentFragment = homeFragment;
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();
    }

    /**
     * 监听返回键
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定退出应用?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setPositiveButton("取消", null);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            toolbar.setTitle("主页");
            switchFragment(new HomeFragment());
        } else if (id == R.id.nav_canteen1) {
            toolbar.setTitle("第一食堂");
            switchFragment(new CanteenFragment(1));
            // Handle the camera action
        } else if (id == R.id.nav_canteen2) {
            toolbar.setTitle("第二食堂");
            switchFragment(new CanteenFragment(2));

        } else if (id == R.id.nav_canteen3) {
            toolbar.setTitle("第三食堂");
            switchFragment(new CanteenFragment(3));

        } else if (id == R.id.nav_purchase) {
            toolbar.setTitle("我的购买");
            switchFragment(new PurchaseFragment());
        } else if (id == R.id.signout) {
            signOut();
        } else if (id == R.id.aboutus) {
            toolbar.setTitle("关于我们");
            switchFragment(new AboutUsFragment());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 退出登录操作
     */
    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认").setMessage("退出登录?").setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("username", "");
                editor.putString("userpwd", "");
                editor.putBoolean("status", false);
                editor.commit();
                EMChatManager.getInstance().logout();//退出环信登录
                BmobUser.logOut(MainActivity.this);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
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

    private void switchFragment(Fragment fragment) {
        if (currentFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (!fragment.isAdded()) {
                transaction.hide(currentFragment).replace(R.id.fragment_container, fragment).commit();
            } else {
                transaction.hide(currentFragment).show(fragment).commit();

            }
        }
        currentFragment = fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_nav_avatar:
                Crop.pickImage(MainActivity.this);
                break;
            case R.id.nav_username:
                showUserInfoDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
            Crop.of(data.getData(), destination).withAspect(400, 400).start(this);
        }
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            imagePath = ImageUtils.getPath(this, Crop.getOutput(data));
            img_nav_avatar.setImageBitmap(ImageUtils.zoomBitmap(BitmapFactory.decodeFile(imagePath), 400, 400));
            imagePath = ImageUtils.saveBitmap(BitmapFactory.decodeFile(imagePath));
            uploadImage(imagePath);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 上传用户头像
     *
     * @param picPath
     */
    private void uploadImage(String picPath) {
        Utils.showProgressDialog(this);
        if (picPath != null && !(picPath.equals(""))) {
            final BmobFile bmobFile = new BmobFile(new File(picPath));
            bmobFile.uploadblock(this, new UploadFileListener() {
                @Override
                public void onSuccess() {
                    ToastUtil.showShort(MainActivity.this, "头像上传成功");
                    updateAvatarUrl(bmobFile);
                }

                @Override
                public void onFailure(int i, String s) {
                    ToastUtil.showShort(MainActivity.this, "头像上传失败，请重试");
                    Utils.closeProgressDialog();
                }
            });

        }
    }

    /**
     * 更新用户头像
     *
     * @param bmobFile
     */
    private void updateAvatarUrl(BmobFile bmobFile) {
        BmobHttp bmobHttp = new BmobHttp(MainActivity.this);
        User user = new User();
        user.setAvatar_url(bmobFile.getFileUrl(this));
        bmobHttp.updateUserinfo(user, new UpdateListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(MainActivity.this, "头像修改成功!");
                Utils.closeProgressDialog();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(MainActivity.this, "头像修改失败!");
                Utils.closeProgressDialog();
            }
        });
    }

    /**
     * 显示用户信息对话框
     */
    private void showUserInfoDialog() {
        final Dialog dialog;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_userinfo, null);
        initDialogView(view);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("我的信息");
        builder.setView(view);
        builder.setNegativeButton("取消", null);
        dialog = builder.create();
        dialog.show();
        tv_ic_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showUpdatePwdDialog();
            }
        });
    }

    /**
     * 初始化Dialog控件
     *
     * @param view
     */
    private void initDialogView(View view) {
        ic_tv_account = (TextView) view.findViewById(R.id.ic_tv_account);
        tv_username = (TextView) view.findViewById(R.id.tv_username);
        ic_tv_phone = (TextView) view.findViewById(R.id.ic_tv_phone);
        tv_userphone = (TextView) view.findViewById(R.id.tv_userphone);
        ic_tv_pwd = (TextView) view.findViewById(R.id.ic_tv_pwd);
        tv_userpassword = (TextView) view.findViewById(R.id.tv_userpassword);
        tv_ic_edit = (TextView) view.findViewById(R.id.tv_ic_edit);

        ic_tv_account.setTypeface(typeface);
        ic_tv_phone.setTypeface(typeface);
        ic_tv_pwd.setTypeface(typeface);
        tv_ic_edit.setTypeface(typeface);
        tv_username.setText(BmobUser.getObjectByKey(this, "username") + "");
        tv_userphone.setText(BmobUser.getObjectByKey(this, "mobilePhoneNumber") + "");
        tv_userpassword.setText(BmobUser.getObjectByKey(this, "password") + "");

    }

    /**
     * 显示修改密码对话框
     */
    private void showUpdatePwdDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.update_pwd, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    ToastUtil.showShort(MainActivity.this, "密码不能为空");
                } else {
                    if (new_pwd1.equals(new_pwd2)) {
                        updatePwd(old_pwd, new_pwd1);
                        dialog.dismiss();
                    } else {
                        ToastUtil.showShort(MainActivity.this, "两次输入密码不一致");
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
        BmobHttp bmobHttp = new BmobHttp(this);
        bmobHttp.updatePwd(oldPwd, newPwd, new UpdateListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(MainActivity.this, "修改密码成功!");
                tv_userpassword.setText(newPwd);
                Utils.closeProgressDialog();
            }

            @Override
            public void onFailure(int i, String s) {
                ToastUtil.showShort(MainActivity.this, "修改密码失败!");
                Utils.closeProgressDialog();
            }
        });
    }
}
