package org.immregistries.ehr.api;

import org.immregistries.ehr.model.Patient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServlet;

@Controller
@RequestMapping("/patient")
public class PatientController {


    @RequestMapping("/random")
    public ModelAndView hello() {

        String helloWorldMessage = "Hello world from java2blog!";
        return new ModelAndView("hello", "message", helloWorldMessage);
    }
}
