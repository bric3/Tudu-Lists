package tudu.web.mvc;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;
import tudu.domain.Property;
import tudu.service.ConfigurationService;
import tudu.service.UserService;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AdministrationControllerTest {

    @Mock(answer = RETURNS_SMART_NULLS) private ConfigurationService cfgService;
    @Mock private UserService userService;

    @InjectMocks private AdministrationController adminController = new AdministrationController();

    @Test
    public void display_should_return_a_nonnull_model() throws Exception {
        assertThat(adminController.display("somepage")).isNotNull();
        // don't break; didn't try hard enough
    }

    @Test
    public void display_should_not_interact_when_page_different_than_configuration_or_users() throws Exception {
        assertThat(adminController.display("somepage")).isNotNull();

        verifyZeroInteractions(cfgService, userService);
    }

    @Test(expected = SmartNullPointerException.class)
    public void display_should_trhow_mockito_ex_when_configService_is_accessed_properties_when_page_is_configuration() throws Exception {
        // given config service not stubbed

        // when
        ModelAndView mv = adminController.display("configuration");
    }

    @Test
    public void display_should_read_configService_properties_when_page_is_configuration() throws Exception {
        // given
        given(cfgService.getProperty(anyString())).willReturn(property("whatever"));

        // when
        ModelAndView mv = adminController.display("configuration");

        // then
        verifyZeroInteractions(userService);
        assertThat(mv.getModelMap().get("page")).isEqualTo("configuration");
    }

    @Test
    public void display_should_put_smtp_config_properties_in_admin_model_when_page_is_configuration() throws Exception {
        // given
        given(cfgService.getProperty(anyString())).willReturn(property("whatever"));

        given(cfgService.getProperty("smtp.host")).willReturn(property("the host"));
        given(cfgService.getProperty("smtp.port")).willReturn(property("the port"));
        given(cfgService.getProperty("smtp.user")).willReturn(property("the user"));
        given(cfgService.getProperty("smtp.password")).willReturn(property("the pass"));
        given(cfgService.getProperty("smtp.from")).willReturn(property("from"));


        // when
        ModelAndView mv = adminController.display("configuration");


        // then
        AdministrationModel adminModel = (AdministrationModel) mv.getModelMap().get("administrationModel");
        assertThat(adminModel.getSmtpHost()).isEqualTo("the host");
        assertThat(adminModel.getSmtpPort()).isEqualTo("the port");
        assertThat(adminModel.getSmtpUser()).isEqualTo("the user");
        assertThat(adminModel.getSmtpPassword()).isEqualTo("the pass");
        assertThat(adminModel.getSmtpFrom()).isEqualTo("from");
    }


    @Test
    public void update_shouldnt_return_a_null_model() throws Exception {
        AdministrationModel adminModel = new AdministrationModel();
        adminModel.setAction("whatever");
        assertThat(adminController.update(adminModel));
    }

    @Test
    public void update_should_update_smtp_config_and_nothing_else() throws Exception {
        // given
        AdministrationController spiedAdmnController = spy(adminController);
        willReturn(new ModelAndView()).given(spiedAdmnController).display(anyString());

        AdministrationModel adminModel = new AdministrationModel();
        adminModel.setAction("configuration");
        adminModel.setSmtpUser("the user");
        adminModel.setSmtpPassword("password");

        // when
        spiedAdmnController.update(adminModel);

        // then
        verify(cfgService).updateEmailProperties(
                anyString(),
                anyString(),
                eq("the user"),
                eq("password"),
                anyString()
        );

        verifyZeroInteractions(userService);
    }

    @Test
    public void update_can_enable_user() throws Exception {
        AdministrationModel adminModel = new AdministrationModel();
        adminModel.setAction("enableUser");
        adminModel.setLogin("Paterne");

        adminController.update(adminModel);

        verify(userService).enableUser("Paterne");
        verify(userService, never()).disableUser("Paterne");
    }

    @Test
    public void update_can_disable_user() throws Exception {
        AdministrationModel adminModel = new AdministrationModel();
        adminModel.setAction("disableUser");
        adminModel.setLogin("Bob");

        adminController.update(adminModel);

        verify(userService).disableUser("Bob");
        verify(userService, times(0)).enableUser("Bob");
    }

    @Test
    public void update_should_fetch_users_on_login_after_disabling_suer() throws Exception {
        AdministrationModel adminModel = new AdministrationModel();
        adminModel.setAction("disableUser");
        adminModel.setSearchLogin("bob*");

        adminController.update(adminModel);

        InOrder inOrder = inOrder(userService);
        inOrder.verify(userService).disableUser(anyString());
        inOrder.verify(userService).findUsersByLogin("bob*");
    }

    private Property property(String value) {
        Property property = new Property();
        property.setValue(value);
        return property;
    }
}
