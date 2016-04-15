/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.user.accountsettings.profile.view;

import com.esofthead.mycollab.common.i18n.GenericI18Enum;
import com.esofthead.mycollab.module.user.accountsettings.localization.UserI18nEnum;
import com.esofthead.mycollab.module.user.domain.User;
import com.esofthead.mycollab.module.user.service.UserService;
import com.esofthead.mycollab.spring.ApplicationContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.ui.NotificationUtil;
import com.esofthead.mycollab.vaadin.web.ui.UIConstants;
import com.esofthead.mycollab.vaadin.web.ui.grid.GridFormLayoutHelper;
import com.google.common.base.MoreObjects;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
class ContactInfoChangeWindow extends Window {
    private TextField txtWorkPhone = new TextField();
    private TextField txtHomePhone = new TextField();
    private TextField txtFaceBook = new TextField();
    private TextField txtTwitter = new TextField();
    private TextField txtSkype = new TextField();
    private Validator validation;

    private User user;

    ContactInfoChangeWindow(User user) {
        this.user = user;
        this.setWidth("450px");
        this.setResizable(false);
        this.setModal(true);
        this.validation = ApplicationContextUtil.getValidator();
        this.initUI();
        this.center();
        this.setCaption(AppContext.getMessage(UserI18nEnum.WINDOW_CHANGE_CONTACT_INFO_TITLE));
    }

    private void initUI() {
        MVerticalLayout mainLayout = new MVerticalLayout().withMargin(new MarginInfo(false, false, true, false)).withWidth("100%");

        GridFormLayoutHelper passInfo = GridFormLayoutHelper.defaultFormLayoutHelper(1, 6);

        passInfo.addComponent(txtWorkPhone, AppContext.getMessage(UserI18nEnum.FORM_WORK_PHONE), 0, 0);
        passInfo.addComponent(txtHomePhone, AppContext.getMessage(UserI18nEnum.FORM_HOME_PHONE), 0, 1);
        passInfo.addComponent(txtFaceBook, "Facebook", 0, 2);
        passInfo.addComponent(txtTwitter, "Twitter", 0, 3);
        passInfo.addComponent(txtSkype, "Skype", 0, 4);

        txtWorkPhone.setValue(MoreObjects.firstNonNull(user.getWorkphone(), ""));
        txtHomePhone.setValue(MoreObjects.firstNonNull(user.getHomephone(), ""));
        txtFaceBook.setValue(MoreObjects.firstNonNull(user.getFacebookaccount(), ""));
        txtTwitter.setValue(MoreObjects.firstNonNull(user.getTwitteraccount(), ""));
        txtSkype.setValue(MoreObjects.firstNonNull(user.getSkypecontact(), ""));
        mainLayout.addComponent(passInfo.getLayout());
        mainLayout.setComponentAlignment(passInfo.getLayout(), Alignment.TOP_LEFT);

        MHorizontalLayout hlayoutControls = new MHorizontalLayout().withMargin(new MarginInfo(false, true, false, true));

        Button cancelBtn = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_CANCEL), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                ContactInfoChangeWindow.this.close();
            }
        });
        cancelBtn.setStyleName(UIConstants.BUTTON_OPTION);

        Button saveBtn = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_SAVE), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                changeUserInfo();
            }
        });
        saveBtn.setStyleName(UIConstants.BUTTON_ACTION);
        saveBtn.setIcon(FontAwesome.SAVE);

        hlayoutControls.with(cancelBtn, saveBtn).alignAll(Alignment.MIDDLE_CENTER);
        mainLayout.with(hlayoutControls).withAlign(hlayoutControls, Alignment.MIDDLE_RIGHT);
        this.setContent(mainLayout);
    }

    public boolean validateForm(final Object data) {
        Set<ConstraintViolation<Object>> violations = this.validation.validate(data);
        if (violations.size() > 0) {
            final StringBuilder errorMsg = new StringBuilder();

            for (ConstraintViolation violation : violations) {
                errorMsg.append(violation.getMessage()).append("<br/>");

                if (violation.getPropertyPath() != null && !violation.getPropertyPath().toString().equals("")) {
                    if (violation.getPropertyPath().toString().equals("workphone")) {
                        txtWorkPhone.addStyleName("errorField");
                    }

                    if (violation.getPropertyPath().toString().equals("homephone")) {
                        txtHomePhone.addStyleName("errorField");
                    }
                }
            }

            NotificationUtil.showErrorNotification(errorMsg.toString());
            return false;
        }

        return true;
    }

    private void changeUserInfo() {
        txtWorkPhone.removeStyleName("errorField");
        txtHomePhone.removeStyleName("errorField");

        user.setWorkphone(txtWorkPhone.getValue());
        user.setHomephone(txtHomePhone.getValue());
        user.setFacebookaccount(txtFaceBook.getValue());
        user.setTwitteraccount(txtTwitter.getValue());
        user.setSkypecontact(txtSkype.getValue());

        if (validateForm(user)) {
            UserService userService = ApplicationContextUtil.getSpringBean(UserService.class);
            userService.updateWithSession(user, AppContext.getUsername());
            close();
            Page.getCurrent().getJavaScript().execute("window.location.reload();");
        }
    }
}
