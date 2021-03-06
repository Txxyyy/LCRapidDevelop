package com.xiaochao.lcrapiddevelop.UI;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.xiaochao.lcrapiddevelop.Adapter.SectionAdapter;
import com.xiaochao.lcrapiddevelop.Data.Constant;
import com.xiaochao.lcrapiddevelop.Data.JsonData;
import com.xiaochao.lcrapiddevelop.R;
import com.xiaochao.lcrapiddevelop.RxjavaRetrofit.HttpData;
import com.xiaochao.lcrapiddevelop.entity.MySection;
import com.xiaochao.lcrapiddevelop.entity.UniversityListDto;
import com.xiaochao.lcrapiddeveloplibrary.BaseQuickAdapter;
import com.xiaochao.lcrapiddeveloplibrary.viewtype.ProgressActivity;

import java.util.List;

import rx.Observer;

public class ListviewGroupingActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,BaseQuickAdapter.RequestLoadMoreListener {
    RecyclerView mRecyclerView;
    ProgressActivity progress;
    SwipeRefreshLayout swipeLayout;
    private Toolbar toolbar;
    private SectionAdapter mQuickAdapter;
    private int PageIndex=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview_grouping);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initView();
        initListener();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        progress = (ProgressActivity) findViewById(R.id.progress);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        progress.showLoading();
        mQuickAdapter = new SectionAdapter(R.layout.list_view_item_layout,R.layout.def_section_head,null);
//        mQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mQuickAdapter.setOnLoadMoreListener(this);
        mQuickAdapter.openLoadMore(4,true);
        mRecyclerView.setAdapter(mQuickAdapter);
        initdate(PageIndex,false);

    }
    private void initListener() {
        swipeLayout.setOnRefreshListener(this);
        mQuickAdapter.setOnLoadMoreListener(this);

        mQuickAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    MySection mySection = (MySection) mQuickAdapter.getData().get(position);
                    if (!mySection.isHeader)
                        Toast.makeText(ListviewGroupingActivity.this,mySection.t.getCnName(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mQuickAdapter.setOnRecyclerViewItemChildClickListener(new BaseQuickAdapter.OnRecyclerViewItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                Toast.makeText(ListviewGroupingActivity.this, "没有更多", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void initdate(final int PageIndex, final Boolean isJz){
        HttpData.getInstance().HttpDataToSchoolList(PageIndex, 4, new Observer<List<UniversityListDto>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                //设置页面为加载错误
                toError();
            }

            @Override
            public void onNext(List<UniversityListDto> universityListDtos) {
                if(isJz){
                    if(universityListDtos.size()==0){
                        //所有数据加载完成后显示
                        mQuickAdapter.notifyDataChangedAfterLoadMore(false);
                        View view = getLayoutInflater().inflate(R.layout.not_loading, (ViewGroup) mRecyclerView.getParent(), false);
                        mQuickAdapter.addFooterView(view);
                    }else{
                        //新增自动加载的的数据
                        mQuickAdapter.notifyDataChangedAfterLoadMore(JsonData.getSampleData(universityListDtos,PageIndex), true);
                    }
                }else{
                    if(universityListDtos.size()==0){
                        //设置页面为无数据
                        toEmpty();
                    }else{
                        //进入显示的初始数据或者下拉刷新显示的数据
                        mQuickAdapter.setNewData(JsonData.getSampleData(universityListDtos,PageIndex));//新增数据
                        mQuickAdapter.openLoadMore(4,true);//设置是否可以下拉加载  以及加载条数
                        swipeLayout.setRefreshing(false);//刷新成功
                        progress.showContent();
                    }
                }
            }
        });
    }
    public void toError(){
        try {
            mQuickAdapter.notifyDataChangedAfterLoadMore(false);
            View view = getLayoutInflater().inflate(R.layout.not_loading, (ViewGroup) mRecyclerView.getParent(), false);
            mQuickAdapter.addFooterView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progress.showError(getResources().getDrawable(R.mipmap.monkey_cry), Constant.ERROR_TITLE, Constant.ERROR_CONTEXT, Constant.ERROR_BUTTON, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.showLoading();
                initdate(1,false);
            }
        });
    }
    public void toEmpty(){
        progress.showEmpty(getResources().getDrawable(R.mipmap.monkey_cry),Constant.EMPTY_TITLE,Constant.EMPTY_CONTEXT);
    }

    //下拉刷新
    @Override
    public void onRefresh() {
        PageIndex=1;
        initdate(PageIndex,false);
    }
    //自动加载
    @Override
    public void onLoadMoreRequested() {
        PageIndex++;
        initdate(PageIndex,true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_grouping, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings_AlphaIn:
                mQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
                mRecyclerView.setAdapter(mQuickAdapter);
                return true;
            case R.id.action_settings_ScaleIn:
                mQuickAdapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
                mRecyclerView.setAdapter(mQuickAdapter);
                return true;
            case R.id.action_settings_SlideInBottom:
                mQuickAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_BOTTOM);
                mRecyclerView.setAdapter(mQuickAdapter);
                return true;
            case R.id.action_settings_SlideInLeft:
                mQuickAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
                mRecyclerView.setAdapter(mQuickAdapter);
                return true;
            case R.id.action_settings_SlideInRight:
                mQuickAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_RIGHT);
                mRecyclerView.setAdapter(mQuickAdapter);
                return true;
            case R.id.action_settings_Custom:
                mQuickAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_CUSTOM);
                mRecyclerView.setAdapter(mQuickAdapter);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
