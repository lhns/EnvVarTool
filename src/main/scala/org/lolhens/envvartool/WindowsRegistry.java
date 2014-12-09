package org.lolhens.envvartool;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.lang.reflect.Method;

/**
 * Simple registry access class implemented using some private APIs
 * in java.util.prefs. It has no other prerequisites.
 */
public final class WindowsRegistry {
    /**
     * Tells if the Windows registry functions are available.
     * (They will not be available when not running on Windows, for example.)
     */
    public static boolean isAvailable() {
        return initError == null;
    }


    /**
     * Reads a string value from the given key and value name.
     */
    public static String readValue(String keyName, String valueName) {
        try (Key key = Key.open(keyName, KEY_READ)) {
            Object o = null;
            try {
                o = regQueryValueEx.invoke(null, key.handle, toByteArray(valueName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            System.out.println(o);
            return fromByteArray(invoke(regQueryValueEx, key.handle, toByteArray(valueName)));
        }
    }


    /**
     * Returns a map of all the name-value pairs in the given key.
     */
    public static Map<String, String> readValues(String keyName) {
        try (Key key = Key.open(keyName, KEY_READ)) {
            int[] info = invoke(regQueryInfoKey, key.handle);
            checkError(info[INFO_ERROR_CODE]);
            int count = info[INFO_COUNT_VALUES];
            int maxlen = info[INFO_MAX_VALUE_LENGTH] + 1;
            Map<String, String> values = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String valueName = fromByteArray(invoke(regEnumValue, key.handle, i, maxlen));
                values.put(valueName, readValue(keyName, valueName));
            }
            return values;
        }
    }


    /**
     * Returns a list of the names of all the subkeys of a key.
     */
    public static List<String> readSubkeys(String keyName) {
        try (Key key = Key.open(keyName, KEY_READ)) {
            int[] info = invoke(regQueryInfoKey, key.handle);
            checkError(info[INFO_ERROR_CODE]);
            int count = info[INFO_COUNT_KEYS];
            int maxlen = info[INFO_MAX_KEY_LENGTH] + 1;
            List<String> subkeys = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                subkeys.add(fromByteArray(invoke(regEnumKeyEx, key.handle, i, maxlen)));
            }
            return subkeys;
        }
    }


    /**
     * Writes a string value with a given key and value name.
     */
    public static void writeValue(String keyName, String valueName, String value) {
        try (Key key = Key.open(keyName, KEY_WRITE)) {
            checkError(invoke(regSetValueEx, key.handle, toByteArray(valueName), toByteArray(value)));
        }
    }


    /**
     * Deletes a value within a key.
     */
    public static void deleteValue(String keyName, String valueName) {
        try (Key key = Key.open(keyName, KEY_WRITE)) {
            checkError(invoke(regDeleteValue, key.handle, toByteArray(valueName)));
        }
    }


    /**
     * Deletes a key and all values within it. If the key has subkeys, an
     * "Access denied" error will be thrown. Subkeys must be deleted separately.
     */
    public static void deleteKey(String keyName) {
        checkError(invoke(regDeleteKey, keyParts(keyName)));
    }


    /**
     * Creates a key. Parent keys in the path will also be created if necessary.
     * This method returns without error if the key already exists.
     */
    public static void createKey(String keyName) {
        int[] info = invoke(regCreateKeyEx, keyParts(keyName));
        checkError(info[INFO_ERROR_CODE]);
        invoke(regCloseKey, info[INFO_HANDLE]);
    }


    /**
     * The exception type that will be thrown if a registry operation fails.
     */
    public static class RegError extends RuntimeException {
        public RegError(String message, Throwable cause) {
            super(message, cause);
        }
    }


    // *************
    // PRIVATE STUFF
    // *************

    private WindowsRegistry() {
    }


    // Map of registry hive names to constants from winreg.h
    private static final Map<String, Integer> hives = new HashMap<>();

    static {
        hives.put("HKEY_CLASSES_ROOT", 0x80000000);
        hives.put("HKCR", 0x80000000);
        hives.put("HKEY_CURRENT_USER", 0x80000001);
        hives.put("HKCU", 0x80000001);
        hives.put("HKEY_LOCAL_MACHINE", 0x80000002);
        hives.put("HKLM", 0x80000002);
        hives.put("HKEY_USERS", 0x80000003);
        hives.put("HKU", 0x80000003);
        hives.put("HKEY_CURRENT_CONFIG", 0x80000005);
        hives.put("HKCC", 0x80000005);
    }


    // Splits a path such as HKEY_LOCAL_MACHINE\Software\Microsoft into a pair of
    // values used by the underlying API: An integer hive constant and a byte array
    // of the key path within that hive.
    private static Object[] keyParts(String fullKeyName) {
        int x = fullKeyName.indexOf('\\');
        String hiveName = x >= 0 ? fullKeyName.substring(0, x) : fullKeyName;
        String keyName = x >= 0 ? fullKeyName.substring(x + 1) : "";
        Integer hkey = hives.get(hiveName);
        if (hkey == null) throw new RegError("Unknown registry hive: " + hiveName, null);
        return new Object[]{hkey, toByteArray(keyName)};
    }


    // Type encapsulating a native handle to a registry key
    private static class Key implements AutoCloseable {
        final int handle;

        private Key(int handle) {
            this.handle = handle;
        }

        static Key open(String keyName, int accessMode) {
            Object[] keyParts = keyParts(keyName);
            int[] ret = invoke(regOpenKey, keyParts[0], keyParts[1], accessMode);
            checkError(ret[INFO_ERROR_CODE]);
            return new Key(ret[INFO_HANDLE]);
        }

        @Override
        public void close() {
            invoke(regCloseKey, handle);
        }
    }


    // Array index constants for results of regOpenKey, regCreateKeyEx, and regQueryInfoKey
    private static final int
            INFO_HANDLE = 0,
            INFO_COUNT_KEYS = 0,
            INFO_ERROR_CODE = 1,
            INFO_COUNT_VALUES = 2,
            INFO_MAX_KEY_LENGTH = 3,
            INFO_MAX_VALUE_LENGTH = 4;


    // Registry access mode constants from winnt.h
    private static final int
            KEY_READ = 0x20019,
            KEY_WRITE = 0x20006;


    // Error constants from winerror.h
    private static final int
            ERROR_SUCCESS = 0,
            ERROR_FILE_NOT_FOUND = 2,
            ERROR_ACCESS_DENIED = 5;

    private static void checkError(int e) {
        if (e == ERROR_SUCCESS) return;
        throw new RegError(
                e == ERROR_FILE_NOT_FOUND ? "Key not found" :
                        e == ERROR_ACCESS_DENIED ? "Access denied" :
                                ("Error number " + e), null);
    }


    // Registry access methods in java.util.prefs.WindowsPreferences
    private static final Method
            regOpenKey = getMethod("WindowsRegOpenKey1", int.class, byte[].class, int.class),
            regCloseKey = getMethod("WindowsRegCloseKey", int.class),
            regQueryValueEx = getMethod("WindowsRegQueryValueEx", int.class, byte[].class),
            regQueryInfoKey = getMethod("WindowsRegQueryInfoKey1", int.class),
            regEnumValue = getMethod("WindowsRegEnumValue1", int.class, int.class, int.class),
            regEnumKeyEx = getMethod("WindowsRegEnumKeyEx1", int.class, int.class, int.class),
            regSetValueEx = getMethod("WindowsRegSetValueEx1", int.class, byte[].class, byte[].class),
            regDeleteValue = getMethod("WindowsRegDeleteValue", int.class, byte[].class),
            regDeleteKey = getMethod("WindowsRegDeleteKey", int.class, byte[].class),
            regCreateKeyEx = getMethod("WindowsRegCreateKeyEx1", int.class, byte[].class);

    private static Throwable initError;

    private static Method getMethod(String methodName, Class<?>... parameterTypes) {
        try {
            Method m = java.util.prefs.Preferences.systemRoot().getClass()
                    .getDeclaredMethod(methodName, parameterTypes);
            m.setAccessible(true);
            return m;
        } catch (Throwable t) {
            initError = t;
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(Method method, Object... args) {
        if (initError != null)
            throw new RegError("Registry methods are not available", initError);
        try {
            return (T) method.invoke(null, args);
        } catch (Exception e) {
            throw new RegError(null, e);
        }
    }


    // Conversion of strings to/from null-terminated byte arrays.
    // There is no support for Unicode; sorry, this is a limitation
    // of the underlying methods that Java makes available.
    private static byte[] toByteArray(String str) {
        byte[] bytes = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++)
            bytes[i] = (byte) str.charAt(i);
        return bytes;
    }

    private static String fromByteArray(byte[] bytes) {
        if (bytes == null) return null;
        char[] chars = new char[bytes.length - 1];
        for (int i = 0; i < chars.length; i++)
            chars[i] = (char) ((int) bytes[i] & 0xFF);
        return new String(chars);
    }
}