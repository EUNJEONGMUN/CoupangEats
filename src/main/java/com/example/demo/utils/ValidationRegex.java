package com.example.demo.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationRegex {
    public static boolean isRegexEmail(String target) {
        String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    public static boolean isRegexPwd(String target) {
        String regex = "(?:[a-zA-Z]+[0-9]+$)|(?:[a-zA-Z]+[^a-zA-Z0-9\\n]+$)|(?:[0-9]+[a-zA-Z]+$)|(?:[0-9]+[^a-zA-Z0-9\\n]+$)|(?:[^a-zA-Z0-9\\n]+[a-zA-Z0-9]+$)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    // 같은 문자 숫자 3자리
    public static boolean samePwd(String target) {
        String regex = "(\\w)\\1\\1";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

}

