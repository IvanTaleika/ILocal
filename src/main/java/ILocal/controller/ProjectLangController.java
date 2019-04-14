package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
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
    public ProjectLang getProjectLang(@PathVariable("id") ProjectLang projectLang, HttpServletResponse response,
                                      @PathVariable("projId") Project project,
                                      @AuthenticationPrincipal User user,
                                      @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        if (project == null || projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find project or project lang!");
            return null;
        }
        if (!project.getProjectLangs().contains(projectLang)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project hasn't contain project lang!");
            return null;
        }
        if (accessDenied(projectLang, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        projectLangService.setPagesCount(projectLang);
        projectLangService.setCounts(projectLang);
        projectLang.setTermLangs(termLangRepository.findByProjectLangId(projectLang.getId(), page));
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        projectLang.setProjectName(project.getProjectName());
        return projectLang;
    }

    @GetMapping("/{id}/pages-count")
    public long getPagesCount(@PathVariable("id") ProjectLang projectLang, HttpServletResponse response) throws IOException {
        if (projectLang == null) {
            response.sendError(404);
            return -1;
        }
        projectLangService.setPagesCount(projectLang);
        return projectLang.getPagesCount();
    }


    @GetMapping("/{id}/project/{projId}/terms")
    public List<TermLang> getTermLangs(@PathVariable("id") ProjectLang projectLang, HttpServletResponse response,
                                       @PathVariable("projId") Project project, Pageable page) throws IOException {
        if (project == null || projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (!project.getProjectLangs().contains(projectLang)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project not contains project lang!");
            return null;
        }
        return termLangRepository.findByProjectLangId(projectLang.getId(), page);
    }

    //СКРЕЕ ВСЕГО НАФИГ
    @GetMapping("/project/{id}")
    public List<ProjectLang> getLangsByProject(@PathVariable("id") Project project) {
        if (project == null) return null;
        return project.getProjectLangs();
    }

    @PutMapping("/{id}/update-lang")
    public ProjectLang updateLang(@PathVariable("id") ProjectLang projectLang, @RequestParam long id,
                                  @AuthenticationPrincipal User user,
                                  HttpServletResponse response) throws IOException {
        if (projectLang == null) {
            response.sendError(404, "Project lang not found");
            return null;
        }
        if (accessDenied(projectLang, user, true)) {
            response.sendError(403, "Access denied");
            return null;
        }
        return projectLangService.updateLang(projectLang, id, response);
    }

    @PostMapping("/{id}/empty")
    public void empty(@PathVariable("id") ProjectLang projectLang, @AuthenticationPrincipal User user,
                      @RequestBody ProjectLang newLang, HttpServletResponse response) throws IOException {
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project lang not found");
            return;
        }
        if (accessDenied(projectLang, user, true)) {
            response.sendError(403, "Access denied");
            return;
        }
        projectLang.setTermLangs(newLang.getTermLangs());
        projectLang.getTermLangs().forEach(a -> {
            if (a.isSelected()) {
                a.setValue("");
                if (bitFlag.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.FUZZY))
                    bitFlag.dropFlag(a, BitFlagService.StatusFlag.FUZZY);
                if (bitFlag.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED))
                    bitFlag.dropFlag(a, BitFlagService.StatusFlag.AUTOTRANSLATED);
                if (bitFlag.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED))
                    bitFlag.dropFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                termLangRepository.save(a);
            }
        });
        projectLangRepository.save(projectLang);
    }

    @PostMapping("/{id}/import-translations")
    public ProjectLang importTranslations(@PathVariable("id") ProjectLang projectLang, MultipartFile file,
                                          @AuthenticationPrincipal User user,
                                          @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page,
                                          HttpServletResponse response) throws IOException {
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project lang not found");
            return null;
        }
        if (accessDenied(projectLang, user, true)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        if (file == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File not chosen, Choose the file!");
            return null;
        }
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        projectLangService.importTranslations(projectLang, convFile);
        projectLang = projectLangRepository.findById((long) projectLang.getId());
        projectLangService.setPagesCount(projectLang);
        projectLangService.setCounts(projectLang);
        projectLang.setTermLangs(termLangRepository.findByProjectLangId(projectLang.getId(), page));
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        projectLang.setProjectName(projectRepository.findById((long) projectLang.getProjectId()).getProjectName());
        return projectLang;
    }

    @GetMapping("/{id}/filter")
    public ProjectLang doFilter(@PathVariable("id") ProjectLang projectLang,
                                @RequestParam(required = false) String term,
                                @RequestParam(required = false) Boolean untranslated,
                                @RequestParam(required = false) Boolean fuzzy,
                                @RequestParam(required = false) String sort_state,
                                @AuthenticationPrincipal User user,
                                @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page,
                                HttpServletResponse response) throws IOException {
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project lang not found!");
            return null;
        }
        if (accessDenied(projectLang, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        return projectLangService.doFilter(projectLang, term, untranslated, fuzzy, sort_state, page.getPageNumber());
    }

    @PostMapping("/{id}/flush-translations")
    public void flushTranslations(@PathVariable("id") ProjectLang projectLang,
                                  @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project lang not found!");
            return;
        }
        if (accessDenied(projectLang, user, true)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return;
        }
        projectLang.getTermLangs().forEach(a -> a.setValue(""));
        projectLangRepository.save(projectLang);
    }

    @GetMapping("/{id}/get-default")
    public ProjectLang getDefaultLang(@PathVariable("id") Project project, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project lang is not found!");
            return null;
        }
        for (ProjectLang projectLang : project.getProjectLangs()) {
            if (projectLang.isDefault()) return projectLang;
        }
        return null;
    }

    @PostMapping("/auto-translate")
    public ProjectLang autoTranslate(@RequestBody ProjectLang projectLang, @RequestParam long from,
                                     HttpServletResponse response,
                                     @AuthenticationPrincipal User user,
                                     @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project lang not found!");
            return null;
        }
        if (projectLang.getId() == from) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You cannot choose this lang!");
            return null;
        }
        if (accessDenied(projectLang, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        ProjectLang lang = projectLangRepository.findById(from);
        String langFrom = lang.getLang().getLangDef().toLowerCase();
        String langTo = projectLang.getLang().getLangDef().toLowerCase();
        lang.getTermLangs().forEach(a -> {
            projectLang.getTermLangs().forEach(b -> {
                if (b.getTerm().getTermValue().equals(a.getTerm().getTermValue()) && b.isSelected() && !a.getValue().equals("")) {
                    try {
                        String res = translator.translate(langFrom, langTo, a.getValue());
                        b.setValue(res);
                        if (bitFlag.isContainsFlag(b.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)) {
                            bitFlag.dropFlag(b, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                            b.getFlags().remove(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.name());
                        }
                        if (!bitFlag.isContainsFlag(b.getStatus(), BitFlagService.StatusFlag.FUZZY)) {
                            bitFlag.addFlag(b, BitFlagService.StatusFlag.FUZZY);
                            b.getFlags().add(BitFlagService.StatusFlag.FUZZY.name());
                        }
                        if (!bitFlag.isContainsFlag(b.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED)) {
                            bitFlag.addFlag(b, BitFlagService.StatusFlag.AUTOTRANSLATED);
                            b.getFlags().add(BitFlagService.StatusFlag.AUTOTRANSLATED.name());
                        }
                        termLangRepository.save(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        });
        ProjectLang stats = projectLangRepository.findById((long) projectLang.getId());
        projectLangService.setCounts(stats);
        projectLang.setTranslatedCount(stats.getTranslatedCount());
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        return projectLang;
    }

    @GetMapping("/{id}/export")
    public void download(@PathVariable("id") ProjectLang projectLang,
                         @AuthenticationPrincipal User user,
                         HttpServletResponse response) throws IOException {
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project lang not found!");
            return;
        }
        if (accessDenied(projectLang, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return;
        }
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

    private boolean accessDenied(ProjectLang projectLang, User user, boolean checkRole) {
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        boolean forbidden = true;
        if (!checkRole) {
            if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId()))
                forbidden = false;
        } else if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId() && a.getRole().name().equals("MODERATOR")))
            forbidden = false;
        if (project.getAuthor().getId() == user.getId()) forbidden = false;
        return forbidden;
    }
}
