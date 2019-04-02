package ILocal.controller;


import ILocal.entity.Lang;
import ILocal.entity.Project;
import ILocal.entity.ProjectLang;
import ILocal.entity.TermLang;
import ILocal.repository.LangRepository;
import ILocal.repository.ProjectLangRepository;
import ILocal.repository.ProjectRepository;
import ILocal.repository.TermLangRepository;
import ILocal.service.BitFlagService;
import ILocal.service.ProjectLangService;
import ILocal.service.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.util.EnumSet;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/project-lang")
public class ProjectLangController {

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private ProjectLangService projectLangService;

    @Autowired
    private BitFlagService bitFlag;

    @Autowired
    private TermLangRepository termLangRepository;

    @Autowired
    private Translator translator;

    @GetMapping
    public List<ProjectLang> getProjectLangs() {
        return projectLangRepository.findAll();
    }

    @GetMapping("/{id}/project/{projId}")
    public ProjectLang getProjectLang(@PathVariable("id") ProjectLang projectLang,
                                      @PathVariable("projId") Project project) {
        if (!project.getProjectLangs().contains(projectLang)) return null;
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        projectLang.setProjectName(project.getProjectName());
        return projectLang;
    }

    //СКРЕЕ ВСЕГО НАФИГ
    @GetMapping("/project/{id}")
    public List<ProjectLang> getLangsByProject(@PathVariable("id") Project project) {
        if (project == null) return null;
        return project.getProjectLangs();
    }

    @PutMapping("/{id}/update-lang")
    public ProjectLang updateLang(@PathVariable("id") ProjectLang projectLang, @RequestParam long id) {
        Lang lang = langRepository.findById(id);
        if(lang == null) return null;
        Project project = projectRepository.findById((long)projectLang.getProjectId());
        for (ProjectLang a : project.getProjectLangs()) {
            if(a.getLang().getId() == id) {
                return null;
            }
        }
        projectLang.setLang(lang);
        projectLang.getTermLangs().forEach(a -> {
            a.setValue("");
            a.setLang(lang);
        });
        projectLangRepository.save(projectLang);
        return projectLang;
    }

    @PostMapping("/{id}/empty")
    public void empty(@PathVariable("id") ProjectLang projectLang, @RequestBody ProjectLang newLang) {
        projectLang.setTermLangs(newLang.getTermLangs());
        projectLang.getTermLangs().forEach(a-> termLangRepository.save(a));
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
        if (projectLang == null) return null;
        return projectLangService.doFilter(projectLang, term, untranslated, fuzzy, sort_state);
    }

    @PostMapping("/{id}/flush-translations")
    public void flushTranslations(@PathVariable("id") ProjectLang projectLang) {
        projectLang.getTermLangs().forEach(a -> a.setValue(""));
        projectLangRepository.save(projectLang);
    }

    @GetMapping("/{id}/get-default")
    public ProjectLang getDefaultLang(@PathVariable("id") Project project) {
        for (ProjectLang projectLang : project.getProjectLangs()) {
            if (projectLang.isDefault()) return projectLang;
        }
        return null;
    }

    @PostMapping("/auto-translate")
    public List<TermLang> autoTranslate(@RequestBody ProjectLang projectLang, @RequestParam long from) {
        if (projectLang == null) return null;
        if (projectLang.getId() == from) return null;
        ProjectLang lang = projectLangRepository.findById(from);
        String langFrom = lang.getLang().getLangDef().toLowerCase();
        String langTo = projectLang.getLang().getLangDef().toLowerCase();
        lang.getTermLangs().forEach(a -> {
            projectLang.getTermLangs().forEach(b -> {
                if (b.getTerm().getTermValue().equals(a.getTerm().getTermValue()) && b.isSelected()) {
                    try {
                        String res = translator.translate(langFrom, langTo, a.getValue());
                        b.setValue(res);
                        EnumSet<BitFlagService.StatusFlag> enumSet = bitFlag.getStatusFlags(b.getStatus());
                        if(enumSet.contains(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)){
                            b.setStatus(b.getStatus() -  BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.getValue());
                            b.getFlags().remove(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.name());
                        }
                        if(enumSet.contains(BitFlagService.StatusFlag.FUZZY)){
                            b.setStatus(b.getStatus() -  BitFlagService.StatusFlag.FUZZY.getValue());
                            b.getFlags().remove(BitFlagService.StatusFlag.FUZZY.name());
                        }
                        termLangRepository.save(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        });
        return projectLang.getTermLangs();
    }

    @GetMapping("/{id}/export")
    public void download(@PathVariable("id") ProjectLang projectLang, HttpServletResponse response) throws IOException {
        File file = projectLangService.createPropertiesFile(projectLang);
        if (file.exists()) {
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));
            response.setContentLength((int) file.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
            file.delete();
        }
    }
}
