package ILocal.service;

import ILocal.repository.ProjectRepository;
import ILocal.entity.Project;
import ILocal.entity.ProjectLang;
import ILocal.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
public class AccessService {

    @Autowired
    private ProjectRepository projectRepository;

    public boolean accessDenied(Project project, User user, boolean checkRole) {
        boolean isForbidden = true;
        if (!checkRole) {
            if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId()))
                isForbidden = false;
        } else if (project.getContributors().stream().anyMatch(a -> a.getContributor().getId() == user.getId() && a.getRole().name().equals("MODERATOR")))
            isForbidden = false;
        if (project.getAuthor().getId() == user.getId()) isForbidden = false;
        return isForbidden;
    }

    public boolean isNotProjectOrAccessDenied(Project project, User user, HttpServletResponse response, boolean checkRole) throws IOException {
        if (project == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project not found!");
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
            return true;
        }
        if (project.getAuthor().getId()!=user.getId()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return true;
        }
        return false;
    }

    public boolean isNotProjectLangOrAccessDenied(ProjectLang projectLang, User user, HttpServletResponse response, boolean checkRole)throws IOException{
        if (projectLang == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Project lang not found!");
            return true;
        }
        if (accessDenied(projectRepository.findById((long)projectLang.getProjectId()), user, checkRole)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied!");
            return true;
        }
        return false;
    }


}
