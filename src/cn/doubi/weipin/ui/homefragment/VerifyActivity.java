package cn.doubi.weipin.ui.homefragment;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.doubi.weipin.R;
import cn.doubi.weipin.domain.UrlContent;
import cn.doubi.weipin.ui.BaseActivity;
import cn.doubi.weipin.ui.HomeActivity;
import cn.doubi.weipin.utils.Logger;
import cn.doubi.weipin.utils.WeiPinUtil;

import com.google.gson.Gson;

public class VerifyActivity extends BaseActivity{

	public static final  String TELPHONE_NUMBER = "tel";
	private Button mSendCodeButton;
	private TextView mMessageText;
	private EditText mVerifyEditText;
	private EditText mTelEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_page);
		initViews();
	}
	@Override
	protected void initViews() {
		mTelEditText = (EditText) findViewById(R.id.Verify_EnterTelNumberEditText);
		mVerifyEditText = (EditText)findViewById(R.id.Verify_EnterVerifyEditText);
		mMessageText = (TextView) findViewById(R.id.Verify_messagetext);
		mSendCodeButton = (Button) findViewById(R.id.Verify_SendVerifyButton);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.Verify_SendVerifyButton:
			String tel = mTelEditText.getText().toString();
			if(!TextUtils.isEmpty(tel) && tel.length() == 11){
					verify();
					checkCode(tel);
			}else{
				showToast(getResString(R.string.tel_enter_error_toast_msg));
			}
			break;
		case R.id.Verify_nextButton:
			String ecode = mVerifyEditText.getText().toString();
			if(!TextUtils.isEmpty(code) && code.length() == 4){
				//开启验证
				if(!TextUtils.isEmpty(code)){
					if(code.equals(ecode)){
						//输入验证码成功
						getSP(getApplicationContext()).edit().putBoolean(WeiPinUtil.IS_VERIFY, true).commit();
						WeiPinUtil.saveTel(mTelEditText.getText().toString(), this);
						//存储推荐人
						showToast("验证成功");
						if(!WeiPinUtil.isEditUserInfo(this)){
							finish();
							startActivity(new Intent(this, FragmentMyInfoNew.class));
						}else{
							finish();
							startActivity(new Intent(VerifyActivity.this,HomeActivity.class));
						}
					}else{
						showToast("验证码不正确");
						
					}
				}
			}else{
				showToast(getResString(R.string.enter_verify_code_error_msg));
			}
			break;

		default:
			break;
		}
	}
	private void checkCode(String tel) {

		AjaxParams params = new AjaxParams();
		params.put("telephoNum", tel);
		getHttpClient().post(UrlContent.SEND_TEL_MESSAGE, params, new AjaxCallBack<String>() {
			private ProgressDialog dialog;
			@Override
			public void onStart() {
				dialog = showBaseProgressDialog();
			}
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				dialog.dismiss();
				mHandler.removeCallbacks(mTimerRunnable);
				mSendCodeButton.setText("发送验证码");
				mSendCodeButton.setClickable(true);
				showToast("请检查网络连接是否畅通!");
				Logger.e("code", strMsg,t);
			}
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				dialog.dismiss();
				CheakCode c = new Gson().fromJson(t, CheakCode.class);
				if(c != null){
					code = c.getValidateCode();
					Logger.i("code", code);
//					showToast("验证码:"+code);
				}else{
					showToast("验证码有问题");
				}
			}
		});
		
		
	}
	private String code;
	private int mTimerCount = 60;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			mSendCodeButton.setText("("+mTimerCount --+")");
			if(mTimerCount == 0){
				mSendCodeButton.setClickable(true);
				mTelEditText.setEnabled(true);
				mSendCodeButton.setText(getResString(R.string.send_verfy_code));
				mHandler.removeCallbacks(mTimerRunnable);
				mTimerCount = 60;
			}
		}
	};
	private Runnable mTimerRunnable = new Runnable() {
		@Override
		public void run() {
			mHandler.postDelayed(this, 1000);
			mHandler.sendEmptyMessage(0);
		}
	};
	private void verify() {
		//开启倒计时定时器
		mTelEditText.setEnabled(false);
		mSendCodeButton.setClickable(false);
		mMessageText.setVisibility(View.VISIBLE);
		mHandler.postDelayed(mTimerRunnable, 1000);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(mTimerRunnable);
	}
	public static boolean isNumeric(String str){
		  for (int i = str.length();--i>=0;){   
		   if (!Character.isDigit(str.charAt(i))){
		    return false;
		   }
		  }
		  return true;
		 }
	class CheakCode{
		private String  validateCode;
		public String getValidateCode() {
			return validateCode;
		}
		public void setValidateCode(String validateCode) {
			this.validateCode = validateCode;
		}
	}
}
