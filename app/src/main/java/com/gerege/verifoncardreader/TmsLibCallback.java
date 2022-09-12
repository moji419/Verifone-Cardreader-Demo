package com.gerege.verifoncardreader;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.verifone.androidtmslib.AndroidTMSLib;
import com.verifone.androidtmslib.tmsEventData;

public class TmsLibCallback implements AndroidTMSLib.tmsEventCallback {
    private static String TAG = "TmsLibCallBack";

    private Context context;

    public TmsLibCallback(Context context){
        this.context = context;
    }

    @Override
    public int eventCallback(tmsEventData tmsEventData) {
        switch (tmsEventData.evtType){
            case AndroidTMSLib.TMS_EVT_CALL_SERVER_RESPONSE:
                Log.d(TAG, "");
                break;
            case AndroidTMSLib.TMS_EVT_SET_APP_STATE:
                /* Call AndroidTMSLib to set app state */
                AndroidTMSLib.getInstance().setApplicationState(tmsEventData.handle, AndroidTMSLib.APP_STATUS_FREE);
                // AndroidTMSLib.getInstance().setApplicationState(eventCallbackData.handle,AndroidTMSLib.APP_STATUS_BUSY);
                // AndroidTMSLib.getInstance().setApplicationState(eventCallbackData.handle,AndroidTMSLib.APP_STATUS_POSTPONE);
                break;
            case AndroidTMSLib.TMS_EVT_GET_TMS_CONFIG_RESPONSE:
                break;
            case AndroidTMSLib.TMS_EVT_SET_TMS_CONFIG_RESPONSE:
                String msgLog = " Mask:" + Integer.toHexString( tmsEventData.eventMask ) + " | " + tmsEventData.status;
                sentNotice(TAG, msgLog);
                break;

            case AndroidTMSLib.TMS_EVT_SET_APP_INFO:
                break;
            case AndroidTMSLib.TMS_EVT_SET_PARM_LIST:
                AndroidTMSLib.getInstance().setApplicationParameterList(tmsEventData.handle, 1, AndroidTMSLib.getInstance().getParamInfoFileName());
                break;
            case AndroidTMSLib.TMS_EVT_GET_FILE:
                if (tmsEventData.filename.equals("tmstesterParamFiles")) {

                    AndroidTMSLib.getInstance().getApplicationFileAvailable(tmsEventData.handle, AndroidTMSLib.TMS_STATUS_SUCCESS, AndroidTMSLib.getInstance().getParamInfoFileName(), true);
                } else {

                    AndroidTMSLib.getInstance().getApplicationFileAvailable(tmsEventData.handle, AndroidTMSLib.TMS_STATUS_FILENAME_ERROR, null, true);
                }
                break;
            case AndroidTMSLib.TMS_EVT_PUT_FILE:
                if (tmsEventData.filename == null) {

                    AndroidTMSLib.getInstance().setFileOperationResultWithDescription(tmsEventData.handle, AndroidTMSLib.TMS_STATUS_FILENAME_ERROR, AndroidTMSLib.TMS_EVT_PUT_FILE, "filename is NULL");
                } else {
                    // tmsEventData.filename is the filename of the parameter
                    // File file = new File(pathName); to read the file if is XML format
                    // sent the result after load the parameter file
                    AndroidTMSLib.getInstance().setFileOperationResultWithDescription(tmsEventData.handle, AndroidTMSLib.TMS_STATUS_SUCCESS, AndroidTMSLib.TMS_EVT_PUT_FILE, "TMSTester automatically passes all PUT_FILE requests with valid filename");
                }
                break;
            case AndroidTMSLib.TMS_EVT_DEL_FILE:
                break;
            case AndroidTMSLib.TMS_EVT_NOTIFICATION:
                String msg = "Mask:" + Integer.toHexString(tmsEventData.eventMask);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                break;
            case AndroidTMSLib.TMS_EVT_DO_TRANSACTION:
                break;
            case AndroidTMSLib.TMS_EVT_REGISTER_APP_RESPONSE:
                if (tmsEventData.status == AndroidTMSLib.TMS_STATUS_SUCCESS) {
                    Log.d(TAG, "Registration SUCCESSFUL");
                    Toast.makeText(context, "Registration SUCCESSFUL", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Registration FAILED");
                    Toast.makeText(context, "Registration FAILED", Toast.LENGTH_LONG).show();
                }
                break;
            case AndroidTMSLib.TMS_EVT_UNREGISTER_APP_RESPONSE:
                break;

            case AndroidTMSLib.TMS_EVT_GET_SERVER_INSTANCE:
                break;

            case AndroidTMSLib.TMS_EVT_LOCK_SERVER_INSTANCE:
                break;

            case AndroidTMSLib.TMS_EVT_RELEASE_SERVER_INSTANCE:
                break;

            case AndroidTMSLib.TMS_EVT_APP_ALERT_RESULT:
                break;

            case AndroidTMSLib.TMS_EVT_CLEAR_APP_INFO_RESULT:
                break;

            case AndroidTMSLib.TMS_EVT_API_ERRORS:
                break;

            default:{
                break;
            }
        }

        return 0;
    }

    public void sentNotice(String tag, String msgLog){
        Log.d(tag, msgLog);
    };

}
