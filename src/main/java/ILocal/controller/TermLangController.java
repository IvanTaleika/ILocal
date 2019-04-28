package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.BitFlagService;
import ILocal.service.ProjectLangService;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    private ProjectRepository projectRepository;

    @Autowired
    private BitFlagService bitFlagService;

    @Autowired
    private ProjectLangService projectLangService;

    private static final Logger logger = Logger.getLogger(TermLangController.class);

    @PutMapping("/{id}/update")
    public TermLang updateValue(@PathVariable("id") TermLang termLang,
                                @RequestBody(required = false) String newVal,
                                @RequestParam(required = false) long writer_id,
                                @AuthenticationPrincipal User user,
                                HttpServletResponse response) throws IOException {
        logger.info("User "+user.getUsername()+" is trying to update term lang value");
        if (termLang == null) {
            logger.error("Term lang not found");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Term lang is not found!");
            return null;
        }
        if (accessDenied(termLang, user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        if (newVal == null) {
            logger.error("New value is null");
            if (bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.FUZZY)) {
                bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.FUZZY);
            }
            newVal = "";
        }
        if (bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED))
            bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.AUTOTRANSLATED);

        termLang.setValue(newVal);
        termLang.setModifier(user);
        termLang.setModifiedDate();
        if (projectLangRepository.findById(termLang.getProjectLangId()).isDefault()) {
            List<TermLang> termLangs = termLangRepository.findByTerm(termLang.getTerm());
            termLangs.remove(termLang);
            termLangs.forEach(a -> {
                if (!bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED) && !a.getValue().equals(""))
                    bitFlagService.addFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
            });
        } else {
            if (bitFlagService.isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED))
                bitFlagService.dropFlag(termLang, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
        }
        termLangRepository.save(termLang);
        projectLangService.setFlagsToTerm(termLang);
        logger.info("User "+user.getUsername()+" updated term lang value");
        return termLang;
    }

    @PutMapping("/{id}/fuzzy")
    public void fuzzy(@PathVariable("id") TermLang termLang, @AuthenticationPrincipal User user,
                      @RequestParam Boolean fuzzy, HttpServletResponse response) throws IOException {
        logger.info("User "+user.getUsername()+" is trying to mark term lang like fuzzy");
        if (termLang == null) {
            logger.error("Term lang not found");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Term lang is not found!");
            return;
        }
        if (accessDenied(termLang, user)) {
            logger.error("Access denied");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
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
        logger.info("User "+user.getUsername()+" marked term lang like fuzzy");
    }

    private boolean accessDenied(TermLang term, User user) {
        boolean isDenied = true;
        Project project = projectRepository.findById((long) term.getTerm().getProjectId());
        if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId()))
            isDenied = false;
        if (project.getAuthor().getId() == user.getId()) isDenied = false;
        return isDenied;
    }
}
