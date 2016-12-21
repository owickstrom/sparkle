package io.tweag.sparkle;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.*;

public class Sparkle {
    static {
        System.out.println("Loading Sparkle application ...");
        try {
            InputStream in =
                Sparkle.class.getResourceAsStream("/sparkle-app.zip");
            File sparkleAppZipFile =
                File.createTempFile("sparkle-app-", ".zip");
            Files.copy(in, sparkleAppZipFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
            in.close();
            try {
              loadApplication(sparkleAppZipFile, "hsapp");
            } finally {
              sparkleAppZipFile.delete();
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new ExceptionInInitializerError(e);
        }
        System.out.println("Application loaded.");
        Sparkle.initializeHaskellRTS();
    }

    public static void loadApplication(File archive, String appName)
        throws IOException
    {
        // Extract all files from the .zip archive.
        //
        ZipFile zip = new ZipFile(archive);
        String tmpDir = System.getProperty("java.io.tmpdir");
        Path sparkleAppTmpDir =
            Files.createTempDirectory(Paths.get(tmpDir), "sparkle-app-");
        ArrayList<Path> pathsList = new ArrayList();
        try {
          for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry)e.nextElement();
            InputStream in = zip.getInputStream(entry);
            Path path = sparkleAppTmpDir.resolve(entry.getName());
            pathsList.add(path);
            Files.copy(in, path);
            in.close();
          }
          zip.close();

          // Dynamically load the app.
          //
          System.load(sparkleAppTmpDir.resolve(appName).toString());
        } finally {
          // Delete the app binary and its libraries, now that they are loaded.
          //
          for (Path p : pathsList)
            p.toFile().delete();
        }
        sparkleAppTmpDir.toFile().delete();
    }

    public static native void bootstrap();
    public static native <R> R apply(byte[] cos, Object... args);
    private static native void initializeHaskellRTS();
}
