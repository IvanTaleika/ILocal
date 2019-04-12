package ILocal.controller;

import ILocal.entity.Lang;
import ILocal.repository.LangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/lang")
public class LangController {

    @Autowired
    private LangRepository langRepository;

    @GetMapping
    public List<Lang> getAll() {
        return langRepository.findAll();
    }

    @PutMapping("/{id}/update")
    public void updateLang(@PathVariable("id") Lang lang, @RequestBody Lang newLang) {
        lang.setLangDef(newLang.getLangDef());
        lang.setLangIcon(newLang.getLangIcon());
        lang.setLangName(newLang.getLangName());
        langRepository.save(lang);
    }

    @DeleteMapping("/{id}/delete")
    public void deleteLang(@PathVariable("id") long id) {
        langRepository.deleteById(id);
    }

    @PostMapping("/add")
    public void add(@RequestBody Lang lang){
        langRepository.save(lang);
    }
}
