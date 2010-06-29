package com.android.spare_parts;

import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

public class ShellInterface extends Thread {

    private static final String TAG = "ShellInterface";

    public static void doExec(String[] commands) {
	List<String> res = new ArrayList<String>();
	Process process = null;
	DataOutputStream os = null;
	DataInputStream osRes = null;
	Message msg = null;
	int i = 1;

	try {
	    process = Runtime.getRuntime().exec("su");
	    os = new DataOutputStream(process.getOutputStream());
	    for (String single : commands) {
		Log.i(TAG, "sh: " + single);
		os.writeBytes(single + "\n");
		os.flush();
		msg = Message.obtain();
		msg.arg1 = i++;
		msg.arg2 = -1;
		Thread.sleep(3000);
	    }
	    os.writeBytes("exit\n");
	    os.flush();
	    process.waitFor();
	    msg = Message.obtain();
	    msg.arg1 = 0;
	    msg.arg2 = 0;
	} catch (Exception e) {
	    e.printStackTrace();
	    msg = Message.obtain();
	    msg.arg1 = 1;
	    msg.arg2 = 0;
	    res.add(e.getMessage());
	} finally {
	    try {
		if (os != null)
		    os.close();
		if (osRes != null)
		    osRes.close();
		process.destroy();
	    } catch (Exception e) {
		// nothing
	    }
	}
    }
}
