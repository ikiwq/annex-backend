package com.annex.backend.services.mail;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CredentialChecker {
    public boolean isEmailValid(String email){

    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
    "[a-zA-Z0-9_+&*-]+)*@" +
    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
    "A-Z]{2,7}$";

    Pattern pat = Pattern.compile(emailRegex);

    if (email == null) return false;

    return pat.matcher(email).matches();
    }

    public boolean isUsernameValid(String username){
        String regex = "^[A-Za-z]\\w{5,29}$";
        Pattern p = Pattern.compile(regex);

        if(username == null) return false;

        Matcher m = p.matcher(username);
        return m.matches();
    }
}
