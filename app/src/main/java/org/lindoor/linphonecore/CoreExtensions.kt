package org.lindoor.linphonecore

import org.linphone.core.Core
import org.linphone.core.ProxyConfig

val Core.firstProxyConfig: ProxyConfig?
    get() {
        return if (proxyConfigList.size > 0) proxyConfigList.get(0) else null
    }