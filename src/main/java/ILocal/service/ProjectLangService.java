package ILocal.service;


import ILocal.entity.*;
import ILocal.repository.LangRepository;
import ILocal.repository.ProjectLangRepository;
import ILocal.repository.ProjectRepository;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectLangService {

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private LangRepository langRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BitFlagService bitFlagService;

    @Autowired
    private EncodeChanger encodeChanger;

    @Autowired
    private ParseFile parser;

    @Autowired
    private StatService statService;

    @Value("${file.load.path}")
    private String filePath;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(ProjectLangService.class);

    public List<TermLang> filter(List<TermLang> termLangs, Boolean untranslated, Boolean fuzzy, Boolean edited) {
        logger.info("Filter term lang list");
        if (untranslated != null && untranslated) {
            termLangs = termLangs.stream().filter(a -> a.getValue().equals("")).collect(Collectors.toList());
        } else if (untranslated != null) {
            termLangs = termLangs.stream().filter(a -> !a.getValue().equals("")).collect(Collectors.toList());
        }

        if (fuzzy != null && fuzzy) {
            termLangs = termLangs.stream().filter(a -> bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.FUZZY)).collect(Collectors.toList());
        } else if (fuzzy != null) {
            termLangs = termLangs.stream().filter(a -> !bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.FUZZY)).collect(Collectors.toList());
        }
        if (edited != null && edited) {
            termLangs = termLangs.stream().filter(a -> bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)).collect(Collectors.toList());
        }
        return setFlags(termLangs);
    }

    public List<TermLang> sort(List<TermLang> termLangs, String sort_state) {
        logger.info("Sorting term lang list");
        if (sort_state != null)
            switch (sort_state) {
                case "key_ASC": {
                    termLangs = termLangs.stream()
                            .sorted((a, b) -> a.getTerm().getTermValue().toLowerCase()
                                    .compareTo(b.getTerm().getTermValue()))
                            .collect(Collectors.toList());
                    break;
                }
                case "key_DESC": {
                    termLangs = termLangs.stream()
                            .sorted((b, a) -> a.getTerm().getTermValue().toLowerCase()
                                    .compareTo(b.getTerm().getTermValue().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                }
                case "date_ASC": {
                    termLangs = termLangs.stream()
                            .sorted((a, b) -> a.getModifiedDate().compareTo(b.getModifiedDate()))
                            .collect(Collectors.toList());
                    break;
                }
                case "date_DESC": {
                    termLangs = termLangs.stream()
                            .sorted((b, a) -> a.getModifiedDate().compareTo(b.getModifiedDate()))
                            .collect(Collectors.toList());
                    break;
                }
            }
        return setFlags(termLangs);
    }

    public List<TermLang> search(List<TermLang> termLangs, String searchParam, String searchState, Long reference) {
        logger.info("Searching term lang");
        if (searchParam == null) return termLangs;
        else {
            switch (searchState) {
                case "KEY": {
                    return termLangs.stream()
                            .filter(a -> a.getTerm().getTermValue().toLowerCase().contains(searchParam.toLowerCase()))
                            .collect(Collectors.toList());
                }
                case "REFERENCE": {
                    if (searchParam.equals("")) return termLangs;
                    if (reference == null || reference == -1) return new ArrayList<>();
                    else {
                        ProjectLang projectLang = projectLangRepository.findById((long) reference);
                        return termLangs.stream()
                                .filter(a -> projectLang.getTermLangs().stream()
                                        .anyMatch(b -> a.getTerm().getTermValue().equals(b.getTerm().getTermValue()) && b.getValue().toLowerCase().contains(searchParam.toLowerCase())))
                                .collect(Collectors.toList());
                    }
                }
                case "TERMLANG": {
                    return termLangs.stream()
                            .filter(a -> a.getValue().toLowerCase().contains(searchParam.toLowerCase()))
                            .collect(Collectors.toList());
                }
                case "MODIFIER": {
                    return termLangs.stream()
                            .filter(a -> a.getModifier() != null && (a.getModifier().getFirstName().toLowerCase().contains(searchParam.toLowerCase())
                                    || a.getModifier().getLastName().toLowerCase().contains(searchParam.toLowerCase())))
                            .collect(Collectors.toList());
                }
                default: {
                    return termLangs;
                }
            }
        }
    }

    public ProjectLang doFilter(ProjectLang projectLang, String searchParam, String searchState,
                                Boolean untranslated, Boolean fuzzy, Boolean edited, String order_state, int page, int size, Long reference) {
        logger.info("Main filter");
        List<TermLang> langs = projectLang.getTermLangs();
        setCounts(projectLang);
        langs = search(langs, searchParam, searchState, reference);
        langs = filter(langs, untranslated, fuzzy, edited);
        langs = sort(langs, order_state);
        projectLang.setTermLangs(langs);
        setPagesCount(projectLang, size);
        if (!langs.isEmpty()) {
            int maxPage = langs.size() / size - 1;
            if (langs.size() % size != 0) maxPage += 1;
            if (page > maxPage) page = maxPage;
            int last = 0;
            if (page == maxPage) last = langs.size();
            else last = (page + 1) * size;
            langs = langs.subList(page * size, last);
            setFlags(langs);
            projectLang.setTermLangs(langs);
        }
        projectLang.setProjectName(projectRepository.findById((long) projectLang.getProjectId()).getProjectName());
        return projectLang;
    }

    public void importTranslations(ProjectLang projectLang, File file, User user) throws IOException, JSONException {
        logger.info("Importing translations");
        Map<String, String> translationMap = parser.parseFile(file);
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        project.getProjectLangs().remove(projectLang);
        List<StatType> typeList = new ArrayList<>();
        projectLang.getTermLangs().stream()
                .forEach(a -> {
                    if (translationMap.keySet().contains(a.getTerm().getTermValue()) && !a.getValue().equals(translationMap.get(a.getTerm().getTermValue()))) {
                        if (a.getValue().equals("")) typeList.add(StatType.TRANSLATE_BY_IMPORT);
                        else typeList.add(StatType.EDIT_BY_IMPORT);
                        a.setValue(translationMap.get(a.getTerm().getTermValue()));
                        a.setModifiedDate();
                        a.setModifier(user);
                        if (bitFlagService.isContainsFlag(a.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)) {
                            bitFlagService.dropFlag(a, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                        }
                        if (projectLang.isDefault()) {
                            project.getProjectLangs().forEach(b -> {
                                b.getTermLangs().forEach(c -> {
                                    if (c.getTerm().getTermValue().equals(a.getTerm().getTermValue()) && !c.getValue().equals("")) {
                                        if (!bitFlagService.isContainsFlag(c.getStatus(), BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)) {
                                            bitFlagService.addFlag(c, BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED);
                                        }
                                    }
                                });
                            });
                        }
                    }
                });
        projectLangRepository.save(projectLang);
        statService.createStats(typeList, user.getId(), project.getId());
    }

    public List<TermLang> setFlags(List<TermLang> terms) {
        logger.info("Settings flags");
        terms.forEach(a -> {
            setFlagsToTerm(a);
        });
        return terms;
    }

    public void setFlagsToTerm(TermLang term) {
        EnumSet<BitFlagService.StatusFlag> flags = bitFlagService.getStatusFlags(term.getStatus());
        List<String> list = new ArrayList<>();
        flags.forEach(b -> list.add(b.toString()));
        term.setFlags(list);
    }

    public File createPropertiesFile(ProjectLang projectLang, boolean unicode) throws IOException {
        logger.info("Creating properties file");
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        new File(filePath).mkdirs();
        File file = new File(filePath + project.getProjectName() + "_" + projectLang.getLang().getLangDef() + ".properties");
        PrintWriter pw2 = new PrintWriter(file, "UTF-8");

        if (unicode) {
            projectLang.getTermLangs().forEach(term -> {
                pw2.write(term.getTerm().getTermValue().replace("\n", "") + "=" + encodeChanger.unicode2UnicodeEsc(term.getValue()));
                pw2.write("\n");
            });
        } else {
            projectLang.getTermLangs().forEach(term -> {
                pw2.write(term.getTerm().getTermValue() + "=" + term.getValue());
                pw2.write("\n");
            });
        }
        pw2.close();
        return file;
    }

    public File createJSONFile(ProjectLang projectLang) throws IOException {
        logger.info("Creating JSON file");
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        new File(filePath).mkdirs();
        File file = new File(filePath + project.getProjectName() + "_" + projectLang.getLang().getLangDef() + ".json");
        PrintWriter pw2 = new PrintWriter(file, "UTF-8");
        pw2.write("{ \n");
        Iterator<TermLang> iterator = projectLang.getTermLangs().iterator();
        while (iterator.hasNext()) {
            TermLang term = iterator.next();
            pw2.write("    \"" + format(term.getTerm().getTermValue()) + "\"" + " : " + "\"" + format(term.getValue()) + "\"");
            if (iterator.hasNext()) pw2.write(", \n");
        }
        pw2.write(" \n}");
        pw2.close();
        return file;
    }

    private String format(String value){
        value = value.trim().replace("\n", "");
        if(value.length()!=0 && value.charAt(0) == '"') value = value.substring(1);
        if(value.length()!=0 && value.charAt(value.length()-1) == '"') value = value.substring(0, value.length()-1);
        return value.replace("\"", "\\\"");
    }

    public ProjectLang updateLang(ProjectLang projectLang, long id, User user, HttpServletResponse response) throws IOException {
        Lang lang = langRepository.findById(id);
        if (lang == null) {
            logger.error("New lang value is incorrect");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Lang not found!");
            return null;
        }
        Project project = projectRepository.findById((long) projectLang.getProjectId());
        if (project == null) {
            logger.info("Project not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        for (ProjectLang a : project.getProjectLangs()) {
            if (a.getLang().getId() == id) {
                logger.error("Project lang is exists");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project lang exists in this project!");
                return null;
            }
        }
        projectLang.setLang(lang);
        projectLang.getTermLangs().forEach(a -> {
            a.setValue("");
            a.setLang(lang);
            a.setModifiedDate();
            a.setModifier(user);
        });
        projectLangRepository.save(projectLang);
        int translatedCount = 0;
        for (TermLang b : projectLang.getTermLangs()) {
            if (!b.getValue().equals("")) translatedCount++;
        }
        projectLang.setTranslatedCount(translatedCount);
        projectLang.setTermsCount(projectLang.getTermLangs().size());
        projectLang.setTermLangs(null);
        return projectLang;
    }

    public void setCounts(ProjectLang projectLang) {
        projectLang.setTermsCount(projectLang.getTermLangs().size());
        long translatedCount = 0;
        for (TermLang termLang : projectLang.getTermLangs()) {
            if (!termLang.getValue().equals("")) translatedCount++;
        }
        projectLang.setTranslatedCount(translatedCount);
    }

    public void setPagesCount(ProjectLang projectLang, int size) {
        int tail = 0;
        if (projectLang.getTermLangs().size() % size != 0) tail += 1;
        projectLang.setPagesCount(projectLang.getTermLangs().size() / size + tail);
    }
}
