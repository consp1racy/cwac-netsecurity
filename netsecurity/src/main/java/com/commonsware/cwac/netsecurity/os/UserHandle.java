package com.commonsware.cwac.netsecurity.os;

import android.os.Binder;

import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
public final class UserHandle {

    public static int myUserId() {
        final long token = Binder.clearCallingIdentity();
        try {
            return Binder.getCallingUserHandle().hashCode();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private UserHandle() {
        throw new AssertionError();
    }
}
