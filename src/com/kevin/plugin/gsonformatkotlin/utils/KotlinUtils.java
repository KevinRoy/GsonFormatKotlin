package com.kevin.plugin.gsonformatkotlin.utils;

import com.google.gson.*;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by seal.wu on 2017/8/21.
 */
public class KotlinUtils {
    private final static Gson gson = new Gson();

    private String className;
    private JsonElement inputElement;
    private boolean isData;

    private Set<String> toBeAppend = new HashSet<String>();

    public KotlinUtils(String inputText, boolean isData) {
        this.inputElement = gson.fromJson(inputText, JsonElement.class);
        this.isData = isData;
    }

    public KotlinUtils(JsonElement jsonElement, boolean isData) {
        this.inputElement = jsonElement;
        this.isData = isData;
    }

    public KotlinUtils(String className, String inputText) {
        this.inputElement = gson.fromJson(inputText, JsonElement.class);
        this.className = className;
    }

    public KotlinUtils(String className, JsonElement jsonElement) {
        this.inputElement = jsonElement;
        this.className = className;
    }

    public String makeKotlinData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");

        JsonElement jsonElement = inputElement;
        if (jsonElement.isJsonObject()) {
            appendClassName(stringBuilder);
            appendJsonObject(stringBuilder, (JsonObject) jsonElement);
        } else {
            throw new IllegalArgumentException("object is not jsonObject");
        }

        int index = stringBuilder.lastIndexOf(",");
        if (index != -1) {
            stringBuilder.deleteCharAt(index);
        }

        if (!TextUtils.isEmpty(className))
            stringBuilder.append("}");

        for (String append : toBeAppend) {
            stringBuilder.append("\n");
            stringBuilder.append(append);
        }
        return stringBuilder.toString();
    }

    //添加class
    private void appendClassName(StringBuilder stringBuilder) {
        if (!TextUtils.isEmpty(className))
            stringBuilder.append("class ").append(className).append("{\n");
    }

    private void appendJsonObject(StringBuilder stringBuilder, JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String property = entry.getKey();
            JsonElement jsonElementValue = entry.getValue();
            String type = "String";
            if (jsonElementValue.isJsonArray()) {
                type = getArrayType(property, (JsonArray) jsonElementValue);
                addProperty(stringBuilder, property, type, null);
            } else if (jsonElementValue.isJsonPrimitive()) {
                type = getPrimitiveType(jsonElementValue);
                addProperty(stringBuilder, property, type, jsonElementValue.getAsString());
            } else if (jsonElementValue.isJsonObject()) {
                type = getJsonObjectType(property, (JsonObject) jsonElementValue);
                addProperty(stringBuilder, property, type, null);
            } else if (jsonElementValue.isJsonNull()) {
                addProperty(stringBuilder, property, type, null);
            }
        }
    }

    @NotNull
    private String getJsonObjectType(String property, JsonObject jsonElementValue) {
        String type;
        type = property.subSequence(0, 1).toString().toUpperCase() + property.subSequence(1, property.length());
        toBeAppend.add(new KotlinUtils(type, jsonElementValue).makeKotlinData());
        return type;
    }

    @NotNull
    private String getArrayType(String property, JsonArray jsonElementValue) {
        String type = "String";
        JsonArray jsonArray = jsonElementValue;
        JsonElement next = jsonArray.iterator().next();
        if (next.isJsonPrimitive()) {
            String subType = getPrimitiveType(next);
            type = "List" + "<" + subType + ">";

        } else if (next.isJsonObject()) {
            property = modifyPropertyForArrayObjType(property);
            String subType = getJsonObjectType(property, (JsonObject) next);
            type = "List" + "<" + subType + ">";
            /**
             * 处理子类
             */
            toBeAppend.add(new KotlinUtils(subType, next).makeKotlinData());
        } else if (next.isJsonArray()) {
            property = modifyPropertyForArrayObjType(property);
            String subType = getArrayType(property, (JsonArray) next);
            type = "List" + "<" + subType + ">";

        } else if (next.isJsonNull()) {
            type = "List" + "<" + "String" + ">";

        }

        return type;
    }

    @NotNull
    private String modifyPropertyForArrayObjType(String property) {
        if (property.endsWith("ies")) {
            property = property.substring(0, property.length() - 3).concat("y");
        } else if (property.contains("List")) {
            int firstLatterAfterListIndex = property.lastIndexOf("List") + 4;
            if (property.length() > firstLatterAfterListIndex) {
                char c = property.charAt(firstLatterAfterListIndex);
                if (c >= 'A' && c <= 'Z') {
                    String pre = property.substring(0, property.lastIndexOf("List"));
                    String end = property.substring(firstLatterAfterListIndex, property.length());
                    property = pre.concat(end);
                }
            } else if (property.length() == firstLatterAfterListIndex) {
                property = property.substring(0, property.lastIndexOf("List"));
            }
        } else if (property.contains("list")) {
            if (property.indexOf("list") == 0) {
                String end = property.substring(5);
                String pre = (property.charAt(4) + "").toLowerCase();
                property = pre.concat(end);
            }
        } else if (property.endsWith("s")) {
            property = property.substring(0, property.length() - 1);
        }

        return property;
    }

    private void addProperty(StringBuilder stringBuilder, String property, String type, String value) {
        if (isData)
            stringBuilder.append("\t\tval ").append(property).append(": ").append(type).append(",");
        else {
            if (type.equals("String")) {
                type = type + "? = null";
            } else if (type.equals("Boolean")) {
                type = type + " = false";
            } else if (type.equals("Double")) {
                type = type + " = 0.toDouble()";
            } else if (type.equals("Long")) {
                type = type + " = 0";
            } else if (type.equals("Int")) {
                type = type + " = 0";
            } else if (type.contains("List")) {
                type = type + "? = null";
            }
            stringBuilder.append("\t\tvar ").append(property).append(": ").append(type);
        }

        if (value != null) {
            stringBuilder.append("// ").append(value).append("\n");
        } else {
            stringBuilder.append("\n");
        }
    }

    @NotNull
    private String getPrimitiveType(JsonElement next) {
        String subType = "String";
        JsonPrimitive asJsonPrimitive = next.getAsJsonPrimitive();
        if (asJsonPrimitive.isBoolean()) {
            subType = "Boolean";
        } else if (asJsonPrimitive.isNumber()) {
            if (asJsonPrimitive.getAsString().contains(".")) {
                subType = "Double";
            } else if (asJsonPrimitive.getAsLong() > Integer.MAX_VALUE) {
                subType = "Long";
            } else {
                subType = "Int";
            }
        } else if (asJsonPrimitive.isString()) {
            subType = "String";
        }
        return subType;
    }
}
