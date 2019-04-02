package ILocal.controller;


import ILocal.entity.TermLang;
import ILocal.repository.ProjectLangRepository;
import ILocal.repository.TermLangRepository;
import ILocal.repository.UserRepository;
import ILocal.service.BitFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public void updateValue(@PathVariable("id") TermLang termLang, @RequestBody(required = false) String newVal, @RequestParam(required = false) long writer_id) {
        if(newVal == null) {
            EnumSet<BitFlagService.StatusFlag> enumSet = bitFlagService.getStatusFlags(termLang.getStatus());
            if(enumSet.contains(BitFlagService.StatusFlag.FUZZY)){termLang.setStatus(termLang.getStatus() - BitFlagService.StatusFlag.FUZZY.getValue());}
            newVal="";
        }
        termLang.setValue(newVal);
        termLang.setModifier(userRepository.findById(writer_id));
        termLang.setModifiedDate(new Date(Calendar.getInstance().getTime().getTime()));
        if (projectLangRepository.findById(termLang.getProjectLangId()).isDefault()) {
            List<TermLang> termLangs = termLangRepository.findByTerm(termLang.getTerm());
            termLangs.remove(termLang);

            termLangs.forEach(a -> {
                EnumSet<BitFlagService.StatusFlag> enumSet = bitFlagService.getStatusFlags(a.getStatus());
                if(!enumSet.contains(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED) && !a.getValue().isEmpty())
                a.setStatus(a.getStatus() + BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.getValue());
            });
        }else{
            EnumSet<BitFlagService.StatusFlag> enumSet = bitFlagService.getStatusFlags(termLang.getStatus());
            if(enumSet.contains(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)){
                termLang.setStatus(termLang.getStatus() -  BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.getValue());
            }
        }
        termLangRepository.save(termLang);
    }

    @PutMapping("/{id}/fuzzy")
    public void fuzzy(@PathVariable("id") TermLang termLang, @RequestParam Boolean fuzzy) {
        EnumSet<BitFlagService.StatusFlag> enumSet = bitFlagService.getStatusFlags(termLang.getStatus());
        if (fuzzy != null && fuzzy) {
            if(!enumSet.contains(BitFlagService.StatusFlag.FUZZY))
            termLang.setStatus(termLang.getStatus() + BitFlagService.StatusFlag.FUZZY.getValue());
        } else if (fuzzy != null) {
            if(enumSet.contains(BitFlagService.StatusFlag.FUZZY))
                termLang.setStatus(termLang.getStatus() - BitFlagService.StatusFlag.FUZZY.getValue());
        }
        termLangRepository.save(termLang);
    }
}
