package ILocal.controller;


import ILocal.entity.*;
import ILocal.repository.*;
import ILocal.service.ParseFile;
import ILocal.service.ProjectService;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/projects")
public class ProjectController {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ProjectLangRepository projectLangRepository;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectContributorRepository contributorRepository;

	@Autowired
	private TermRepository termRepository;

	@Autowired
	private ParseFile parser;

	@GetMapping
	public List<Project> getAll() {
		return projectRepository.findAll();
	}

	@GetMapping("/{id}")
	public Project getProject(@PathVariable("id") Project project) {
		return project;
	}

	@PutMapping("/{id}/update")
	public Project updateProject(@PathVariable("id") Project project, @RequestParam(required = false) String newName,
								 @RequestParam(required = false) String newDescription) {
		if (newName != null && !newName.equals(""))
			project.setProjectName(newName);
		if (newDescription != null && !newDescription.equals(""))
			project.setDescription(newDescription);
		projectRepository.save(project);
		return project;
	}

	@DeleteMapping("/delete")
	public void deleteProject(@RequestParam long id) {
		Project project = projectRepository.findById(id);
		if (project != null)
			projectRepository.delete(project);
	}

	@PostMapping("/add")
	public Project addProject(@RequestBody Project project, @RequestParam long author_id, @RequestParam long lang_id) {
		return projectService.addProject(project, author_id, lang_id);
	}

	@PostMapping("/{id}/language/add")
	public ProjectLang addProjectLang(@PathVariable("id") Project project, @RequestParam long lang_id) {
		if(lang_id == -1) return null;
		return projectService.addProjectLang(project, lang_id);
	}

	@PostMapping("/language/delete")
	public boolean deleteProjectLang(@RequestBody long id) {
		if (projectLangRepository.findById(id).isDefault()) return false;
		if (projectLangRepository.findById(id) != null)
			projectLangRepository.deleteById(id);
		return true;
	}

	@PostMapping("/{id}/add/contributor")
	public ProjectContributor addContributor(@PathVariable("id") Project project, @RequestBody User newUser, @RequestParam String role) {
		return projectService.addContributor(project, newUser, role);
	}

	@PostMapping("/delete/contributor")
	public boolean deleteContributor(@RequestBody long id) {
		if (contributorRepository.findById(id) == null) return false;
		contributorRepository.deleteById(id);
		return true;
	}

	@PostMapping("/{id}/add/term")
	public Term addTermToProject(@PathVariable("id") Project project, @RequestBody String term) {
		return projectService.addTerm(project, term);
	}

	@PostMapping("/{id}/delete/term")
	public boolean deleteTerm(@PathVariable("id") Project project, @RequestBody long term_id) {
		Term term = termRepository.findById(term_id);
		if (term == null || project == null) return false;
		if (!project.getTerms().contains(term)) return false;
		project.getTerms().remove(term);
		termRepository.deleteById(term_id);
		projectService.deleteTermFromProject(project, term_id);
		return true;
	}

	@DeleteMapping("/flush")
	public void flush(@RequestParam long id) {
		Project project = projectRepository.findById(id);
		if (project == null) return;
		projectService.flush(project);
	}

	@GetMapping("/{userId}/projects")
	public List<Project> getUserProjects(@PathVariable("userId") User user) {
		return projectRepository.findByAuthor(user);
	}

	@GetMapping("/{userId}/contributions")
	public List<Project> getUserContributions(@PathVariable("userId") User user) {
		List<Project> projects = projectRepository.findAll();
		return projects.stream()
				.filter(a -> a.getContributors().stream().anyMatch(b -> b.getContributor().getId() == user.getId()))
				.collect(Collectors.toList());
	}

	@PutMapping("/{id}/update/default")
	public List<ProjectLang> updateDefaultLang(@PathVariable("id") Project project, @RequestParam(required = false) String lang_name) {
		project.getProjectLangs().stream().forEach(a -> a.setDefault(false));
		for (ProjectLang projectLang : project.getProjectLangs()) {
			if (projectLang.getLang().getLangName().equals(lang_name)) {
				projectLang.setDefault(true);
				projectLangRepository.save(projectLang);
			}
		}
		return project.getProjectLangs();
	}


	@PostMapping("/{id}/import-terms")
	public void importTerms(@PathVariable("id") Project project, MultipartFile file,
							@RequestParam boolean import_values,
                            @RequestParam(required = false) Long projLangId) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		projectService.importTerms(project, convFile, import_values, projLangId);
	}

	@GetMapping("/{id}/sort")
	public List<ProjectLang> sortProjectLangs(@PathVariable("id") Project project, @RequestParam(required = false) String sort_state) {
		return projectService.sort(project.getProjectLangs(), sort_state);
	}

	@GetMapping("/filter")
	public List<Project> doFilter(@RequestParam(required = false) String name,
								  @RequestParam(required = false) String term,
								  @RequestParam(required = false) String contributorName,
								  @RequestParam(required = false) String sort_state) {
		return projectService.doFilter(term, contributorName, name, sort_state);
	}

	@PostMapping("/{id}/notify")
	public void notify(@PathVariable("id") Project project, @RequestBody String message){
		projectService.notifyContributors(project, message);
	}

}
