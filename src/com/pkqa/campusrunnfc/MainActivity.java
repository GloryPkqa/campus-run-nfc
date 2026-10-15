package com.pkqa.campusrunnfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final String ALIPAY="com.eg.android.AlipayGphone";
    private static final String DEFAULT_URL="";
    private static final int INK=0xff142b3a, MUTED=0xff6d7e8b, TEAL=0xff087f77;
    private SharedPreferences preferences;
    private TextView status, addressState;
    private String lastDetail="";

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        preferences=getSharedPreferences("campus_run",MODE_PRIVATE);
        getWindow().setStatusBarColor(0xfff4f7fb);
        getWindow().setNavigationBarColor(0xfff4f7fb);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xfff4f7fb);
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(22),dp(20),dp(22),dp(26));
        scroll.addView(page);setContentView(scroll);

        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView appName=label("校园跑 NFC 助手",23,INK,true);
        bar.addView(appName,new LinearLayout.LayoutParams(0,-2,1));
        TextView badge=label("NFC",12,TEAL,true);badge.setPadding(dp(12),dp(7),dp(12),dp(7));badge.setBackground(shape(0xffdeefed,11));bar.addView(badge);
        page.addView(bar,new LinearLayout.LayoutParams(-1,dp(48)));

        FrameLayout hero=new FrameLayout(this);hero.setBackground(shape(0xff132f45,27));hero.setClipToOutline(true);
        hero.addView(new RunningArt(this),new FrameLayout.LayoutParams(-1,-1));
        LinearLayout heroText=new LinearLayout(this);heroText.setOrientation(LinearLayout.VERTICAL);heroText.setPadding(dp(23),dp(25),dp(18),dp(20));
        TextView school=label("自定义 NFC 链接",14,0xffbad1df,false);heroText.addView(school);
        TextView heading=label("阳光校园跑",29,Color.WHITE,true);LinearLayout.LayoutParams headingParams=new LinearLayout.LayoutParams(-1,-2);headingParams.topMargin=dp(18);heroText.addView(heading,headingParams);
        TextView subtitle=label("NFC启动工具",16,0xffd1e2e9,false);LinearLayout.LayoutParams subtitleParams=new LinearLayout.LayoutParams(-1,-2);subtitleParams.topMargin=dp(6);heroText.addView(subtitle,subtitleParams);
        TextView pill=label("少些等待，多些运动",12,0xff8fe5d6,false);LinearLayout.LayoutParams pillParams=new LinearLayout.LayoutParams(-2,-2);pillParams.topMargin=dp(31);heroText.addView(pill,pillParams);
        hero.addView(heroText,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams heroParams=new LinearLayout.LayoutParams(-1,dp(242));heroParams.topMargin=dp(21);page.addView(hero,heroParams);

        LinearLayout actionCard=new LinearLayout(this);actionCard.setOrientation(LinearLayout.VERTICAL);actionCard.setPadding(dp(20),dp(23),dp(20),dp(22));actionCard.setBackground(shape(Color.WHITE,23));actionCard.setElevation(dp(2));
        actionCard.addView(label("一键开启校园跑",20,INK,true));
        TextView help=label("点击下方按钮，打开支付宝校园跑页面。",13,MUTED,false);LinearLayout.LayoutParams helpParams=new LinearLayout.LayoutParams(-1,-2);helpParams.topMargin=dp(8);helpParams.bottomMargin=dp(21);actionCard.addView(help,helpParams);
        Button start=new Button(this);start.setText("NFC 启动校园跑   ›");start.setTextSize(17);start.setTypeface(null,Typeface.BOLD);start.setTextColor(Color.WHITE);start.setAllCaps(false);start.setBackground(shape(TEAL,17));start.setStateListAnimator(null);start.setBackgroundTintList(ColorStateList.valueOf(TEAL));
        start.setOnClickListener(new View.OnClickListener(){public void onClick(View view){sendNdef();}});actionCard.addView(start,new LinearLayout.LayoutParams(-1,dp(58)));
        status=label(savedUrl().isEmpty()?"请先设置启动地址":"●  已就绪",12,TEAL,false);status.setGravity(Gravity.CENTER);LinearLayout.LayoutParams statusParams=new LinearLayout.LayoutParams(-1,-2);statusParams.topMargin=dp(16);actionCard.addView(status,statusParams);
        status.setOnLongClickListener(new View.OnLongClickListener(){public boolean onLongClick(View view){if(!lastDetail.isEmpty())new AlertDialog.Builder(MainActivity.this).setTitle("启动详情").setMessage(lastDetail).setPositiveButton("知道了",null).show();return true;}});
        LinearLayout.LayoutParams cardParams=new LinearLayout.LayoutParams(-1,-2);cardParams.topMargin=dp(19);page.addView(actionCard,cardParams);

        LinearLayout addressCard=new LinearLayout(this);addressCard.setGravity(Gravity.CENTER_VERTICAL);addressCard.setPadding(dp(17),dp(15),dp(12),dp(15));addressCard.setBackground(shape(0xffe9eef4,17));
        LinearLayout addressText=new LinearLayout(this);addressText.setOrientation(LinearLayout.VERTICAL);addressText.addView(label("启动地址",13,INK,true));addressState=label(savedUrl().isEmpty()?"尚未设置，请先添加":"已保存 NFC 地址",12,MUTED,false);addressState.setPadding(0,dp(4),0,0);addressText.addView(addressState);addressCard.addView(addressText,new LinearLayout.LayoutParams(0,-2,1));
        TextView edit=label("修改  ›",14,TEAL,true);edit.setGravity(Gravity.CENTER);edit.setPadding(dp(16),dp(12),dp(8),dp(12));edit.setOnClickListener(new View.OnClickListener(){public void onClick(View view){editUrl();}});addressCard.addView(edit);
        LinearLayout.LayoutParams addressParams=new LinearLayout.LayoutParams(-1,-2);addressParams.topMargin=dp(17);page.addView(addressCard,addressParams);
        TextView credit=label("@Pkqa",12,0xff8c9aa6,false);credit.setGravity(Gravity.CENTER);credit.setPadding(dp(22),dp(13),dp(22),dp(13));
        credit.setOnClickListener(new View.OnClickListener(){public void onClick(View view){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/GloryPkqa/campus-run-nfc")));}catch(ActivityNotFoundException error){showError("未找到浏览器","请安装可打开网页的应用。");}}});
        LinearLayout.LayoutParams creditParams=new LinearLayout.LayoutParams(-1,-2);creditParams.topMargin=dp(12);page.addView(credit,creditParams);
        if(saved!=null){status.setText(saved.getString("status","●  已就绪"));lastDetail=saved.getString("detail","");}
    }

    private String savedUrl(){return preferences.getString("url",DEFAULT_URL);}
    private Uri validUri(String value){
        return Uri.parse(LinkValidator.validate(value));
    }
    private void sendNdef(){
        if(savedUrl().isEmpty()){editUrl();return;}
        try{
            Uri uri=validUri(savedUrl());
            // Send ordinary Android NDEF Intent data to the Alipay package.
            NdefMessage message=new NdefMessage(new NdefRecord[]{NdefRecord.createUri(uri),NdefRecord.createApplicationRecord(ALIPAY)});
            Intent intent=new Intent(NfcAdapter.ACTION_NDEF_DISCOVERED,uri).addCategory(Intent.CATEGORY_DEFAULT).setPackage(ALIPAY);
            intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES,new NdefMessage[]{message});
            startActivity(intent);
            lastDetail="NFC 消息已发送，请以支付宝实际显示的结果为准。";
            status.setText("已发送，请在支付宝继续");status.setTextColor(TEAL);
        }catch(ActivityNotFoundException error){showError("未找到可用入口","请先安装支付宝。若已安装，当前支付宝版本可能不支持此入口。");}
        catch(SecurityException error){showError("启动被拒绝",String.valueOf(error.getMessage()));}
        catch(Exception error){showError("未能启动",String.valueOf(error.getMessage()));}
    }
    private void showError(String title,String detail){lastDetail=title+"\n"+detail;status.setText(title+"，长按查看详情");status.setTextColor(0xffad4b43);new AlertDialog.Builder(this).setTitle(title).setMessage(detail).setPositiveButton("知道了",null).show();}
    private void editUrl(){
        final EditText field=new EditText(this);field.setText(savedUrl());field.setTextSize(13);field.setMinLines(4);field.setMaxLines(8);field.setGravity(Gravity.TOP);field.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(22),dp(8),dp(22),0);box.addView(field,new LinearLayout.LayoutParams(-1,-2));
        TextView tutorial=label("不知道地址？查看获取教程 →",14,TEAL,true);tutorial.setPadding(0,dp(14),0,dp(12));
        tutorial.setOnClickListener(new View.OnClickListener(){public void onClick(View view){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://github.com/GloryPkqa/campus-run-nfc/blob/main/docs/get-link.md")));}catch(ActivityNotFoundException error){showError("未找到浏览器","请安装可打开网页的应用。");}}});box.addView(tutorial,new LinearLayout.LayoutParams(-1,-2));
        final AlertDialog dialog=new AlertDialog.Builder(this).setTitle("修改 NFC 启动地址").setMessage("粘贴完整地址，保存后用于下次启动。").setView(box).setNegativeButton("取消",null).setNeutralButton("清空地址",null).setPositiveButton("保存",null).create();
        dialog.setOnShowListener(new android.content.DialogInterface.OnShowListener(){public void onShow(android.content.DialogInterface ignored){
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener(){public void onClick(View view){field.setText(DEFAULT_URL);}});
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){public void onClick(View view){try{String value=field.getText().toString().trim();if(!value.isEmpty())validUri(value);preferences.edit().putString("url",value).apply();addressState.setText(value.isEmpty()?"尚未设置，请先添加":"已保存 NFC 地址");status.setText(value.isEmpty()?"请先设置启动地址":"●  地址已保存，可以启动");status.setTextColor(TEAL);dialog.dismiss();}catch(Exception error){field.setError(error.getMessage());}}});
        }});dialog.show();
    }
    @Override protected void onSaveInstanceState(Bundle out){out.putString("status",status.getText().toString());out.putString("detail",lastDetail);super.onSaveInstanceState(out);}
    private int dp(float value){return(int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private TextView label(String value,int size,int color,boolean bold){TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(color);view.setIncludeFontPadding(false);if(bold)view.setTypeface(null,Typeface.BOLD);return view;}
    private GradientDrawable shape(int color,float radius){GradientDrawable drawable=new GradientDrawable();drawable.setColor(color);drawable.setCornerRadius(dp(radius));return drawable;}

    private static final class RunningArt extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        RunningArt(Activity context){super(context);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){
            float w=getWidth(),h=getHeight();paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(0,0,w,h,new int[]{0xff102a40,0xff19465a},null,Shader.TileMode.CLAMP));canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
            paint.setColor(0x1337dac1);canvas.drawCircle(w*0.95f,h*0.77f,h*0.64f,paint);paint.setColor(0x1237dac1);canvas.drawCircle(w*0.92f,h*0.82f,h*0.40f,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.2f*getResources().getDisplayMetrics().density);paint.setColor(0x1e89eadb);canvas.drawCircle(w*0.96f,h*0.83f,h*0.51f,paint);
            canvas.save();canvas.translate(w*0.83f,h*0.68f);float scale=h/190f;canvas.scale(scale,scale);paint.setStrokeWidth(6);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setColor(0xff53c7b8);
            Path body=new Path();body.moveTo(-10,-23);body.lineTo(-23,-5);body.lineTo(-4,8);body.lineTo(-15,31);body.moveTo(-7,10);body.lineTo(15,24);body.lineTo(30,24);body.moveTo(-16,-15);body.lineTo(2,-6);body.lineTo(15,-16);body.moveTo(-20,-8);body.lineTo(-38,-5);canvas.drawPath(body,paint);
            paint.setStyle(Paint.Style.FILL);canvas.drawCircle(-3,-38,8,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);paint.setColor(0x857fe4d4);canvas.drawLine(-47,13,-34,13,paint);canvas.drawLine(-43,24,-29,24,paint);canvas.restore();
        }
    }
}
