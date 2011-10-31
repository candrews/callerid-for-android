package com.integralblue.callerid.inject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.widget.Toast;

import com.google.inject.Inject;
import com.integralblue.callerid.R;

public class VersionInformationHelper {
	
	public static final String PROMPT_FOR_NEW_VERSION_PREFERENCE = "promptForNewVersion";
	public static final String LATEST_VERSION_PREFERENCE = "latestVersion";
	
	
	@Inject PackageInfo packageInfo;
	@Inject Application application;
	@Inject SharedPreferences sharedPreferences;
	
	/** Should we prompt the user to upgrade?
	 * Takes into consideration if there a later version available and if the user has expressed that he does not want to be prompted.
	 * @return
	 */
	public boolean shouldPromptForNewVersion(){
		if (isLaterVersionAvailable()
				&& sharedPreferences.getBoolean(PROMPT_FOR_NEW_VERSION_PREFERENCE, true)) {
			return true;
		}else{
			return false;
		}
	}
	
	/** Tell the user how to upgrade.
	 * May open a browser, open a market app, or something else.
	 */
	public void showNewVersionInformation(){
		final String signatureMD5 = getApplicationSignatureMD5();
		if(application.getString(R.string.integralblue_signature_md5).equals(signatureMD5)){
			//This is an "officially signed" apk. Send them to the Android market for an update.
			//Note that this apk might not have come from the Android market - but it could have.
			try{
				application.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + application.getPackageName())));
				return;
			}catch(ActivityNotFoundException e){
				//ignore the exception, fall through to the fallback case
			}
		}
		//This apk doesn't use my "official" signature, or the Android market is not installed.
		//We don't know how they got the app, so
		//send them to the app home page and ask them to upgrade using whatever means
		//they'd like.
		application.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(application.getString(R.string.app_home_url))));
		Toast.makeText(application, R.string.new_version_unknown_signature,
				Toast.LENGTH_LONG).show();
	}
	
	/** Should we allow the user to be prompted if there's a new version available?
	 * If the user expresses that they never want to be prompted, for example, call this method with "false" as the parameter
	 * @param shouldPrompt
	 */
	public void setAllowPromptForNewVersion(boolean shouldPrompt){
		Editor e = sharedPreferences.edit();
		e.putBoolean(PROMPT_FOR_NEW_VERSION_PREFERENCE, shouldPrompt);
		final boolean commitRet = e.commit();
		assert (commitRet);
	}
	
	/** Get the version code of the currently running application
	 * @return
	 */
	public int getCurrentVersionCode(){
		return packageInfo.versionCode;
	}
	
	/** Get the latest version code of the latest known version of the application.
	 * May be -1 if no version information is available.
	 * @return
	 */
	public int getLatestVersionCode(){
		return sharedPreferences.getInt(LATEST_VERSION_PREFERENCE, -1);
	}
	
	/** Set the latest known version code.
	 * Should only be called when we become aware of a new version code.
	 * @param versionCode
	 */
	public void setLatestVersionCode(int versionCode){
		if(versionCode > getLatestVersionCode()){
			//the version info from the server is greater than what we believe the latest version is
			//save the version info in a preference for access elsewhere
			Editor editor = sharedPreferences.edit();
			editor.putInt(LATEST_VERSION_PREFERENCE, versionCode);
			editor.commit();
		}
	}
	
	/** Is there a later version of the application available than what is currently running?
	 * @return
	 */
	public boolean isLaterVersionAvailable(){
		return getCurrentVersionCode() < getLatestVersionCode();
	}
	
	/** Get the MD5 of the signature of this application
	 * @return
	 */
	public String getApplicationSignatureMD5(){
		return md5(packageInfo.signatures[0].toByteArray());
	}
	
    private static String md5(byte[] s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                            .getInstance("MD5");
            digest.update(s);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++){
            	hexString.append(String.format("%02X", 0xFF & messageDigest[i]));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
        	throw new RuntimeException("How can the MD5 message digest algorithm not be available?!",e);
        }
	}
    
	public Dialog createNewVersionDialog(Context context){
		return (new AlertDialog.Builder(context)
			.setTitle(R.string.new_version_dialog_title)
			.setPositiveButton(R.string.new_version_dialog_upgrade_button_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							showNewVersionInformation();
							dialog.dismiss();
						}
					})
			.setNeutralButton(R.string.new_version_dialog_not_now_button_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
			.setNegativeButton(R.string.new_version_dialog_never_button_text,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							setAllowPromptForNewVersion(false);
							dialog.cancel();
						}
					}).create());
	}
}
