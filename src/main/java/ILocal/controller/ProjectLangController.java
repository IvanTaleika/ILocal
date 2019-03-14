package ILocal.controller;

import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/project-lang")
public class ProjectLangController {

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private ProjectLangService projectLangService;

    @Autowired
    public BitFlagService bitFlag;

    @GetMapping
    public List<ProjectLang> getProjectLangs() {
        return projectLangRepository.findAll();
    }

    @GetMapping("/{id}")
    public ProjectLang getProjectLang(@PathVariable("id") ProjectLang projectLang) {
        projectLang.getTermLangs().stream()
                .forEach(a -> {
                    EnumSet<BitFlagService.StatusFlag> flags = bitFlag.getStatusFlags(a.getStatus());
                    List<String> list = new ArrayList<>();
                    flags.forEach(b -> list.add(b.toString()));
                    a.setFlags(list);
                });
        return projectLang;
    }

    //СКРЕЕ ВСЕГО НАФИГ
    @GetMapping("/project/{id}")
    public List<ProjectLang> getLangsByProject(@PathVariable("id") Project project) {
        if (project == null) return null;
        return project.getProjectLangs();
    }

    @PutMapping("/{id}/update-lang")
    public void updateLang(@PathVariable("id") ProjectLang projectLang, @RequestBody long id) {
        Lang lang = langRepository.findById(id);
        projectLang.setLang(lang);
        projectLang.getTermLangs().forEach(a -> {
            a.setValue("");
            a.setLang(lang);
        });
        projectLangRepository.save(projectLang);
    }

    @PostMapping("/{id}/empty")
    public void empty(@PathVariable("id") ProjectLang projectLang, @RequestBody ProjectLang newLang) {
        projectLang.setTermLangs(newLang.getTermLangs());
        projectLangRepository.save(projectLang);
    }

    @GetMapping("/{id}/filter")
    public List<TermLang> filterTermLang(@PathVariable("id") ProjectLang projectLang,
                                         @RequestParam(required = false) Boolean untranslated,
                                         @RequestParam(required = false) Boolean fuzzy) {
        return projectLangService.filter(projectLang, untranslated, fuzzy);
    }

    @PostMapping("/{id}/import-translations")
    public void importTranslations(@PathVariable("id") ProjectLang projectLang, File file) throws IOException {
        projectLangService.importTranslations(projectLang, file);
    }

    @GetMapping("/{id}/sort")
    public List<TermLang> sort(@PathVariable("id") ProjectLang projectLang,
                               @RequestParam(required = false) String sort_order) {
        return projectLangService.sort(projectLang, sort_order);
    }

    @GetMapping("/{id}/search")
    public List<TermLang> search(@PathVariable("id") ProjectLang projectLang, @RequestParam String term){
        return projectLang.getTermLangs().stream()
                .filter(a-> a.getTerm().getTermValue().toLowerCase().contains(term.toLowerCase()))
                .collect(Collectors.toList());
    }
}
