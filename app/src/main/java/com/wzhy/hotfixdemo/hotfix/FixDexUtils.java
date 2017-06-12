package com.wzhy.hotfixdemo.hotfix;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by wzhy on 2017/6/8. Refer to the lesson of dong nao.
 */

public class FixDexUtils {

    private static HashSet<File> loadedDex = new HashSet<File>();

    static {
        loadedDex.clear();
    }

    public static void loadFixedDex(Context context){
        if (context == null) return;
        //遍历所有的修复的dex
        File fileDir = context.getDir(Consts.DEX_DIR, Context.MODE_PRIVATE);
        File[] listFiles = fileDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                loadedDex.add(file);//存入集合
            }
        }
        //dex合并之前的dex
        doDexInject(context, fileDir, loadedDex);
    }

    private static void doDexInject(Context appContext, File fileDir, HashSet<File> loadedDex) {
        String optimizeDir = fileDir.getAbsolutePath() + File.separator + "opt_dex";
        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
        //1.加载应用的dex
        try {
            PathClassLoader pathLoader = (PathClassLoader) appContext.getClassLoader();
            for (File dex : loadedDex) {
                //2.加载指定修复的dex文件。
                DexClassLoader dexLoader = new DexClassLoader(
                        dex.getAbsolutePath(),
                        fopt.getAbsolutePath(),
                        null,
                        pathLoader
                );

                //合并
                Object dexObj = getPathList(dexLoader);
                Object pathObj = getPathList(pathLoader);
                Object dDexElements = getDexElements(dexObj);
                Object pathDexElements = getDexElements(pathObj);

                //合并完成
                Object dexElements = combineArray(dDexElements, pathDexElements);
                //重新给pathList里面的Element[] dexElements;赋值
                Object pathList = getPathList(pathLoader);
                setField(pathList, pathList.getClass(), "dexElements", dexElements);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static Object getPathList(Object baseClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(baseClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
        return getField(obj, obj.getClass(), "dexElements");
    }

    private static Object getField(Object obj, Class<?> cl, String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    private static void setField(Object obj, Class<?> cl, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    /**
     * 两个数组合并
     * @param arrNew 新数组
     * @param arrOld 旧数组
     * @return 合并后的数组
     */
    private static Object combineArray(Object arrNew, Object arrOld) {
        Class<?> localClass = arrNew.getClass().getComponentType();
        int i = Array.getLength(arrNew);
        int j = i + Array.getLength(arrOld);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; k++) {
            if (k < i) {
                Array.set(result, k, Array.get(arrNew, k));
            } else {
                Array.set(result, k, Array.get(arrOld, k - i));
            }
        }
        return result;
    }
}
