package ILocal.controller;

import ILocal.entity.TermLang;
import ILocal.repository.ProjectLangRepository;
import ILocal.repository.TermLangRepository;
import ILocal.repository.UserRepository;
import ILocal.service.BitFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/term-lang")
public class TermLangController {

    @Autowired
    private TermLangRepository termLangRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private BitFlagService bitFlagService;

    @GetMapping
    public List<TermLang> getAll() {
        return termLangRepository.findAll();
    }

    @PutMapping("/{id}/update")
    public void updateValue(@PathVariable("id") TermLang termLang,
                            @RequestBody(required = false) String newVal,
                            @RequestParam(required = false) long writer_id,
                            HttpServletResponse response) throws IOException {
        if (termLang == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Term lang is not found!");
            return;
        }
        if (newVal == null) {
            if (bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.FUZZY)) {
                bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.FUZZY);
            }
            newVal = "";
        }
        if (bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED))
            bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.AUTOTRANSLATED);

        termLang.setValue(newVal);
        termLang.setModifier(userRepository.findById(writer_id));
        termLang.setModifiedDate(new Date(Calendar.getInstance().getTime().getTime()));
        if (projectLangRepository.findById(termLang.getProjectLangId()).isDefault()) {
            List<TermLang> termLangs = termLangRepository.findByTerm(termLang.getTerm());
            termLangs.remove(termLang);
            termLangs.forEach(a -> {
                if (!bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED))
                    bitFlagService.addFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
            });
        } else {
            if (bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED))
                bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
        }
        termLangRepository.save(termLang);
    }

    @PutMapping("/{id}/fuzzy")
    public void fuzzy(@PathVariable("id") TermLang termLang, @RequestParam Boolean fuzzy, HttpServletResponse response) throws IOException {
        if (termLang == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Term lang is not found!");
            return;
        }
        if (fuzzy != null && fuzzy) {
            if (!bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.FUZZY))
               bitFlagService.addFlag(termLang, BitFlagService.StatusFlag.FUZZY);
        } else if (fuzzy != null) {
            if (bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.FUZZY))
                bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.FUZZY);
        }
        termLangRepository.save(termLang);
    }
}
