package ILocal.service;

import ILocal.entity.Project;
import ILocal.entity.ProjectLang;
import ILocal.entity.User;
import ILocal.repository.ProjectRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class AccessService {

    private static final Logger logger = org.apache.log4j.Logger.getLogger(AccessService.class);

    @Autowired
    private ProjectRepository projectRepository;

    public boolean accessDenied(Project project, User user, boolean checkRole) {
        if (!checkRole) {
            if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId()))
                return false;
        } else if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId() && a.getRole().name().equals("MODERATOR")))
            return false;
        if (project.getAuthor().getId() == user.getId()) return false;
        logger.error("Access denied, User " + user.getUsername() + " is not an author or contributor to project " + project.getProjectName());
        return true;
    }

    public boolean isNotProjectOrAccessDenied(Project project, User user, HttpServletResponse response, boolean checkRole) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            logger.error("Project not found");
            return true;
        }
        if (accessDenied(project, user, checkRole)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return true;
        }
        return false;
    }

    public boolean isNotProjectOrNotAuthor(Project project, User user, HttpServletResponse response) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
            logger.error("Project not found");
            return true;
        }
        if (project.getAuthor().getId() != user.getId()) {
            logger.error("User " + user.getUsername() + " is not an author to project " + project.getProjectName());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return true;
        }
        return false;
    }

    public boolean isNotProjectLangOrAccessDenied(ProjectLang projectLang, User user, HttpServletResponse response, boolean checkRole) throws IOException {
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project lang not found!");
            logger.error("Project lang not found");
            return true;
        }
        if (accessDenied(projectRepository.findById((long) projectLang.getProjectId()), user, checkRole)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return true;
        }
        return false;
    }

}
