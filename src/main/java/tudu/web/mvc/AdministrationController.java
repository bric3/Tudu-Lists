package tudu.web.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import tudu.domain.User;
import tudu.service.ConfigurationService;
import tudu.service.UserService;

import java.util.List;

/**
 * Application administration actions.
 * 
 * @author Julien Dubois
 */
@Controller
@RequestMapping("/admin")
public class AdministrationController {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private UserService userService;

    /**
     * Show the administration page action.
     *
     * Niveau 1
     * - Vérifier qu'aucune interactions n'a lieu lorsque la page demandée n'est ni "configuration" ni "users"
     *
     * Niveau 2
     * - La réponse par défaut du mock configService et faire un test avec la page configuration
     * - Vérifier dans un test que pour la page "configuration" configService soit invoqué quelque soit les propriétés
     *
     * Niveau 2 à 3
     * - Vérifier dans un test que pour la page "configuration" les propriétés smtp (et uniquement celles là) soit donnée au model
     * - Vérifier dans un test que pour la page "configuration" il n'y a pas d'interaction avec userService.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView display(@RequestParam(required = false) String page) {

        ModelAndView mv = new ModelAndView();
        AdministrationModel model = new AdministrationModel();

        if (page == null || page.equals("")) {
            page = "configuration";
        }

        if (page.equals("configuration")) {
            mv.addObject("page", "configuration");

            String propertyStaticPath = this.configurationService.getProperty("application.static.path").getValue();
            model.setPropertyStaticPath(propertyStaticPath);

            String googleAnalyticsKey = configurationService.getProperty("google.analytics.key").getValue();
            model.setGoogleAnalyticsKey(googleAnalyticsKey);

            String smtpHost = configurationService.getProperty("smtp.host").getValue();
            model.setSmtpHost(smtpHost);

            String smtpPort = configurationService.getProperty("smtp.port").getValue();
            model.setSmtpPort(smtpPort);

            String smtpUser = configurationService.getProperty("smtp.user").getValue();
            model.setSmtpUser(smtpUser);

            String smtpPassword = configurationService.getProperty("smtp.password").getValue();
            model.setSmtpPassword(smtpPassword);

            String smtpFrom = configurationService.getProperty("smtp.from").getValue();
            model.setSmtpFrom(smtpFrom);


        } else if (page.equals("users")) {
            mv.addObject("page", "users");
            model.setSearchLogin("");
            mv.addObject("numberOfUsers", this.userService.getNumberOfUsers());

        }
        mv.addObject("administrationModel", model);
        mv.setViewName("administration");
        return mv;
    }

    /**
     * Update the application configuration.
     *
     * Niveau 1
     * - Vérifer que pour l'action "enableUser" le service afférent est appelé et que disableUser ne l'est pas
     * - Vérifer que pour l'action "disableUser" le service afférent est appelé et que enableUser ne l'est pas (d'une manière différente)
     *
     * Niveau 2
     * - Vérifier que pour l'action appelle findUsersByLogin après un enableUser ou un disableUser
     * - Vérifier que pour l'action appelle findUsersByLogin après un enableUser ou un disableUser
     *
     * Niveau 2 à 3
     * - Vérifier qu'aucune intéraction n'est faite avec userService/ configurationService si la page ne correspond à rien
     * - Vérifier que le configService.updateEmailProperties est bien appelé en ne vérifiant que les valeurs user et password
     */
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView update(@ModelAttribute AdministrationModel model) {
        ModelAndView mv = new ModelAndView();
        if ("configuration".equals(model.getAction())) {
            this.configurationService.updateApplicationProperties(model.getPropertyStaticPath(),
                    model.getGoogleAnalyticsKey());

            this.configurationService.updateEmailProperties(
                    model.getSmtpHost(),
                    model.getSmtpPort(),
                    model.getSmtpUser(),
                    model.getSmtpPassword(),
                    model.getSmtpFrom()
            );

            mv = this.display("configuration");
            mv.addObject("success", "true");
        } else {
            if ("disableUser".equals(model.getAction())) {
                String login = model.getLogin();
                this.userService.disableUser(login);
            }
            if ("enableUser".equals(model.getAction())) {
                String login = model.getLogin();
                this.userService.enableUser(login);
            }
            if (model.getSearchLogin() == null) {
                model.setSearchLogin("");
            }
            List<User> users = this.userService.findUsersByLogin(model.getSearchLogin());
            mv.addObject("users", users);
            mv.addObject("page", "users");
            mv.addObject("administrationModel", model);
            mv.setViewName("administration");
        }

        return mv;
    }
}
