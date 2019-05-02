package ILocal.controller;


import ILocal.entity.*;
import ILocal.entity.UI.View;
import ILocal.repository.ProjectRepository;
import ILocal.repository.TermCommentRepository;
import ILocal.repository.TermLangRepository;
import ILocal.repository.TermRepository;
import ILocal.service.AccessService;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/terms")
public class TermController {

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TermLangRepository termLangRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AccessService accessService;

    @Autowired
    private TermCommentRepository termCommentRepository;

    private static final Logger logger = org.apache.log4j.Logger.getLogger(TermController.class);

//    @GetMapping
//    public List<Term> getAll() {
//        return termRepository.findAll();
//    }

    @GetMapping("/project/{id}")
    public Project getProjectTerms(@PathVariable("id") Project project, HttpServletResponse response,
                                   @AuthenticationPrincipal User user,
                                   @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to get project terms");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, false)) return null;
        project.setContributors(null);
        project.setProjectLangs(null);
        setPagesCount(project, page.getPageSize());
        project.setTermsCount(project.getTerms().size());
        project.setTerms(termRepository.findByProjectId(project.getId(), page));
        logger.info("User " + user.getUsername() + " got project terms");
        return project;
    }

    private void setPagesCount(Project project, int size) {
        int tail = 0;
        if (project.getTerms().size() % size != 0) tail += 1;
        project.setPagesCount(project.getTerms().size() / size + tail);
    }

    @GetMapping("/{id}")
    public Term getTerm(@PathVariable("id") Term term, HttpServletResponse response) throws IOException {
        if (term == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return null;
        }
        return term;
    }

    @PutMapping("/{id}/update")
    public Term updateTerm(@PathVariable("id") Term term, @RequestBody String newValue,
                           @AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to update project term");
        if (term == null) {
            logger.error("Term not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not  found!");
            return null;
        }
        Project project = projectRepository.findById((long) term.getProjectId());
        if (accessService.accessDenied(project, user, true)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return null;
        }
        for (Term t : project.getTerms()) {
            if (t.getTermValue().equals(newValue)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }
        }

        term.setTermValue(newValue);
        termRepository.save(term);
        logger.info("User " + user.getUsername() + " updated project terms");
        return term;
    }

    @GetMapping("/{projectId}/filter")
    public Project filter(@PathVariable("projectId") Project project,
                          @RequestParam(required = false) String sort,
                          @RequestParam(required = false) String value,
                          @AuthenticationPrincipal User user,
                          @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable page,
                          HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to filter project terms");
        if (accessService.isNotProjectOrAccessDenied(project, user, response, false)) return null;
        project.setTermsCount(project.getTerms().size());
        List<Term> terms = project.getTerms();
        if (value != null) {
            terms = terms.stream()
                    .filter(a -> a.getTermValue().toLowerCase().contains(value.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (sort != null)
            switch (sort) {
                case "ASC": {
                    terms = terms.stream().sorted((a, b) -> a.getTermValue().toLowerCase().compareTo(b.getTermValue().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                }
                case "DESC": {
                    terms = terms.stream().sorted((b, a) -> a.getTermValue().toLowerCase().compareTo(b.getTermValue().toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                }
            }
        int currentPage = page.getPageNumber();
        project.setSearchedTermsCount(terms.size());
        project.setTerms(terms);
        setPagesCount(project, page.getPageSize());
        if (!terms.isEmpty()) {
            int maxPage = terms.size() / page.getPageSize() - 1;
            if (terms.size() % page.getPageSize() != 0) maxPage += 1;
            if (currentPage > maxPage) currentPage = maxPage;
            int last = 0;
            if (currentPage == maxPage) last = terms.size();
            else last = (currentPage + 1) * page.getPageSize();
            terms = terms.subList(currentPage * page.getPageSize(), last);
            project.setTerms(terms);
        }
        project.setProjectLangs(null);
        project.getTerms().forEach(a-> a.setCommentsCount(termCommentRepository.countAllByTermId(a.getId())));
        logger.info("User " + user.getUsername() + " filtered project terms");
        return project;
    }

//    private void setTermPagesCount(Project project, int size) {
//        int tail = 0;
//        if (project.getTerms().size() % size != 0) tail += 1;
//        project.setPagesCount(project.getTerms().size() / size + tail);
//    }


    @GetMapping("/{id}/translations")
    @JsonView(View.ProjectItem.class)
    public List<TermLang> getTranslations(@PathVariable("id") Term term, @AuthenticationPrincipal User user,
                                          HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to get project term translations");
        if (term == null) {
            logger.error("Term not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found!");
            return null;
        }
        Project project = projectRepository.findById((long) term.getProjectId());
        if (accessService.accessDenied(project, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return null;
        }
        logger.info("User " + user.getUsername() + " got project terms");
        return termLangRepository.findByTerm(term);
    }

    @GetMapping("/{id}/get-comments")
    @JsonView(View.ProjectItem.class)
    public List<TermComment> getComments(@PathVariable("id") Term term, @AuthenticationPrincipal User user,
                                         HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to get project term comments");
        if (term == null) {
            logger.error("Term not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found");
            return null;
        }
        Project project = projectRepository.findById((long) term.getProjectId());
        if (accessService.accessDenied(project, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return null;
        }
        return termCommentRepository.findAllByTermId(term.getId());
    }

    @PostMapping("/{id}/add-comment")
    @JsonView(View.ProjectItem.class)
    public TermComment addComment(@PathVariable("id") Term term, @AuthenticationPrincipal User user,
                                  HttpServletResponse response, @RequestBody String text) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to add project term comment");
        if (term == null) {
            logger.error("Term not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found ");
            return null;
        }
        Project project = projectRepository.findById((long) term.getProjectId());
        if (accessService.accessDenied(project, user, false)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied! ");
            return null;
        }
        TermComment termComment = new TermComment();
        termComment.setAuthor(user);
        termComment.setCreationDate();
        termComment.setTermId(term.getId());
        termComment.setText(text);
        termCommentRepository.save(termComment);
        return termComment;
    }

    @PostMapping("/delete-comment")
    public void deleteComment(@RequestBody TermComment comment, @AuthenticationPrincipal User user,
                              HttpServletResponse response) throws IOException {
        logger.info("User " + user.getUsername() + " is trying to delete project term comment");
        if (comment == null) {
            logger.error("Comment not found ");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Comment not found ");
            return;
        }
        Term term = termRepository.findById((long) comment.getTermId());
        if (term == null) {
            logger.error("Term not found");
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Term not found ");
            return;
        }
        long authId = projectRepository.getAuthorIdByProjectId(term.getProjectId());
        if (authId != user.getId() && comment.getAuthor().getId() != user.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!  ");
            return;
        }
        termCommentRepository.delete(comment);
    }
}
