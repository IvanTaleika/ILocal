package ILocal.controller;


import ILocal.entity.ResultSearch;
import ILocal.entity.User;
import ILocal.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/border")
    public ResultSearch borderSearch(@AuthenticationPrincipal User user, @RequestParam String searchValue,
                                     @RequestParam Boolean projects, @RequestParam Boolean terms, @RequestParam Boolean translations, @RequestParam int size) {
        if (searchValue.isEmpty()) return new ResultSearch(new ArrayList<>(), 0, 0);
        return searchService.borderTrashSearch(user, searchValue, projects, terms, translations, size);
    }
}
