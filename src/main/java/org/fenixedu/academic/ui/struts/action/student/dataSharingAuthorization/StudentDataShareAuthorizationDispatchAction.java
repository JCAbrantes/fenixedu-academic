/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Academic.
 *
 * FenixEdu Academic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Academic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.academic.ui.struts.action.student.dataSharingAuthorization;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.academic.ui.struts.action.student.StudentApplication.StudentViewApp;
import org.fenixedu.bennu.struts.annotations.Forward;
import org.fenixedu.bennu.struts.annotations.Forwards;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.bennu.struts.portal.EntryPoint;
import org.fenixedu.bennu.struts.portal.StrutsFunctionality;

@StrutsFunctionality(app = StudentViewApp.class, descriptionKey = "title.student.dataShareAuthorizations", path = "data-share",
        titleKey = "title.student.dataShareAuthorizations.short")
@Mapping(path = "/studentDataShareAuthorization", module = "student")
@Forwards({ @Forward(name = "authorizations", path = "/student/dataShareAuthorization/manageAuthorizations.jsp"),
        @Forward(name = "historic", path = "/student/dataShareAuthorization/authorizationHistory.jsp") })
public class StudentDataShareAuthorizationDispatchAction extends FenixDispatchAction {
    @EntryPoint
    public ActionForward manageAuthorizations(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        request.setAttribute("student", getLoggedPerson(request).getStudent());
        return mapping.findForward("authorizations");
    }

    public ActionForward saveAuthorization(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        addActionMessage(request, "message.student.dataShareAuthorization.saveSuccess");
        request.setAttribute("student", getLoggedPerson(request).getStudent());
        return mapping.findForward("authorizations");
    }

    public ActionForward viewAuthorizationHistory(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) {
        request.setAttribute("student", getLoggedPerson(request).getStudent());
        return mapping.findForward("historic");
    }
}
