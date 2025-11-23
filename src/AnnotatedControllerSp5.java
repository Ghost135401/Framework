package com.vonjy.spring.controller;

import com.vonjy.spring.annotation.ControllerAnnotation;
import com.vonjy.spring.annotation.UrlMapping;
import com.vonjy.spring.util.http.*;
import java.util.LinkedList;
import java.util.List;

@ControllerAnnotation
public class AnnotatedControllerSp5 {

    @UrlMapping(path = "/c1/string1")
    private String methodWithAnnotation1() {
        return "Réponse textuelle depuis l'endpoint /c1/path1";
    }

    @UrlMapping(path = "/c1/string2")
    private String methodWithAnnotation2() {
        return "Donnée retournée par /c1/path2";
    }

    @UrlMapping(path = "/c1/mav1")
    private ModelAndView methodWithAnnotation3() {
        ModelAndView viewModel = new ModelAndView("/pages/mav/page1.jsp");

        List<Integer> actionsSequence = new LinkedList<>();
        actionsSequence.add(3);
        actionsSequence.add(7);
        actionsSequence.add(2);

        viewModel.setAttribute("actionsSequence", actionsSequence);
        viewModel.setAttribute("notificationText", "Démarrage du processus!");

        return viewModel;
    }

    @UrlMapping(path = "/c1/void1")
    private void methodWithAnnotation4() {
        System.out.println("Execution terminée depuis l'URL /c1/void1");
    }

    private void methodWithoutAnnotation() {
        // Méthode interne sans mapping
    }

    private void anotherMethodWithoutAnnotation() {
        // Autre méthode utilitaire non exposée
    }
}
