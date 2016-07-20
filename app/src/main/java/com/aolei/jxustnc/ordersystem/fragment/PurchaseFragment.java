package com.aolei.jxustnc.ordersystem.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aolei.jxustnc.ordersystem.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的购买Fragment
 */

public class PurchaseFragment extends Fragment {

    private View view;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tab_purchase;
    private String[] mTabs = {"未完成订单", "已完成订单"};

    public PurchaseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_purchase, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        tab_purchase = (TabLayout) view.findViewById(R.id.tab_purchase);
        tab_purchase.setBackground(getResources().getDrawable(R.color.colorPrimary));
        tab_purchase.setTabTextColors(Color.parseColor("#A7D5EF"), Color.WHITE);
        tab_purchase.setTabMode(TabLayout.MODE_FIXED);
        tab_purchase.setSelectedTabIndicatorColor(Color.RED);
        tab_purchase.setSelectedTabIndicatorHeight(10);
        mFragments = new ArrayList<>();
        mFragments.add(new UserOrderfragment(false));
        mFragments.add(new UserOrderfragment(true));
        mViewPager = (ViewPager) view.findViewById(R.id.purchase_viewpager);
        fragmentPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTabs[position];
            }
        };
        mViewPager.setAdapter(fragmentPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tab_purchase.setupWithViewPager(mViewPager);
    }
}
