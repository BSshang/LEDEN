package com.leiden.interfaces;

import com.leiden.model.LeidenDevice;

public interface LeidenConnectResultCallBack {
    void success(LeidenDevice leidenDevice);

    void close(LeidenDevice leidenDevice);

    void fail(LeidenDevice leidenDevice);
}
