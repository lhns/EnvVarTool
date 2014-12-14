package org.lolhens.envvartool;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

/**
 * Created by LolHens on 10.12.2014.
 */
public class Advapi32UtilExt {


    /**
     * Get a registry value type.
     *
     * @param root  Root key.
     * @param key   Registry path.
     * @param value Name of the value to retrieve.
     * @return String value.
     */
    public static int registryGetValueType(WinReg.HKEY root, String key, String value) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ,
                phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            return registryGetValueType(phkKey.getValue(), value);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    /**
     * Get a registry value type.
     *
     * @param hKey  Parent Key.
     * @param value Name of the value to retrieve.
     * @return String value.
     */
    public static int registryGetValueType(WinReg.HKEY hKey, String value) {
        IntByReference lpcbData = new IntByReference();
        IntByReference lpType = new IntByReference();
        int rc = Advapi32.INSTANCE.RegQueryValueEx(hKey, value, 0,
                lpType, (char[]) null, lpcbData);
        if (rc != W32Errors.ERROR_SUCCESS
                && rc != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
            throw new Win32Exception(rc);
        }
        return lpType.getValue();
    }

    public static void registrySetValue(WinReg.HKEY root, String keyPath, String name, Object value, int type) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, keyPath, 0,
                WinNT.KEY_READ | WinNT.KEY_WRITE, phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            registrySetValue(phkKey.getValue(), name, value, type);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static void registrySetValue(WinReg.HKEY root, String name, Object value, int type) {
        byte[] data = null;
        switch (type) {
            case WinNT.REG_BINARY:
                data = (byte[]) value;
                Advapi32Util.registrySetBinaryValue(root, name, (byte[]) value);
                break;
            case WinNT.REG_QWORD:
                data = new byte[8];
                long longVal = (value instanceof Integer) ? (long) (int) value : (long) value;
                for (int i = 0; i < data.length; i++) data[i] = (byte) ((longVal >> i * 8) & 0xff);
                break;
            case WinNT.REG_DWORD:
                data = new byte[4];
                int intVal = (int) value;
                for (int i = 0; i < data.length; i++) data[i] = (byte) ((intVal >> i * 8) & 0xff);
                break;
            case WinNT.REG_MULTI_SZ:
                String[] arrVal = (String[]) value;
                int size = 0;
                for (String s : arrVal) {
                    size += s.length() * Native.WCHAR_SIZE;
                    size += Native.WCHAR_SIZE;
                }
                size += Native.WCHAR_SIZE;

                int offset = 0;
                Memory memory = new Memory(size);
                for (String s : arrVal) {
                    memory.setWideString(offset, s);
                    offset += s.length() * Native.WCHAR_SIZE;
                    offset += Native.WCHAR_SIZE;
                }
                for (int i = 0; i < Native.WCHAR_SIZE; i++) {
                    memory.setByte(offset++, (byte) 0);
                }
                data = memory.getByteArray(0, size);
                break;
            case WinNT.REG_SZ:
            case WinNT.REG_EXPAND_SZ:
                char[] chars = Native.toCharArray((String) value);
                data = new byte[chars.length * Native.WCHAR_SIZE];
                for (int i = 0; i < chars.length; i++) {
                    data[i * 2] = (byte) (chars[i] & 0xff);
                    data[i * 2 + 1] = (byte) ((chars[i] >> 8) & 0xff);
                }
                break;
            default:
                throw new RuntimeException("Unexpected registry type " + type);
        }
        int rc = Advapi32.INSTANCE.RegSetValueEx(root, name, 0, type, data, data.length);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
    }

    public static Object registryGetValue(WinReg.HKEY root, String key, String value) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = Advapi32.INSTANCE.RegOpenKeyEx(root, key, 0, WinNT.KEY_READ,
                phkKey);
        if (rc != W32Errors.ERROR_SUCCESS) {
            throw new Win32Exception(rc);
        }
        try {
            return registryGetValue(phkKey.getValue(), value);
        } finally {
            rc = Advapi32.INSTANCE.RegCloseKey(phkKey.getValue());
            if (rc != W32Errors.ERROR_SUCCESS) {
                throw new Win32Exception(rc);
            }
        }
    }

    public static Object registryGetValue(WinReg.HKEY root, String value) {
        if (registryGetValueType(root, value) == WinNT.REG_EXPAND_SZ) {
            return Advapi32Util.registryGetExpandableStringValue(root, value);
        } else {
            return Advapi32Util.registryGetValue(root, "", value);
        }
    }
}
