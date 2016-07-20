package com.aolei.jxustnc.ordersystem.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.aolei.jxustnc.ordersystem.R;
import com.aolei.jxustnc.ordersystem.httputil.BmobHttp;
import com.aolei.jxustnc.ordersystem.util.ToastUtil;

import cn.bmob.v3.listener.SaveListener;

/**
 * 关于我们Fragment
 */
public class AboutUsFragment extends Fragment {

    private View view;
    private Button btn_submit_feedback;
    private EditText et_feedback;

    public AboutUsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about_us, container, false);
        initView(view);
        initEvent();
        return view;
    }

    private void initEvent() {
        btn_submit_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = et_feedback.getText().toString();
                if (!message.equals("")) {
                    BmobHttp bmobHttp = new BmobHttp(getActivity());
                    bmobHttp.doFeedBack(message, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            ToastUtil.showShort(getActivity(), "感谢您的反馈!");
                            et_feedback.setText("");
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            ToastUtil.showShort(getActivity(), "反馈失败，请重试" + s);
                        }
                    });
                } else {
                    ToastUtil.showShort(getActivity(), "请填写反馈信息");
                }
            }
        });
    }

    private void initView(View view) {
        btn_submit_feedback = (Button) view.findViewById(R.id.btn_submit_feedback);
        et_feedback = (EditText) view.findViewById(R.id.et_feedback);
    }
}
