package ILocal.controller;

import ILocal.entity.Project;
import ILocal.entity.ProjectLang;
import ILocal.repository.LangRepository;
import ILocal.repository.ProjectLangRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/project-lang")
public class ProjectLangController {

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private LangRepository langRepository;

    @GetMapping
    public List<ProjectLang> getProjectLangs() {
        return projectLangRepository.findAll();
    }

    @GetMapping("/{id}")
    public ProjectLang getProjectLang(@PathVariable("id") ProjectLang projectLang) {
        return projectLang;
    }

    @GetMapping("/project/{id}")
    public List<ProjectLang> getLangsByProject(@PathVariable("id") Project project) {
        if (project == null) {
            return null;
        }
        return project.getProjectLangs();
    }

    @PutMapping("/{id}/update-lang")
    public void updateLang(@PathVariable("id") ProjectLang projectLang, @RequestBody long id) {
        projectLang.setLang(langRepository.findById(id));
        projectLang.getTermLangs().forEach(a -> a.setValue(""));
        projectLangRepository.save(projectLang);
    }
}
