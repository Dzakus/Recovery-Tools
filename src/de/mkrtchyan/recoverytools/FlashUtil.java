package de.mkrtchyan.recoverytools;

/*
 * Copyright (c) 2013 Ashot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;

import org.rootcommands.util.RootAccessDeniedException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.sbstrm.appirater.Appirater;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.Notifyer;

public class FlashUtil extends AsyncTask<Void, Void, Boolean>{
	
	Context mContext;
	ProgressDialog pDialog;
	Common c;
	Notifyer nu;
	Support s = new Support();
	File charger;
	File chargermon;
	File ric;
	private static File fflash;
	private static File fdump;
	File file;
	int JOB;
	
	public FlashUtil(Context context, File file, int JOB){
		mContext = context;
		this.file = file;
		this.JOB = JOB;
		nu = new Notifyer(mContext);
		c = new Common();
		if (s.MTD){
			fflash = new File(mContext.getFilesDir(), "flash_image");
			fdump = new File(mContext.getFilesDir(), "dump_image");
			
			c.chmod(fflash, "641", true);
			c.chmod(fdump, "641", true);
		}
		if (s.DEVICE.equals("C6603")) {
			charger = new File(mContext.getFilesDir(), "charger");
			chargermon = new File(mContext.getFilesDir(), "chargermon");
			ric = new File(mContext.getFilesDir(), "ric");
			
			c.chmod(charger, "755", true);
			c.chmod(chargermon, "755", true);
			c.chmod(ric, "755", true);
		}
	}
	
	
	protected void onPreExecute() {
		
		
		pDialog = new ProgressDialog(mContext);
		int Title;
		if (JOB == 1) {
			Title = R.string.flashing;
		} else {
			Title = R.string.creating_bak;
		}
		pDialog.setTitle(Title);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		pDialog.setMessage(file.getAbsolutePath());
		pDialog.setCancelable(false);
		pDialog.show();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		
		try {
			if (s.DEVICE.equals("C6603")) {
				c.executeSuShell(mContext, "cat " + charger.getAbsolutePath() + " >> /system/bin" + charger.getName());
				c.executeSuShell(mContext, "cat " + chargermon.getAbsolutePath() + " >> /system/bin" + chargermon.getName());
				c.executeSuShell(mContext, "cat " + ric.getAbsolutePath() + " >> /system/bin" + ric.getName());
				c.chmod(ric, "755", true);
				c.chmod(charger, "755", true);
				c.chmod(chargermon, "755", true);
			}
			
			switch (JOB) {
			
			case 1:
				if (file.exists()) {
					
					if (s.MTD)
						c.executeSuShell(mContext, fflash.getAbsolutePath() + " recovery " + file.getAbsolutePath());
					
					if (!s.MTD
							&& !s.BLM
							&& !s.RecoveryPath.equals(""))
						c.executeSuShell(mContext, "dd if=" + file.getAbsolutePath() + " of=" + s.RecoveryPath);
					if (s.DEVICE.equals("C6603"))
						c.chmod(file, "644", true);
				}
				break;
			case 2:
				if (s.MTD)
					c.executeSuShell(mContext, fdump.getAbsolutePath() + " recovery " + file.getAbsolutePath());
				
				if (!s.MTD
						&& !s.BLM
						&& !s.RecoveryPath.equals(""))
					c.executeSuShell(mContext, "dd if=" + s.RecoveryPath + " of=" + file.getAbsolutePath());
				break;
			}
		} catch (RootAccessDeniedException e) {e.printStackTrace();}
		return null;
	}
	
	protected void onPostExecute(Boolean succes) {
		
		pDialog.dismiss();
		if (JOB == 1) {
			nu.createAlertDialog(R.string.tsk_end, mContext.getString(R.string.flashed) + " " + mContext.getString(R.string.reboot_recovery_now), new Runnable() {
				@Override
				public void run() {try {
					c.executeSuShell("reboot recovery");
				} catch (RootAccessDeniedException e) {e.printStackTrace();}}
			});
		} else {
			nu.createDialog(R.string.tsk_end, R.string.bakreport, true, true);
		}
		
		Appirater.appLaunched(mContext);
		
		c.setBooleanPerf(mContext, "flash-util", "first-flash", false);
	}
}
