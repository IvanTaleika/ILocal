package ILocal.controller;

import ILocal.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private MailService mailService;

    @PostMapping("/testSend")
    public void send() {
        mailService.send("zotehojo@datasoma.com", "active", "rabotaet");
    }
}

