package cn.doubi.weipin.ui.homefragment;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.doubi.weipin.R;
import cn.doubi.weipin.domain.OralInfo;
import cn.doubi.weipin.domain.UrlContent;
import cn.doubi.weipin.utils.Logger;
import cn.doubi.weipin.utils.WeiPinUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class OralFragment extends BaseFragment implements OnItemClickListener{

	private PullToRefreshListView mListView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	private void initestdate() {
//		OralInfo info = new OralInfo();
//		info.setHireTitle("Java 工程师");
//		info.setCompanyName("火狐科技");
//		info.setCompanyAddress("中关村");
//		info.setCreate_time("2014/10/10 10:10:22");
//		info.setLinkTel("0102123123");
//		info.setSalay("2000");
//		OralInfo info1 = new OralInfo();
//		info1.setSalay("2000");
//		info1.setLinkTel("0102123123");
//		info1.setHireTitle("Android 工程师");
//		info1.setCompanyName("新浪科技");
//		info1.setCompanyAddress("朝阳区");
//		info1.setCreate_time("2014/10/10 10:10:22");
//		OralInfo info2 = new OralInfo();
//		info2.setSalay("2000");
//		info2.setLinkTel("0102123123");
//		info2.setHireTitle("JavaScript 工程师");
//		info2.setCompanyName("腾讯科技");
//		info2.setCompanyAddress("深圳市");
//		info2.setCreate_time("2014/10/10 10:10:22");
//		mInfos.add(info);
//		mInfos.add(info1);
//		mInfos.add(info2);
	}
//	if(WeiPinUtil.getUserCount(getActivity()) <= 0){
//		mListView.setVisibility(View.INVISIBLE);
//		Logger.i("COUNT", "隐藏");
//	}else{
//		mListView.setVisibility(View.VISIBLE);
//		Logger.i("COUNT", "显示");
//	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.page_2_layout, null);
		mListView = (PullToRefreshListView)v.findViewById(R.id.pull_refresh_list);
		initestdate();
		mMyAdapter = new MyAdapter();
		mListView.setAdapter(mMyAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				Logger.i("R", "刷新完成");
				String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				getOralList(false);
			}
		});
		
		getOralList(false);
		return v;
	}
	@Override
	public void onResume() {
		super.onResume();
		Logger.i("STATE", this.toString()+":onResume");
		getOralList(false);
	}
	@Override
	public void onPushClick() {
		super.onPushClick();
		Logger.i("STATE", this.toString()+":onPushClick");
		getOralList(false);
	}
	private List<OralInfo> mInfos = new ArrayList<OralInfo>();
	private BaseAdapter mMyAdapter;
	public void getOralList(final boolean showDialog){
		final String userId = WeiPinUtil.getUserId(getActivity());
		if(TextUtils.isEmpty(userId)){
			showToast("注册之后才能看到面试信息");
		}else{
			
		AjaxParams params = new AjaxParams();
		params.put("userId", userId);
		getHttpClient().post(UrlContent.HIRELIST_URL,params,new AjaxCallBack<String>() {
			private ProgressDialog dialog;
			@Override
			public void onStart() {
				super.onStart();
				if(showDialog){
					dialog = showBaseProgressDialog();
				}
			}
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Logger.e("ORAL", "失败："+strMsg,t);
				if(dialog != null){
					dialog.dismiss();
				}
				mListView.onRefreshComplete();
			}
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				Logger.i("ORAL", t);
				Gson gson  = new Gson();
				List<OralInfo> infos = gson.fromJson(t, new TypeToken<List<OralInfo>>(){}.getType());
				//过滤已经面试的信息
				mInfos.clear();
				if(infos != null){
					for (int i = 0; i < infos.size(); i++) {
						OralInfo info = infos.get(i);
						if(info.getOralRst().contains("1") && infos.get(i).getUser_id().equals(userId) ){
							mInfos.add(info);	
						}
					}
					mListView.onRefreshComplete();
					mMyAdapter.notifyDataSetChanged();
					if(dialog != null){
						dialog.dismiss();
					}
				}
			}
		});
		}
	}
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return mInfos.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			ViewHolder holder = null;
			if(convertView != null){
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}else{
				holder = new ViewHolder();
				view = View.inflate(getActivity(), R.layout.mianshi_list_oral_item, null);
				holder.title = (TextView) view.findViewById(R.id.oralItemTitle);
				holder.address = (TextView) view.findViewById(R.id.oralImteAddr);
				holder.number = (TextView) view.findViewById(R.id.oralitemnumber);
				holder.time = (TextView) view.findViewById(R.id.oralitemtime);
				holder.gangwei = (TextView) view.findViewById(R.id.oralgangwei);
				holder.salay = (TextView) view.findViewById(R.id.xinchou);
				holder.workaddress = (TextView) view.findViewById(R.id.worktype);//面试地点
				holder.passButton = (TextView) view.findViewById(R.id.passbutton);
				holder.tel = (TextView) view.findViewById(R.id.tel);
				view.setTag(holder);
			}
			OralInfo info = mInfos.get(position);
			holder.title.setText(info.getCompanyName());
			holder.gangwei.setText("面试时间:"+info.getCreate_time());//TODO：面试时间，改为getHireTime
			holder.address.setText("联系人:"+info.getComLinkman());//联系人
			holder.number.setText((position+1)+"");
			holder.time.setText(info.getCreate_time());
			holder.tel.setText("联系电话:"+info.getComTel());
			holder.salay.setText("面试编号:"+info.getOral_id());//改为面试编号
			holder.workaddress.setText("面试地点:"+info.getCompanyAddress());//面试地点
			if(info.getOral_rst().equals("4")){
				holder.passButton.setText("未通过");
			}
			
			return view;
		}
		
	}
	static class ViewHolder{
		TextView title;
		TextView time;
		TextView number;
		TextView address;//联系人
		TextView gangwei;//面试时间
		TextView salay;
		TextView workaddress;//面试地点
		TextView tel;//联系电话
		TextView passButton;
		
	}
	@Override
	public void onItemClick(AdapterView<?> pview, View view, int position, long id) {
		OralInfo info = (OralInfo) mMyAdapter.getItem(position-1);
		//构造一个数组
		Intent i = new Intent(getActivity(),HireDetailActivity.class);
		String[] data = new String[]{info.getCompanyName(),info.getHireTitle(),info.getOralTime(),info.getCompanyAddress(),info.getComTel(),info.getSalay(),info.getWorkYear()+"年",info.getOral_id(),info.getComLinkman()};
		i.putExtra("oral", data);
		i.putExtra("type", "oral");
		i.putExtra("oralState", -1);
		startActivity(i);
	}
}
