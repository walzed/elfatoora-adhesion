package tn.tn.elfatoora.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/cgu")
public class cGUController {
 
    @GetMapping("/donnees-personnelles")
    public String cguDonneesPrsonnelles() {
    	 return "cgu/donnees-personnelles";
    }

    @GetMapping("/infos-legales")
    public String cguInfoLegal() {
    	 return "cgu/infos-legales";
    }
    

    @GetMapping("/conditions-generales")
    public String cguCondition() {
    	 return "cgu/conditions-generales";
    }
}
