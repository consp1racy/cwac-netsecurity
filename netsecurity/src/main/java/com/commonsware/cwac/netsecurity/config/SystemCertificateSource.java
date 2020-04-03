/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.netsecurity.config;

import com.commonsware.cwac.netsecurity.os.Environment;
import com.commonsware.cwac.netsecurity.os.UserHandle;

import java.io.File;

import static android.os.Build.VERSION.SDK_INT;

/**
 * {@link CertificateSource} based on the system trusted CA store.
 * @hide
 */
public final class SystemCertificateSource extends DirectoryCertificateSource {
    private static class NoPreloadHolder {
        private static final SystemCertificateSource INSTANCE = new SystemCertificateSource();
    }

    private final File mUserRemovedCaDir;

    private SystemCertificateSource() {
        super(new File(System.getenv("ANDROID_ROOT") + "/etc/security/cacerts"));
        File configDir = resolveConfigDir();
        mUserRemovedCaDir = new File(configDir, "cacerts-removed");
    }

    private File resolveConfigDir() {
        if (SDK_INT >= 21) {
            return Environment.getUserConfigDirectory(UserHandle.myUserId());
        } else {
            return new File(System.getenv("ANDROID_DATA") + "/misc/keychain");
        }
    }

    public static SystemCertificateSource getInstance() {
        return NoPreloadHolder.INSTANCE;
    }

    @Override
    protected boolean isCertMarkedAsRemoved(String caFile) {
        return new File(mUserRemovedCaDir, caFile).exists();
    }
}
