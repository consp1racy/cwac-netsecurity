package com.commonsware.cwac.netsecurity.os;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

@RestrictTo(LIBRARY)
@SuppressLint({"PrivateApi", "SoonBlockedPrivateApi"})
public final class Environment {

    private static Method METHOD_GET_USER_CONFIG_DIRECTORY;

    static {
        Method m;
        try {
            //noinspection JavaReflectionMemberAccess
            m = android.os.Environment.class
                    .getDeclaredMethod("getUserConfigDirectory", int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        METHOD_GET_USER_CONFIG_DIRECTORY = m;
    }

    @RequiresApi(21)
    @NonNull
    public static File getUserConfigDirectory(int userId) {
        try {
            //noinspection ConstantConditions
            return (File) METHOD_GET_USER_CONFIG_DIRECTORY.invoke(null, userId);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    private Environment() {
        throw new AssertionError();
    }
}
