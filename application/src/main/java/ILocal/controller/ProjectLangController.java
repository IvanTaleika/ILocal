package ILocal.controller;

import ILocal.entity.Lang;
import ILocal.entity.Project;
import ILocal.entity.ProjectLang;
import ILocal.entity.TermLang;
import ILocal.repository.LangRepository;
import ILocal.repository.ProjectLangRepository;
import ILocal.service.BitFlagService;
import ILocal.service.ProjectLangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    @GetMapping("/{id}/project/{projId}")
    public ProjectLang getProjectLang(@PathVariable("id") ProjectLang projectLang,
                                      @PathVariable("projId") Project project) {
        if (!project.getProjectLangs().contains(projectLang)) return null;
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
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
//
//    @GetMapping("/{id}/filter")
//    public List<TermLang> filterTermLang(@PathVariable("id") ProjectLang projectLang,
//                                         @RequestParam(required = false) Boolean untranslated,
//                                         @RequestParam(required = false) Boolean fuzzy) {
//        return projectLangService.filter(projectLang, untranslated, fuzzy);
//    }

    @PostMapping("/{id}/import-translations")
    public void importTranslations(@PathVariable("id") ProjectLang projectLang, File file) throws IOException {
        projectLangService.importTranslations(projectLang, file);
    }
//
//    @GetMapping("/{id}/sort")
//    public List<TermLang> sort(@PathVariable("id") ProjectLang projectLang,
//                               @RequestParam(required = false) String sort_state) {
//        return projectLangService.sort(projectLang, sort_state);
//    }

    @GetMapping("/{id}/filter")
    public List<TermLang> doFilter(@PathVariable("id") ProjectLang projectLang,
								   @RequestParam(required = false) String term,
								   @RequestParam(required = false) Boolean untranslated,
								   @RequestParam(required = false) Boolean fuzzy,
								   @RequestParam(required = false) String sort_state) {
        if(projectLang == null) return null;
        return projectLangService.doFilter(projectLang, term, untranslated, fuzzy, sort_state);
    }

    @PostMapping("/{id}/flush-translations")
    public void flushTranslations(@PathVariable("id") ProjectLang projectLang) {
        projectLang.getTermLangs().forEach(a -> a.setValue(""));
        projectLangRepository.save(projectLang);
    }
}
