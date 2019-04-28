package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.*;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private AccessService accessService;

    private static final Logger logger = Logger.getLogger(ProjectLangController.class);

    @GetMapping("/{id}/project/{projId}")
    public ProjectLang getProjectLang(@PathVariable("id") ProjectLang projectLang, HttpServletResponse response,
                                      @PathVariable("projId") Project project,
                                      @AuthenticationPrincipal User user,
                                      @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to get the project lang");
        if (project == null || projectLang == null) {
            logger.error("Project of project lang not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find project or project lang!");
            return null;
        }
        if (!project.getProjectLangs().contains(projectLang)) {
            logger.error("Project doesn't contain project lang ");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project doesn't contain project lang!");
            return null;
        }
        if (accessService.accessDenied(project, user, false)) {
            logger.error("Access denied");
            logger.error("Access denied");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        projectLangService.setPagesCount(projectLang, page.getPageSize());
        projectLangService.setCounts(projectLang);
        projectLang.setTermLangs(termLangRepository.findByProjectLangId(projectLang.getId(), page));
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        projectLang.setProjectName(project.getProjectName());
        return projectLang;
    }

    @GetMapping("/{id}/pages-count")
    public long getPagesCount(@PathVariable("id") ProjectLang projectLang, HttpServletResponse response, Pageable page) throws IOException {
        if (projectLang == null) {
            response.sendError(404);
            return -1;
        }
        projectLangService.setPagesCount(projectLang, page.getPageSize());
        return projectLang.getPagesCount();
    }


    @GetMapping("/{id}/project/{projId}/terms")
    public List<TermLang> getTermLangs(@PathVariable("id") ProjectLang projectLang, HttpServletResponse response,
                                       @AuthenticationPrincipal User user,
                                       @PathVariable("projId") Project project, Pageable page) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to get the project lang terms on page " + page.getPageNumber());
        if (project == null || projectLang == null) {
            logger.error("Project or project lang not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (!project.getProjectLangs().contains(projectLang)) {
            logger.error("Project doesn't contain project lang");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project does not contains project lang!");
            return null;
        }
        if (accessService.accessDenied(project, user, false)) response.sendError(403);
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
        logger.info("User " + user.getUsername() + " is trying to update project lang");
        if (accessService.isNotProjectLangOrAccessDenied(projectLang, user, response, true)) return null;
        return projectLangService.updateLang(projectLang, id, response);
    }

    @PostMapping("/{id}/empty")
    public ProjectLang empty(@PathVariable("id") ProjectLang projectLang, @AuthenticationPrincipal User user,
                             @RequestBody ProjectLang newLang, HttpServletResponse response) throws IOException {
        logger.info("User "+user.getUsername()+" is trying to empty selected term langs");
        if (accessService.isNotProjectLangOrAccessDenied(projectLang, user, response, true)) return null;
        projectLang.setTermLangs(newLang.getTermLangs());
        projectLang.getTermLangs().forEach(a -> {
            if (a.isSelected() && !a.getValue().equals("")) {
                a.setValue("");
                a.setModifiedDate();
                a.setModifier(user);
                if (bitFlag.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.FUZZY))
                    bitFlag.dropFlag(a, BitFlagService.StatusFlag.FUZZY);
                if (bitFlag.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED))
                    bitFlag.dropFlag(a, BitFlagService.StatusFlag.AUTOTRANSLATED);
                if (bitFlag.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED))
                    bitFlag.dropFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
            }
        });
        termLangRepository.saveAll(projectLang.getTermLangs());
        projectLangRepository.save(projectLang);
        logger.info("User "+user.getUsername()+" made selected term langs empty");
        return projectLang;
    }

    @PostMapping("/{id}/import-translations")
    public ProjectLang importTranslations(@PathVariable("id") ProjectLang projectLang, MultipartFile file,
                                          @AuthenticationPrincipal User user,
                                          @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page,
                                          HttpServletResponse response) throws IOException, JSONException {
        logger.info("User "+user.getUsername()+" is trying to import translations");
        if (accessService.isNotProjectLangOrAccessDenied(projectLang, user, response, false)) return null;
        if (file == null) {
            logger.error("File is null");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File not chosen, Choose the file!");
            return null;
        }
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        projectLangService.importTranslations(projectLang, convFile);
        projectLang = projectLangRepository.findById((long) projectLang.getId());
        projectLangService.setPagesCount(projectLang, page.getPageSize());
        projectLangService.setCounts(projectLang);
        projectLang.setTermLangs(termLangRepository.findByProjectLangId(projectLang.getId(), page));
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        projectLang.setProjectName(projectRepository.findById((long) projectLang.getProjectId()).getProjectName());
        logger.info("User "+user.getUsername()+" imported translations");
        return projectLang;
    }

    @GetMapping("/{id}/filter")
    public ProjectLang doFilter(@PathVariable("id") ProjectLang projectLang,
                                @RequestParam(required = false) String term,
                                @RequestParam(required = false) Boolean untranslated,
                                @RequestParam(required = false) Boolean fuzzy,
                                @RequestParam(required = false) Boolean def_edited,
                                @RequestParam(required = false) String sort_state,
                                @AuthenticationPrincipal User user,
                                @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page,
                                HttpServletResponse response) throws IOException {
        logger.info("User "+user.getUsername()+" is trying to filter the project lang");
        if (accessService.isNotProjectLangOrAccessDenied(projectLang, user, response, false)) return null;
        return projectLangService.doFilter(projectLang, term, untranslated, fuzzy, def_edited, sort_state, page.getPageNumber(), page.getPageSize());
    }

    @PostMapping("/{id}/flush-translations")
    public void flushTranslations(@PathVariable("id") ProjectLang projectLang,
                                  @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        if (accessService.isNotProjectLangOrAccessDenied(projectLang, user, response, true)) return;
        projectLang.getTermLangs().forEach(a -> a.setValue(""));
        projectLangRepository.save(projectLang);
    }

    @GetMapping("/{id}/get-default")
    public ProjectLang getDefaultLang(@PathVariable("id") Project project, HttpServletResponse response) throws IOException {
        logger.info("User  is trying to get the default project lang");
        if (project == null) {
            logger.error("Project not found");
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
        logger.info("User "+user.getUsername()+" is trying to auto translate term langs");
        if (accessService.isNotProjectLangOrAccessDenied(projectLang, user, response, false)) return null;
        if (projectLang.getId() == from) {
            logger.error("User choose incorrect lang");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You cannot choose this lang!");
            return null;
        }
        ProjectLang lang = projectLangRepository.findById(from);
        String langFrom = lang.getLang().getLangDef().toLowerCase();
        String langTo = projectLang.getLang().getLangDef().toLowerCase();
        List<ProjectLang> projectLangsBuffer = new ArrayList<>();
        if(projectLang.isDefault()){
            projectLangsBuffer = projectLangRepository.findByProjectId(projectLang.getProjectId());
            projectLangsBuffer.remove(projectLang);
        }
        final List<ProjectLang> projectLangs = new ArrayList<>(projectLangsBuffer);
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
                        if(projectLang.isDefault()){
                            projectLangs.forEach(c->{
                                c.getTermLangs().forEach(d-> {
                                    if(d.getTerm().getTermValue().equals(b.getTerm().getTermValue()) && !d.getValue().equals("")){
                                        if (!bitFlag.isContainsFlag(d.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)) {
                                            bitFlag.addFlag(d, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                                            d.getFlags().add(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.name());
                                        }
                                    }
                                });
                            });
                        }
                        b.setModifier(user);
                        b.setModifiedDate();
                        //termLangRepository.save(b);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        });
        termLangRepository.saveAll(projectLang.getTermLangs());
        ProjectLang stats = projectLangRepository.findById((long) projectLang.getId());
        projectLangService.setCounts(stats);
        projectLang.setTranslatedCount(stats.getTranslatedCount());
        projectLang.setTermLangs(projectLangService.setFlags(projectLang.getTermLangs()));
        logger.info("User "+user.getUsername()+" translated term langs");
        return projectLang;
    }

    @GetMapping("/{id}/export")
    public void download(@PathVariable("id") ProjectLang projectLang,
                         @AuthenticationPrincipal User user,
                         @RequestParam String type,
                         HttpServletResponse response) throws IOException {
        logger.info("User "+user.getUsername()+" is trying to export the project lang");
        if (accessService.isNotProjectLangOrAccessDenied(projectLang, user, response, false)) return;
        File file;
        switch (type){
            case "json": file = projectLangService.createJSONFile(projectLang); break;
            case "properties": file = projectLangService.createPropertiesFile(projectLang); break;
            default: return;
        }

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
        logger.info("User "+user.getUsername()+" exported the project lang");
    }

}
