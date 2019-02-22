package ILocal.controller;

import ILocal.repository.ProjectRepository;
import com.iba.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ExampleController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/projects")
    public List<Project> getAll(){
        return projectRepository.findAll();
    }
}
