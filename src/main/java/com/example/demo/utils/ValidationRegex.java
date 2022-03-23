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
    public static boolean isRegexPhone(String target) {
        String regex = "^01([0|1|6|7|8|9]?)([0-9]{3,4})([0-9]{4})$";
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

    public static boolean isRegexPwdLen(String target){
        String regex = "^.{8,20}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    public static boolean isRegexPwdThreeSame(String target){
        String regex = "(\\w)\\1\\1";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    public static boolean isRegexPwdContinuous(String target){
        int first = 0;
        int second = 0;

        for (int i=0; i<target.length()-2; i++) {
            char firstVal = target.charAt(i);
            char secondVal = target.charAt(i+1);
            char thirdVal = target.charAt(i+2);
            first =secondVal-firstVal;
            second = thirdVal-secondVal;
            if(Math.abs(first)==1 && Math.abs(second)==1){
                return false;
            }
        }
        return true;
    }

}

