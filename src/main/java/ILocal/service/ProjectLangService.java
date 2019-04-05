package ILocal.service;


import ILocal.entity.ProjectLang;
import ILocal.entity.TermLang;
import ILocal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
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
    private ParseFile parser;

    public List<TermLang> filter(List<TermLang> termLangs, Boolean untranslated, Boolean fuzzy) {
        if (untranslated != null && untranslated) {
            termLangs = termLangs.stream().filter(a -> a.getValue().equals("")).collect(Collectors.toList());
        } else if (untranslated != null) {
            termLangs = termLangs.stream().filter(a -> !a.getValue().equals("")).collect(Collectors.toList());
        }

        if (fuzzy != null && fuzzy) {
            termLangs = termLangs.stream().filter(a -> {
                EnumSet<BitFlagService.StatusFlag> enumSet = bitFlagService.getStatusFlags(a.getStatus());
                return enumSet.contains(BitFlagService.StatusFlag.FUZZY);
            }).collect(Collectors.toList());
        } else if (fuzzy != null) {
            termLangs = termLangs.stream().filter(a -> {
                EnumSet<BitFlagService.StatusFlag> enumSet = bitFlagService.getStatusFlags(a.getStatus());
                return !enumSet.contains(BitFlagService.StatusFlag.FUZZY);
            }).collect(Collectors.toList());
        }
        return setFlags(termLangs);
    }

    public List<TermLang> importTranslations(ProjectLang projectLang, File file) throws IOException {
        Map<String, String> translationMap = parser.parseFile(file);
        projectLang.getTermLangs().stream()
                .forEach(a -> {
                    if (translationMap.keySet().contains(a.getTerm().getTermValue())) {
                        a.setValue(translationMap.get(a.getTerm().getTermValue()));
                        a.setModifiedDate(new Date(Calendar.getInstance().getTime().getTime()));
                        EnumSet<BitFlagService.StatusFlag> enumSet = bitFlagService.getStatusFlags(a.getStatus());
                        if(enumSet.contains(BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED)){
                            a.setStatus(a.getStatus()-BitFlagService.StatusFlag.DEFAULT_WAS_CHANGED.getValue());
                        }
                    }
                });
        projectLangRepository.save(projectLang);
        return projectLang.getTermLangs();
    }

    public List<TermLang> sort(List<TermLang> termLangs, String sort_state) {
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

    public List<TermLang> search(List<TermLang> termLangs, String term){
        if(term == null) return termLangs;
        return termLangs.stream()
                .filter(a -> a.getTerm().getTermValue().toLowerCase().contains(term.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<TermLang> doFilter(ProjectLang projectLang, String term,
                                   Boolean untranslated, Boolean fuzzy, String order_state){
    List<TermLang> langs = projectLang.getTermLangs();
    langs = search(langs, term);
    langs = filter(langs, untranslated, fuzzy);
    return sort(langs, order_state);
    }

    public List<TermLang> setFlags(List<TermLang> terms){
        terms.forEach(a -> {
                    EnumSet<BitFlagService.StatusFlag> flags = bitFlagService.getStatusFlags(a.getStatus());
                    List<String> list = new ArrayList<>();
                    flags.forEach(b -> list.add(b.toString()));
                    a.setFlags(list);
                });
        return terms;

    }

    public File createPropertiesFile(ProjectLang projectLang) throws IOException {
        Project project = projectRepository.findById((long)projectLang.getProjectId());
        File file =  new File(project.getProjectName()+"_"+projectLang.getLang().getLangDef()+".properties");
        PrintWriter pw2 = new PrintWriter(file, "windows-1251");
        projectLang.getTermLangs().forEach(term ->{
            pw2.write(term.getTerm().getTermValue()+"="+term.getValue());
            pw2.write("\n");
        });
        pw2.close();
        return file;
    }

    public ProjectLang updateLang(ProjectLang projectLang, long id, HttpServletResponse response) throws IOException {
        Lang lang = langRepository.findById(id);
        if(lang == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Lang not found!");
            return null;
        }
        Project project = projectRepository.findById((long)projectLang.getProjectId());
        if(project == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            return null;
        }
        for (ProjectLang a : project.getProjectLangs()) {
            if(a.getLang().getId() == id) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project lang exists in this project!");
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
}
