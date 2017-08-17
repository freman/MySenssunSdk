package cn.senssun.ble.sdk.eqi;

import cn.senssun.ble.sdk.BleDevice;
import cn.senssun.ble.sdk.entity.FatMeasure;
import cn.senssun.ble.sdk.eqi.BleEQIConnectService.OnDisplayDATA;
import cn.senssun.ble.sdk.eqi.BleEQIConnectService.OnMeasureDATA;
import cn.senssun.ble.sdk.util.LOG;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BleEQISDK {
	private BleDevice bleDevice;
	public 	 BleEQIConnectService mBleConnectService;
	public   ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBleConnectService=((BleEQIConnectService.LocalBinder) service).getService();//BluetoothLeService mBluetoothLeService =
			if(mBleConnectService.initialize()){
				if(mOnInitService!=null){
					mOnInitService.OnInitService();
				}
			}
			mBleConnectService.setOnDisplayDATA(new OnDisplayDATA() {
				@Override
				public void OnDATA(String data) {
					String[] strdata=data.split("-");
					if(strdata[1].equals("status")){
						if(strdata[2].equals("disconnect")){
							if(mOnConnectState!=null){
								mOnConnectState.OnState(false);
							}
						}else if(strdata[2].equals("connect")){
							if(mOnConnectState!=null){
								mOnConnectState.OnState(true);
							}
						}else if(strdata[2].equals("dataCommun")){
							if(mOnSendDataCommunListener!=null){
								mOnSendDataCommunListener.OnListener();
							}
						}else if(strdata[2].equals("fatTest")){
							if(mOnSendFatTestListener!=null){
								mOnSendFatTestListener.OnListener(true);
							}
						}else if(strdata[2].equals("fatTestError")){
							if(mOnSendFatTestListener!=null){
								mOnSendFatTestListener.OnListener(false);
							}
						}else if(strdata[2].equals("dataCommunEnd")){
							if(mOnSendDataCommnuEndListener!=null){
								mOnSendDataCommnuEndListener.OnListener();
							}
						}
					}
				}
			});
			mBleConnectService.setOnMeasureDATA(new OnMeasureDATA() {
				@Override
				public void OnDATA(FatMeasure fatMeasure) {
					if(mOnMeasure!=null){
						mOnMeasure.OnData(fatMeasure);
					}
				}
			});

		}
		@Override
		public void onServiceDisconnected(ComponentName componentName) {
		}
	};

	public  void InitSDK(Context mContext) {
		Intent gattServiceIntent = new Intent(mContext, BleEQIConnectService.class);
		mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public  void stopSDK(Context mContext){
		mContext.unbindService(mServiceConnection);
	}

	public  boolean isConnect(){
		try {
			if(mBleConnectService==null)
				return false;
			else 
				return	mBleConnectService.ismConnect();
		} catch (Exception e) {
			return false;
		}

	}

	public  void Connect(BleDevice bleDevice){
		this.bleDevice=bleDevice;
		if(bleDevice.getBluetoothDevice().getName().contains("EQi-99")){
			mBleConnectService.scanLeStartDevice(bleDevice.getBluetoothDevice().getAddress());
		}else if(bleDevice.getBluetoothDevice().getName().contains("EQi-912")){
			mBleConnectService.connect(bleDevice.getBluetoothDevice().getAddress());
		}
	}

	public  void Disconnect(){
		if(bleDevice.getBluetoothDevice().getName().contains("EQi-99")){
			mBleConnectService.scanLeStopDevice();
		}else if(bleDevice.getBluetoothDevice().getName().contains("EQi-912")){
			mBleConnectService.disconnect();
		}
	}

	/***************************************自定义搜索连接代码********************************************/

	public boolean ConnectStatus(){
		try {
			return mBleConnectService.ismConnect();
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * @param height 身高
	 * @param age 年龄
	 * @param sex 用户性别 （1：男 0：女）
	 * @param serialNum 用户序号 1-12
	 */
	public  void SendTestFatInfo(int height,int age,int sex,int serialNum){
		//往蓝牙模块写入数据
		try {
			if(bleDevice.getBluetoothDevice().getName().contains("EQi-912")){
				mBleConnectService.FatTestBuffer(height,age,sex,serialNum);	
			}
		} catch (Exception e) {
			LOG.logE("BleSDK", "发送脂肪测试命令出错");
		}
	}

	/**
	 * @param serialNum 用户序号 1-12
	 */
	public  void SendDataCommun(int serialNum){
		//往蓝牙模块写入数据
		try {
			if(bleDevice.getBluetoothDevice().getName().contains("EQi-912")){
				mBleConnectService.DataCommunBuffer(serialNum);
			}
		} catch (Exception e) {
			LOG.logE("BleSDK", "发送同步历史命令出错");
		}
	}


	public interface OnConnectState{
		void OnState(boolean State);
	}

	OnConnectState mOnConnectState=null;
	public void setOnConnectState(OnConnectState e){
		mOnConnectState=e;
	}

	public interface OnMeasure{
		void OnData(FatMeasure bodyMeasure);
	}

	OnMeasure mOnMeasure=null;
	public void setOnMeasure(OnMeasure e){
		mOnMeasure=e;
	}

	public interface OnInitService{
		void OnInitService();
	}

	OnInitService mOnInitService=null;
	public void setOnInitService(OnInitService e){
		mOnInitService=e;
	}


	public interface OnSendFatTestListener{
		void OnListener(boolean isSuc);
	}

	OnSendFatTestListener mOnSendFatTestListener=null;
	public void setOnSendFatTestListener(OnSendFatTestListener e){
		mOnSendFatTestListener=e;
	}

	//	public interface OnFatTestErrorListener{
	//		void OnListener();
	//	}
	//
	//	OnFatTestErrorListener mOnFatTestErrorListener=null;
	//	public void setOnFatTestErrorListener(OnFatTestErrorListener e){
	//		mOnFatTestErrorListener=e;
	//	}


	public interface OnSendDataCommunListener{
		void OnListener();
	}

	OnSendDataCommunListener mOnSendDataCommunListener=null;
	public void setOnSendDataCommunListener(OnSendDataCommunListener e){
		mOnSendDataCommunListener=e;
	}

	public interface OnSendDataCommnuEndListener{
		void OnListener();
	}

	OnSendDataCommnuEndListener mOnSendDataCommnuEndListener=null;
	public void setOnSendDataCommnuEndListener(OnSendDataCommnuEndListener e){
		mOnSendDataCommnuEndListener=e;
	}


}
