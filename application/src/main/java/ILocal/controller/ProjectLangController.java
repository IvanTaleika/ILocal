package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.LangRepository;
import ILocal.repository.ProjectLangRepository;
import ILocal.service.*;
import java.io.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private BitFlagService bitFlag;

    @Autowired
    private ParseFile parseFile;

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

    @PostMapping("/{id}/import-translations")
    public List<TermLang> importTranslations(@PathVariable("id") ProjectLang projectLang, MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        return projectLangService.importTranslations(projectLang, convFile);
    }

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

    @GetMapping("/{id}/get-default")
    public ProjectLang getDefaultLang(@PathVariable("id") Project project){
        for (ProjectLang projectLang : project.getProjectLangs()) {
        if(projectLang.isDefault()) return projectLang;
        }
        return null;
    }
}
