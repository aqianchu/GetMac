package com.zqc.getmac;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView text;
    private Button ipBt;
    private Button macBt;
    private Button phoneBt;
    private SmsObserver smsObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        ipBt = (Button) findViewById(R.id.get_id);
        macBt = (Button) findViewById(R.id.get_mac);
        phoneBt = (Button) findViewById(R.id.get_phone_number);
        ipBt.setOnClickListener(this);
        macBt.setOnClickListener(this);
        phoneBt.setOnClickListener(this);

        smsObserver = new SmsObserver(this, smsHandler);
        getContentResolver().registerContentObserver(SMS_INBOX, true,smsObserver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_id:
                WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                StringBuilder sb = new StringBuilder();
                if (info != null) {
                    sb.append("SSID:"+info.getSSID());
                    sb.append("\nBSSID:"+info.getBSSID());
                    sb.append("\nRSSI:"+info.getRssi());
                    text.setText(sb.toString());
                }
                break;
            case R.id.get_mac:
                String mac = NetUtils.tryGetWifiMac(this);
                if (!TextUtils.isEmpty(mac)) {
                    text.setText(mac);
                }
                break;
            case R.id.get_phone_number:
//                String phone = new PhoneInfo(this).getProvidersName();
//                if (!TextUtils.isEmpty(phone)) {
//                    text.setText(phone);
//                } else {
//                    text.setText("获取失败");
//                }

                SendMessageUtil.sendMessage(this,"请问怎么查询火车票", "10086");
                break;
        }
    }


    public Handler smsHandler = new Handler() {
        // 这里可以进行回调的操作
        // TODO
        public void handleMessage(android.os.Message msg) {
            System.out.println("smsHandler 执行了.....");
        }
    };

    private Uri SMS_INBOX = Uri.parse("content://sms/");

    public void getSmsFromPhone() {
        ContentResolver cr = getContentResolver();
        String[] projection = new String[] { "body","address","person"};// "_id", "address",
        // "person",, "date",
        // "type
        String where = " date >  "
                + (System.currentTimeMillis() - 10 * 60 * 1000);
        Cursor cur = cr.query(SMS_INBOX, projection, where, null, "date desc");
        if (null == cur)
            return;
        if (cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));// 手机号
            String name = cur.getString(cur.getColumnIndex("person"));// 联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));

            System.out.println(">>>>>>>>>>>>>>>>手机号：" + number);
            System.out.println(">>>>>>>>>>>>>>>>联系人姓名列表：" + name);
            System.out.println(">>>>>>>>>>>>>>>>短信的内容：" + body);

//            // 这里我是要获取自己短信服务号码中的验证码~~
//            Pattern pattern = Pattern.compile("[a-zA-Z0-9]{5}");
//            Matcher matcher = pattern.matcher(body);//String body="测试验证码2346ds";
//            if (matcher.find()) {
//                String res = matcher.group().substring(0, 5);// 获取短信的内容
//                showToast(res);
//                System.out.println(res);
//            }
//            showToast("手机号:"+number);
            if (!TextUtils.isEmpty(number)) {
                text.setText("手机号："+number);
            }
        }
    }

    class SmsObserver extends ContentObserver {

        public SmsObserver(Context context, Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // 每当有新短信到来时，使用我们获取短消息的方法
            getSmsFromPhone();
        }
    }
}
