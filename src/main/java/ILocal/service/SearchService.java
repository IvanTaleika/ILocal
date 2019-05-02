package ILocal.service;


import ILocal.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final static String projectUrl = "/projects/";
    private final static String langUrl = "/langs/";
    private final static String termUrl = "/terms";

    @Autowired
    private ProjectService projectService;


    public ResultSearch borderTrashSearch(@AuthenticationPrincipal User user, @RequestParam String searchValue, boolean findProjects, boolean findTerms, boolean findTranslations, int itemsCount) {
        List<SearchItem> searchItemList = getResultSearch(user, searchValue, findProjects, findTerms, findTranslations);
        int size = searchItemList.size();
        if (searchItemList.size() > itemsCount)
            searchItemList = searchItemList.subList(0, itemsCount);
        return new ResultSearch(searchItemList, size);
    }

    public List<SearchItem> getResultSearch(@AuthenticationPrincipal User user, @RequestParam String searchValue, boolean findProjects, boolean findTerms, boolean findTranslations) {
        List<SearchItem> searchItemList = new ArrayList<>();
        List<Project> projectList = projectService.getAllUserProjects(user);
        projectList.forEach(a -> {
            if (a.getProjectName().toLowerCase().contains(searchValue.toLowerCase()) && findProjects) {
                SearchItem searchItem = new SearchItem(a.getProjectName(), Category.PROJECT.getValue());
                searchItem.setProject(a.getProjectName());
                searchItem.setDescription(a.getDescription());
                searchItem.setLink(projectUrl + a.getId());
                searchItemList.add(searchItem);
            }
            if(findTerms)
            a.getTerms().forEach(d -> {
                if (d.getTermValue().toLowerCase().contains(searchValue.toLowerCase())) {
                    SearchItem searchItem = new SearchItem(d.getTermValue(), Category.TERM.getValue());
                    searchItem.setProject(a.getProjectName());
                    searchItem.setSearchedValue(searchValue);
                    searchItem.setLink(projectUrl + a.getId() + termUrl);
                    searchItemList.add(searchItem);
                }
            });
            if(findTranslations)
            a.getProjectLangs().forEach(b -> {
                b.getTermLangs().forEach(c -> {
                    if (c.getValue().toLowerCase().contains(searchValue.toLowerCase())) {
                        SearchItem searchItem = new SearchItem(c.getValue(), Category.TERMVALUE.getValue());
                        searchItem.setProject(a.getProjectName());
                        searchItem.setLang(c.getLang().getLangName());
                        searchItem.setTerm(c.getTerm().getTermValue());
                        searchItem.setSearchedValue(searchValue);
                        searchItem.setLink(projectUrl + a.getId() + langUrl + b.getId());
                        searchItemList.add(searchItem);

                    }
                });
            });
        });
        return searchItemList;
    }
}
