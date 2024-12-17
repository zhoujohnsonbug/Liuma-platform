package com.autotest.LiuMa.common.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class NameOrderUtils {

    public static List<String> stringToList(String input) {
        List<String> list = new ArrayList<>(Arrays.asList(input.split("\\|")));
        return list;
    }

    public static void moveFirstToEnd(List<String> list) {
        if (list.size() > 1) {
            String first = list.get(0);
            list.remove(0);
            list.add(first);
        }
    }

    public static String listToString(List<String> list) {
        String output = String.join("|", list);
        return output;
    }

    public static String convertString(String name) {
        List<String> list = stringToList(name);
        moveFirstToEnd(list);
        String output = listToString(list);
        return output;
    }


    public static String getFirstString(String input) {
        int index = input.indexOf("|");
        if (index != -1) {
            String first = input.substring(0, index);
            return first;
        } else {
            return "";
        }
    }
    public static void main(String[] args) {
        String input = "蔡娇|杨姗|张云华|蒋伟明|周泽强|陈威";
        String s = convertString(input);
        String s1 = convertString(s);
        System.out.println(s);
        String firstString = getFirstString(s);
        System.out.println(firstString);
    }
}
