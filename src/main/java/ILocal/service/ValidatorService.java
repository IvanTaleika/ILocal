package ILocal.service;

import ILocal.entity.User;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ValidatorService {

    public boolean validateEmail(String email) {
        return Pattern.compile("^([\\w\\-\\.]+)@((\\[([0-9]{1,3}\\.){3}[0-9]{1,3}\\])|(([\\w\\-]+\\.)+)([a-zA-Z]{2,4}))$").matcher(email).matches();
    }

    public boolean validateUsername(String username) {
        return Pattern.compile("^[a-zA-Z\\d\\._]{4,27}$").matcher(username).matches();
    }

    public boolean validatePassword(String password) {
        return Pattern.compile("^[a-zA-Z\\d\\._]{6,27}$").matcher(password).matches();
    }

    public boolean validateDescription(String description) {
        return Pattern.compile("[^\\^\\{\\}\\[\\]]{1,5000}").matcher(description).matches();
    }

    public boolean validateProjectName(String projectName) {
        return Pattern.compile("^[a-zA-Zа-яА-Я0-9\\s]{1,27}$").matcher(projectName).matches();
    }

    public boolean validateFirstName(String firstName) {
        return Pattern.compile("^[a-zA-Zа-яА-Я]{4,27}").matcher(firstName).matches();
    }

    public boolean validateLastName(String lastName) {
        return Pattern.compile("^[a-zA-Zа-яА-Я]{4,27}").matcher(lastName).matches();
    }

    public boolean validateTermValue(String term) {
        return Pattern.compile("[a-zA-Zа-яА-Я.\\s\\d]{1,100}").matcher(term).matches();
    }

    public boolean validateContactValue(String contact) {
        return Pattern.compile("[+\\d@\\/a-zA-Z_.\\-\\\\]{1,30}").matcher(contact).matches();
    }

    public boolean validateCompanyValue(String company) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s]{1,70}").matcher(company).matches();
    }

    public boolean validatePeriodValue(String period) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s]{1,70}").matcher(period).matches();
    }

    public boolean validatePositionValue(String position) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s]{1,70}").matcher(position).matches();
    }

    public boolean validateActivityValue(String activity) {
        return Pattern.compile(".{1,2000}").matcher(activity).matches();
    }

    public boolean validateLevelValue(String level) {
        return Pattern.compile("[\\da-zA-Zа-яА-Я.,\\-\\s<>:;?!()]{1,200}").matcher(level).matches();
    }

    public boolean validateUser(User user) {
        return validateUsername(user.getUsername()) &&
                validatePassword(user.getPassword()) &&
                validatePassword(user.getRepeatPassword()) &&
                validateEmail(user.getEmail()) &&
                validateFirstName(user.getFirstName()) &&
                validateLastName(user.getLastName());
    }
}
