package ILocal.service;

import ILocal.entity.*;
import ILocal.repository.*;
import java.io.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectLangService {

    @Autowired
    private ProjectLangRepository projectLangRepository;

    @Autowired
    private TermLangRepository termLangRepository;

    @Autowired
    private BitFlagService bitFlagService;

    @Autowired
    private ParseFile parser;

    public List<TermLang> filter(ProjectLang projectLang, Boolean untranslated, Boolean fuzzy) {
        List<TermLang> termLangs = projectLang.getTermLangs();
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
        return termLangs;
    }

    public void importTranslations(ProjectLang projectLang, File file) throws IOException {
        Map<String, String> translationMap = parser.parseFile(file);
        projectLang.getTermLangs().stream()
                .forEach(a -> {
                    if (translationMap.keySet().contains(a.getTerm().getTermValue())) {
                        a.setValue(translationMap.get(a.getTerm().getTermValue()));
                        a.setModifiedDate(new Date(Calendar.getInstance().getTime().getTime()));
                    }
                });
        projectLangRepository.save(projectLang);
    }

    public List<TermLang> sort(ProjectLang projectLang, String sort_order) {
        List<TermLang> termLangs = projectLang.getTermLangs();
        if (sort_order != null)
            switch (sort_order) {
                case "key_ASC": {
                    termLangs = termLangs.stream()
                            .sorted((a, b) -> a.getTerm().getTermValue().toLowerCase()
                                    .compareTo(b.getTerm().getTermValue().toString()))
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
        return termLangs;
    }
}
