/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package com.mchaze.walls.util;

import java.io.*;

public class FileUtil {

    public static void validate(File file, boolean isDir) {
        if (file.exists()) return;
        else if (isDir) file.mkdir();
        else
            try {
                file.createNewFile();
            } catch (IOException e) {
                Messaging.printErr("Error! Could not create new File [" + file.getName() + "]");
                e.printStackTrace();
            }
    }

    public static boolean copy(File source, File target) {
        try {
            copyDir(source, target);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void copyDir(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists())
                target.mkdir();
            String files[] = source.list();
            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(target, file);
                copyDir(srcFile, destFile);
            }
        } else {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);
            in.close();
            out.close();
        }
    }


    public static File getFromDir(File dir, String fileName, boolean isDir) {
        for (File file : dir.listFiles())
            if (isDir && !file.isDirectory()) continue;
            else if (!file.getName().equalsIgnoreCase(fileName)) continue;
            else return file;
        return null;
    }

    public static void deleteDirectory(File folder) {
        if (!folder.exists()) return;
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) deleteDirectory(file);
            else file.delete();
        }
        folder.delete();
    }
}
