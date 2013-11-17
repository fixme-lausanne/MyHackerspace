package ch.fixme.status;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Network extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctxt, Intent intent) {
        if (Main.checkNetwork(ctxt)) {
            Log.i(Main.TAG, "Update widget on network change");
            Widget.UpdateAllWidgets(ctxt, true);
        }
    }

}
