package com.chnword.chnword.net;

import android.os.Handler;

import org.json.JSONObject;

/**
 * Created by khtc on 15/5/12.
 */
public class RegistNet extends AbstractNet {
    public RegistNet(Handler handler, JSONObject param, String url) {
        super(handler, param, url);
    }
}
