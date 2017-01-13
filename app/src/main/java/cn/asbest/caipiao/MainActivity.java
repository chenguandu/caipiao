package cn.asbest.caipiao;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.asbest.caipiao.adapter.CaipiaoListAdapter;
import cn.asbest.model.Data;
import cn.asbest.model.Ssq;
import cn.sharp.android.ncr.StaticRecFromCamera;
import cn.sharp.android.ncr.ocr.OCRItems;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "caipiao";
    @BindView(R.id.result)
    TextView mResult;
    @BindView(R.id.listview)
    RecyclerView mList;
    @BindView(R.id.expect_num)
    EditText mExpectNum;
    @BindView(R.id.num)
    EditText mNum;
    CaipiaoListAdapter mCaipiaoListAdapter;
    List<Data> mCaipiaoList;
    //扫描到的开奖日期
    String mScanDate = null;

    Subscription mInputNumSubscriber;
    Subscription mSearchSubscriber;

    //输入的期号是否有效，如果是否合法，或超出范围，最多能查最近20期
    boolean isValidExpect;

    Subscriber<Ssq> mSsqSubscriber = new Subscriber<Ssq>() {
        @Override
        public void onCompleted() {
            Log.d(TAG,"onCompleted");
        }

        @Override
        public void onError(Throwable e) {
            Log.d(TAG,"error:"+e.getMessage());
        }

        @Override
        public void onNext(Ssq ssq) {
//            Log.d(TAG,new Gson().toJson(ssq));
            mCaipiaoList = ssq.getData();
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setHintTextSize(mExpectNum, 14, "请输入期号");
        setHintTextSize(mNum, 14, "请输入号码，每个数字间以逗号(,)分开,每行一注号码");
        mResult = (TextView) findViewById(R.id.result);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mCaipiaoListAdapter = new CaipiaoListAdapter();
        mList.setAdapter(mCaipiaoListAdapter);
        getData().subscribe(mSsqSubscriber);
        String code = readCode();
        if(!TextUtils.isEmpty(code)){
            mNum.setText(code);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveCode(mNum.getText().toString());
        if (!mSsqSubscriber.isUnsubscribed()){
            mSsqSubscriber.unsubscribe();
        }
        if (mInputNumSubscriber != null && !mInputNumSubscriber.isUnsubscribed()){
            mInputNumSubscriber.unsubscribe();
            mInputNumSubscriber = null;
        }
        if (mSearchSubscriber != null && mSearchSubscriber.isUnsubscribed()){
            mSearchSubscriber.unsubscribe();
            mSearchSubscriber = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_scan:
                startActivityForResult(new Intent(this, StaticRecFromCamera.class),88);
                break;
            case R.id.menu_about:
//                ErrorUploadService.uploadError(this,"--------",true);
                break;
            case R.id.menu_use:
                throw new NullPointerException("Test commit error by app.");
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 88){
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    OCRItems ocrItems = (OCRItems) data.getSerializableExtra("scan_result");
                    setCodeFromCamera(ocrItems);
                } else {
                    showScanError(false);
                }
            } else if (resultCode == RESULT_CANCELED){
                showScanError(true);
            } else {
                showScanError(false);
            }
        }
    }

    private void setCodeFromCamera(OCRItems ocrItems){
        final StringBuilder sb = new StringBuilder();
        if (ocrItems != null && ocrItems.telephone != null){
           Subscription scription = Observable.from(ocrItems.telephone)
                    .filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            //s = "11?2016/11/15";//有时候会有这样的情况出现
                            boolean maches = s.matches("^\\S+(/)(\\d{1,2})(/)\\S+$");//匹配带有双/的字符串，即日期
                            Log.d(TAG,s + " maches:"+maches);
                            return s.contains("+") || maches;
                        }
                    })
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String s) {
                            Log.d(TAG,"map");
                            if (s.contains("/")){//日期处理
                                //s = "11?2016/11/18ss";
                                if (mScanDate == null) {//只取第一个日期
                                    int index = s.indexOf("/");//有时候会扫出11?2016/11/15，所以要手动截取
                                    if (index > 4){
                                        s = s.substring(index-4);
                                    }
                                    if (s.length()>10){
                                        s = s.substring(0, 10);
                                    }
                                    mScanDate = s.replaceAll("/", "-");
                                }
                                return null;
                            }else {
                                s = s.replaceAll("\\D","");//去掉非数字的字符
                            }
                            return s;
                        }
                    })
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Subscriber<String>() {
                   @Override
                   public void onStart() {
                       Log.d(TAG,"onStart");
                       mScanDate = null;
                   }

                @Override
                public void onCompleted() {
                    Log.d(TAG,"get scan result onCompleted");
                    if (sb.length()>0){
                        mNum.setText(sb.toString());
                        mExpectNum.setText("");
                        if (mScanDate != null && mCaipiaoList != null && mScanDate.matches("^\\d{4}(\\-|/|\\.)\\d{1,2}(\\-|/|\\.)\\d{1,2}$")) {
                            Data item;
                            for(int i=0; i < mCaipiaoList.size(); i++){
                                item = mCaipiaoList.get(i);
                                if (i == 0){
                                    Log.d(TAG,String.format("ScanDate:%s",mScanDate));
                                    if (mScanDate.compareTo(item.getOpentime()) > 0){
                                        mExpectNum.setText(Integer.valueOf(item.getExpect())+1 + "");
                                        break;
                                    }
                                }
                                if (item.getOpentime().contains(mScanDate)){
                                    mExpectNum.setText(item.getExpect());
                                    break;
                                }
                            }
                        }
                        search();
                    } else {
                        mNum.setText("");
                        showScanError(false);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG,"Error:"+e.getMessage());
                }

                @Override
                public void onNext(String strings) {
                    Log.d(TAG,"onNext:"+strings);
                    if (!TextUtils.isEmpty(strings) && strings.length() == 14) {
                        StringBuilder builder = new StringBuilder();
                        for(int i = 0; i < 7; i++){
                            if (i != 0){
                                builder.append(",");
                            }
                            builder.append(strings.substring(i*2,i*2+2));
                        }
                        if (sb.length() > 0) {
                            sb.append("\n").append(builder);
                        } else {
                            sb.append(builder);
                        }
                    }
                }
            });
        }
    }

    /**
     * 显示扫描错误信息
     */
    private void showScanError(boolean cancel){
        String tip = "";
        if (cancel){
            tip = "已取消扫描";
        } else {
            tip = "扫描失败，请重试或手动输入号码";
        }
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
        mResult.setText(tip);
    }

    /**
     * 设置查询当期的期号
     */
    private void setExpectNum(){
        if(mCaipiaoList != null && mCaipiaoList.get(0) != null){
            mExpectNum.setText(mCaipiaoList.get(0).getExpect());
        }
    }

    private void updateUI(){
        mCaipiaoListAdapter.setData(mCaipiaoList);
        setExpectNum();
    }

    private Observable<Ssq> getData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.Server_Caipiao)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) // 添加Rx适配器
                .addConverterFactory(GsonConverterFactory.create()) // 添加Gson转换器
                .build();
        ApiManager apiManager = retrofit.create(ApiManager.class);
        return apiManager.getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<List<String>> getInputNum(String expect, String nums){
        Log.d(TAG, "expect:"+expect+", nums:"+nums);
        return Observable.just(nums)
                .map(new Func1<String, List<String>>() {
                    @Override
                    public List<String> call(String s) {
                        if(mCaipiaoList == null || mCaipiaoList.size()==0){
                            throw new IllegalArgumentException("Caipiao list is null!");
                        }
                        String[] numlist = s.split("\n");
                        return Arrays.asList(numlist);
                    }
                })
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                            @Override
                            public Observable<?> call(Throwable throwable) {
                                if(throwable instanceof IllegalArgumentException){
                                    return getData().doOnNext(new Action1<Ssq>() {
                                        @Override
                                        public void call(Ssq ssq) {
                                            mCaipiaoList = ssq.getData();
                                            updateUI();
                                        }
                                    });
                                }
                                return Observable.error(throwable);
                            }
                        });
                }
                });
    }

    private String readCode(){
        SharedPreferences spf = getSharedPreferences("config", Context.MODE_PRIVATE);
        return spf.getString("code",null);
    }

    private void saveCode(String code){
        SharedPreferences spf = getSharedPreferences("config", Context.MODE_PRIVATE);
        spf.edit().putString("code", code).commit();
    }

    /**
     * 检验输入号码的合法性
     * @return
     */
    private boolean checkInputCode(String codeStr){
        boolean result = true;
        String[] codelist = codeStr.split("\n");
        int i = 1;
        StringBuilder sb = new StringBuilder();
        for (String code : codelist){
            String[] num = code.split(",");
            if (num.length != 7){
                if (codelist.length > 1) {
                    sb.append("第")
                    .append(i)
                    .append("行");
                }
                sb.append("号码")
                .append(code)
                .append("无效，")
                .append("必须是7个数字").append("\n");
                result = false;
                break;
            } else {
                for(int k = 0;k < num.length;k++){
                    if (k == 6){
                        if (Integer.valueOf(num[k]) > 16) {
                            sb.append("蓝色号码不能大于16\n");
                            result = false;
                            break;
                        }
                    } else {
                        if (Integer.valueOf(num[k]) > 33) {
                            sb.append("红色号码不能大于33\n");
                            result = false;
                            break;
                        }
                    }
                }
            }
            i++;
        }
        if (!result){
            mResult.setText(sb.toString());
        }
        return result;
    }

    @OnClick(R.id.search_btn)
    public void search(){
        isValidExpect = false;
        if (mCaipiaoList != null && mCaipiaoList.size()>0){
            if (TextUtils.isEmpty(mExpectNum.getText().toString())) {
                Toast.makeText(this, "请输入期号", Toast.LENGTH_SHORT).show();
                mResult.setText("请输入期号");
                return;
            } else {
                int expect = Integer.valueOf(mExpectNum.getText().toString());
                int lastExpect = Integer.valueOf(mCaipiaoList.get(0).getExpect());
                if (expect > lastExpect){
                    Toast.makeText(this, "未开奖", Toast.LENGTH_SHORT).show();
                    mResult.setText("未开奖");
                    return;
                }
            }
        }
        if (TextUtils.isEmpty(mNum.getText().toString())){
            Toast.makeText(this, "请输入要查询的彩票号码", Toast.LENGTH_SHORT).show();
            mResult.setText("请输入要查询的彩票号码");
            return;
        }
        if (!checkInputCode(mNum.getText().toString())){
            return;
        }
        Observable<List<String>> list = getInputNum(mExpectNum.getText().toString(), mNum.getText().toString());
        if (mInputNumSubscriber != null && mInputNumSubscriber.isUnsubscribed()){
            mInputNumSubscriber.unsubscribe();
        }
        mInputNumSubscriber = list.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> nums) {
                        Log.d(TAG, "input code:" + nums.toString());
                        doSearch(nums);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "error:" + throwable.getMessage());
                    }
                });
    }

    /**
     * 对奖
     * @param nums 需要对奖的号码
     */
    private void doSearch(final List<String> nums){
        if (mSearchSubscriber != null && mSearchSubscriber.isUnsubscribed()){
            mSearchSubscriber.unsubscribe();
        }
        mSearchSubscriber = Observable.from(mCaipiaoList)
                .filter(new Func1<Data, Boolean>() {
                    @Override
                    public Boolean call(Data data) {
                        return data.getExpect().equals(mExpectNum.getText().toString());
                    }
                })
                .map(new Func1<Data, List<String>>() {
                    @Override
                    public List<String> call(Data data) {
                        String newdata = data.getOpencode().replace("+",",");
                        return Arrays.asList(newdata.split(","));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<String>>() {
            //购买彩票注数
            int buycount = nums.size();

            @Override
            public void call(List<String> s) {
                isValidExpect = true;
                List<String> openCode = new ArrayList<>(s);
                //开奖蓝球
                String openBlueCode = openCode.remove(openCode.size() - 1);
                Log.d(TAG, String.format("open code : %s + %s", openCode.toString(), openBlueCode));
                List<String> inputCode;
                String inputBlue;
                String checkResult;
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < buycount; k++) {
                    inputCode = new ArrayList<>(Arrays.asList(nums.get(k).split(",")));
                    inputBlue = inputCode.remove(inputCode.size() - 1);
                    Log.d(TAG, String.format("input code %d: %s + %s", k + 1, inputCode.toString(), inputBlue));
                    inputCode.retainAll(openCode);
                    Log.d(TAG, String.format("retain code %d: %s[%s]", k + 1, inputCode.toString(), openBlueCode.equals(inputBlue) ? openBlueCode : "-"));
                    checkResult = checkCode(inputCode, openBlueCode.equals(inputBlue));
                    if (!"0".equals(checkResult)) {
                        checkResult = String.format("恭喜你，中了%s等奖:%s", checkResult, nums.get(k));
                        Log.d(TAG, checkResult);
                        sb.append(checkResult).append("\n");
                    } else {
                        Log.d(TAG, "非常遗憾，未中奖");
                    }
                }
                if (TextUtils.isEmpty(sb)) {
                    mResult.setTextColor(Color.DKGRAY);
                    mResult.setText("非常遗憾，未中奖\n");
                } else {
                    mResult.setTextColor(Color.RED);
                    mResult.setText(sb.toString());
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.d(TAG, String.format("Error : %s", throwable.getMessage()));
                throwable.printStackTrace();
            }
        }, new Action0() {
            @Override
            public void call() {
                if (!isValidExpect){
                    Log.d(TAG, "期号已超出范围或非法，只能查最近20期");
                    mResult.setText("期号已超出范围或非法，只能查最近20期\n");
                    Toast.makeText(MainActivity.this, "期号已超出范围或非法，只能查最近20期", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "completed");
            }
        });
    }

    /**
     * 检测中奖等级
     * @param retainCode 中奖的红球
     * @param blueCode 是否中蓝球
     * @return 返回中奖的等级
     */
    private String checkCode(List<String> retainCode, boolean blueCode){
        int count = retainCode.size();
        String result = "0";
        if (blueCode){
            if (count == 6){
                result = "一";
            } else if (count == 5){
                result = "三";
            } else if (count == 4){
                result = "四";
            } else if (count == 3){
                result = "五";
            } else {
                result = "六";
            }
        } else {
            if (count == 6){
                result = "二";
            } else if (count == 5){
                result = "四";
            } else if (count == 4){
                result = "五";
            }
        }
        return result;
    }

    /**
     * 设置hint和hint的字体大小
     * @param view
     * @param size
     */
    private void setHintTextSize(EditText view, int size, String text){
        // 新建一个可以添加属性的文本对象
        SpannableString ss = new SpannableString(text);
        // 新建一个属性对象,设置文字的大小
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(size,true);
        // 附加属性到文本
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 设置hint
        view.setHint(new SpannedString(ss)); // 一定要进行转换,否则属性会消失
    }
}
