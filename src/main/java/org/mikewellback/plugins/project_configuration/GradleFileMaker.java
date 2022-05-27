package org.mikewellback.plugins.project_configuration;

import com.intellij.openapi.vfs.LocalFileSystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GradleFileMaker {

    public static final String GRADLE_FILE_NAME = "config.gradle";

    public static String createGradleFileContent(ConfigurationProperty[] props) {
        StringBuilder baseBuilder = new StringBuilder("\n" +
                "buildscript {\n" +
                "    Properties props = new Properties()\n" +
                "    def propFile = file('config.properties')\n" +
                "    if (propFile.canRead()) {\n" +
                "        props.load(new FileInputStream(propFile))\n" +
                "    }\n" +
                "    def getOrEmpty = { name -> (props != null && props.containsKey(name)) ? props[name] : \"\" }\n");
        for (ConfigurationProperty prop: props) {
            if (!prop.isValue()) {
                continue;
            }
            baseBuilder
                    .append("    ext.")
                    .append(prop.getName())
                    .append(" = getOrEmpty('")
                    .append(prop.getName())
                    .append("')\n");
        }
        baseBuilder.append("}");
        return baseBuilder.toString();
    }

    public static void writeGradleFile(String filePath, ConfigurationProperty[] props) {
        writeGradleFile(filePath, createGradleFileContent(props));
    }

    public static void writeGradleFile(String filePath, String content) {
        File file = new File(filePath + "/" + GRADLE_FILE_NAME);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocalFileSystem.getInstance().refreshIoFiles(List.of(new File[]{file}));
    }
}
