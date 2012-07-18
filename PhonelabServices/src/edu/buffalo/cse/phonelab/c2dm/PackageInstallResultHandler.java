package edu.buffalo.cse.phonelab.c2dm;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
/**
 * Class to instantiate the android system's package installer.Can be extended to show our own dialog if needed.
 * @author ans25
 *
 */
public class PackageInstallResultHandler extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//savedInstanceState.g
		intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" +"1"/* app.getAppID()*/)), "application/vnd.android.package-archive");
		//intent.putExtra("receiver",resultReceiver);
		startActivityForResult(intent, 1777); 
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//super.onActivityResult(requestCode, resultCode, data);
		System.out.println("hahahahahaha");
		finish();
	}

}
