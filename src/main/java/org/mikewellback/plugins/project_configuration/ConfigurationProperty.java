package org.mikewellback.plugins.project_configuration;

import com.intellij.openapi.vfs.LocalFileSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConfigurationProperty {

    public static final String CONFIG_FILE_NAME = "config.properties";

    public enum PropertyType {
        BOOLEAN, NUMBER, TEXT, ARRAY, LOCKED, UNDEFINED
    }

    private String name;
    private String value;
    private PropertyType type;
    private String[] values;
    private String comment;

    public ConfigurationProperty(String line) {
        name = "";
        value = "";
        type = PropertyType.UNDEFINED;
        values = new String[0];
        comment = line;
    }

    public ConfigurationProperty(String[] vals, String prevLine) {
        if (vals == null || vals.length < 1) {
            name = "";
            value = "";
        } else {
            name = vals[0];
            if (vals.length < 2) {
                value = "";
            } else {
                StringBuilder build = new StringBuilder();
                for (int i = 1; i < vals.length; i++) {
                    if (build.length() > 0) {
                        build.append("=");
                    }
                    build.append(vals[i]);
                }
                value = build.toString();
            }
        }
        if (prevLine == null || prevLine.isBlank()) {
            type = PropertyType.TEXT;
            values = new String[0];
            comment = prevLine;
        } else {
            String[] args = prevLine.split(" ");
            if (args.length < 1 || args[0].length() < 2) {
                type = PropertyType.TEXT;
                values = new String[0];
                comment = prevLine;
            } else {
                String typeCh = args[0].substring(1, 2);
                switch (typeCh) {
                    case "b":
                        type = PropertyType.BOOLEAN;
                        values = new String[0];
                        comment = prevLine.substring(2);
                        break;
                    case "n":
                        type = PropertyType.NUMBER;
                        values = new String[0];
                        comment = prevLine.substring(2);
                        break;
                    case "t":
                        type = PropertyType.TEXT;
                        values = new String[0];
                        comment = prevLine.substring(2);
                        break;
                    case "a":
                        type = PropertyType.ARRAY;
                        if (args.length - 1 < 1) {
                            values = new String[0];
                            comment = null;
                        } else {
                            int commIndex = -1;
                            boolean okNext = false;
                            StringBuilder spaces = new StringBuilder();
                            for (int i = 2; i < prevLine.length(); i++) {
                                String sub = prevLine.substring(i, i + 1);
                                if ("\\".equals(sub)) {
                                    okNext = true;
                                    spaces = new StringBuilder();
                                } else if (!okNext && "#".equals(sub)) {
                                    commIndex = i;
                                    i = prevLine.length();
                                    break;
                                } else {
                                    okNext = false;
                                    if (" ".equals(sub)) {
                                        spaces.append(sub);
                                    } else {
                                        spaces = new StringBuilder();
                                    }
                                }
                            }
                            String[] fields = prevLine.substring(0, commIndex).split(" ");
                            values = new String[fields.length - 1];
                            for (int i = 1; i < fields.length; i++) {
                                StringBuilder fixed = new StringBuilder();
                                boolean prevSlash = false;
                                for (int c = 0; c < fields[i].length(); c++) {
                                    String sub = fields[i].substring(c, c + 1);
                                    if (prevSlash) {
                                        prevSlash = false;
                                        switch (sub) {
                                            case "s":
                                                fixed.append(" ");
                                                break;
                                            case "h":
                                                fixed.append("#");
                                                break;
                                            case "\\":
                                                fixed.append("\\");
                                                break;
                                            default:
                                                fixed.append("\\").append(sub);
                                        }
                                    } else if ("\\".equals(sub)) {
                                        prevSlash = true;
                                    } else {
                                        fixed.append(sub);
                                    }
                                }
                                values[i - 1] = fixed.toString();
                            }
                            comment = spaces.toString() + prevLine.substring(commIndex);
                        }
                        break;
                    case "l":
                        type = PropertyType.LOCKED;
                        values = new String[0];
                        comment = prevLine.substring(2);
                        break;
                    default:
                        type = PropertyType.UNDEFINED;
                        values = new String[0];
                        comment = prevLine;
                        break;
                }
            }
        }
    }

    public static File getFile(String filePath) {
        return new File(filePath + "/" + ConfigurationProperty.CONFIG_FILE_NAME);
    }

    public static void writeProperties(String filePath, ConfigurationProperty[] props) {
        File file = getFile(filePath);
        try (FileWriter fw = new FileWriter(file)) {
            fw.flush();
            for (ConfigurationProperty prop: props) {
                String line = "";
                String line2 = null;
                if (!prop.isValue()) {
                    line = prop.getComment();
                } else {
                    switch (prop.getType()) {
                        case BOOLEAN:
                            line = "#b" + prop.getComment();
                            break;
                        case NUMBER:
                            line = "#n" + prop.getComment();
                            break;
                        case TEXT:
                            line = "#t" + prop.getComment();
                            break;
                        case ARRAY:
                            StringBuilder b = new StringBuilder();
                            for (String v : prop.getValues()) {
                                b.append(" ");
                                StringBuilder sb = new StringBuilder();
                                boolean prevSlash = false;
                                for (int c = 0; c < v.length(); c++) {
                                    String sub = v.substring(c, c + 1);
                                    if (prevSlash) {
                                        prevSlash = false;
                                        switch (sub) {
                                            case "s":
                                                sb.append("\\\\s");
                                                break;
                                            case "h":
                                                sb.append("\\\\h");
                                                break;
                                            case " ":
                                                sb.append("\\\\\\s");
                                                break;
                                            case "#":
                                                sb.append("\\\\\\h");
                                                break;
                                            case "\\":
                                            default:
                                                sb.append("\\").append(sub);
                                        }
                                    } else if ("\\".equals(sub)) {
                                        prevSlash = true;
                                    } else if (" ".equals(sub)) {
                                        sb.append("\\s");
                                    } else if ("#".equals(sub)) {
                                        sb.append("\\h");
                                    } else {
                                        sb.append(sub);
                                    }
                                }
                                b.append(sb.toString());
                            }
                            line = "#a" + b.toString() + prop.getComment();
                            break;
                        case LOCKED:
                            line = "#l" + prop.getComment();
                            break;
                        case UNDEFINED:
                            line = prop.getComment();
                            break;
                    }
                    line2 = prop.getName() + "=" + prop.getValue();
                }
                fw.append(line);
                fw.append('\n');
                System.out.println(line);
                if (line2 != null) {
                    fw.append(line2);
                    fw.append('\n');
                    System.out.println(line2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LocalFileSystem.getInstance().refreshIoFiles(List.of(new File[]{file}));
    }

    public static ConfigurationProperty[] readProperties(String filePath) {
        List<ConfigurationProperty> l = new ArrayList<>();
        String prevLine = null;
        try (Scanner input = new Scanner(getFile(filePath))) {
            while (input.hasNextLine()) {
                String line = input.nextLine();
                if (line.startsWith("#")) {
                    if (prevLine != null) {
                        l.add(new ConfigurationProperty(prevLine));
                    }
                    prevLine = line;
                } else if (!line.isBlank()) {
                    String[] sp = line.split("=");
                    if (sp.length > 1) {
                        l.add(new ConfigurationProperty(sp, prevLine));
                        prevLine = null;
                    } else if (prevLine != null) {
                        l.add(new ConfigurationProperty(prevLine));
                        prevLine = null;
                    }
                } else if (prevLine != null) {
                    l.add(new ConfigurationProperty(prevLine));
                    prevLine = null;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ConfigurationProperty[] confProps = new ConfigurationProperty[l.size()];
        for (int i = 0; i < l.size(); i++) {
            confProps[i] = l.get(i);
        }
        return confProps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public boolean getBooleanValue() {
        return "true".equals(this.value);
    }

    public Number getNumberValue() {
        try {
            return Integer.parseInt(this.value);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(this.value);
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    public String getSelectedValue() {
        for (String v: this.values) {
            if (v.equals(this.value)) {
                return v;
            }
        }
        return this.value;
    }

    public boolean isValue() {
        return this.name != null && !this.name.isBlank();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
