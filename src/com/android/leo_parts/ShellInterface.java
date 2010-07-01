/* //device/apps/Settings/src/com/android/settings/Keyguard.java
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

package com.android.leo_parts;

import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

public class ShellInterface extends Thread {

    private static final String TAG = "ShellInterface";
    private static String[] mCommands;

    public ShellInterface(String[] commands) {
	mCommands = commands;
    }

    public int getStatus() {
	return 42;
    }

    public void run() {
	List<String> res = new ArrayList<String>();
	Process process = null;
	DataOutputStream os = null;
	DataInputStream osRes = null;
	Message msg = null;
	int i = 1;

	try {
	    process = Runtime.getRuntime().exec("su");
	    os = new DataOutputStream(process.getOutputStream());
	    for (String single : mCommands) {
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
