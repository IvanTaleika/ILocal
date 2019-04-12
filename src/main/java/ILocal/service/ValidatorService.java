package ILocal.service;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidatorService {

    public boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile("^([\\w\\-\\.]+)@((\\[([0-9]{1,3}\\.){3}[0-9]{1,3}\\])|(([\\w\\-]+\\.)+)([a-zA-Z]{2,4}))$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean validateUsername(String username) {
        Pattern pattern = Pattern.compile("^[a-zA-Z\\d\\._]{4,27}$");
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    public boolean validatePassword(String password) {
        Pattern pattern = Pattern.compile("^[a-zA-Z\\d\\._]{7,27}$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public boolean validateDescription(String description) {
        Pattern pattern = Pattern.compile("[^\\^\\{\\}\\[\\]]{1,5000}");
        Matcher matcher = pattern.matcher(description);
        return matcher.matches();
    }

    public boolean validateProjectName(String projectName) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9\\ ]{4,27}$");
        Matcher matcher = pattern.matcher(projectName);
        return matcher.matches();
    }

    public boolean validateFirstName(String firstName) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]{4,27}$");
        Matcher matcher = pattern.matcher(firstName);
        return matcher.matches();
    }

    public boolean validateLastName(String lastName) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]{4,27}$");
        Matcher matcher = pattern.matcher(lastName);
        return matcher.matches();
    }
}
