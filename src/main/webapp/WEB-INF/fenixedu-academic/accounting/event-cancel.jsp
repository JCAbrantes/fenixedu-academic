<%--

    Copyright © 2017 Instituto Superior Técnico

    This file is part of FenixEdu Academic.

    FenixEdu Academic is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FenixEdu Academic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FenixEdu Academic.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page trimDirectiveWhitespaces="true" %>

<link rel="stylesheet" type="text/css" media="screen" href="<%= request.getContextPath() %>/CSS/accounting.css"/>

${portal.toolkit()}

<spring:url value="../{event}/cancelEvent" var="cancelUrl">
    <spring:param name="event" value="${event.externalId}"/>
</spring:url>
<spring:url value="../{event}/details" var="detailsUrl">
    <spring:param name="event" value="${event.externalId}"/>
</spring:url>

<div class="container-fluid">
    <header>
        <h1>
            <jsp:include page="heading-event.jsp"/>
        </h1>
    </header>
        <div class="row">
            <c:if test="${not empty error}">
                <section>
                    <ul class="nobullet list6">
                        <li><span class="error0"><c:out value="${error}"/></span></li>
                    </ul>
                </section>
            </c:if>
        </div>
    <c:set var="person" scope="request" value="${event.person}"/>
    <jsp:include page="heading-person.jsp"/>

    <div class="row">
        <h3>Cancelar Divida</h3>
        <form:form role="form" class="form-horizontal" action="${cancelUrl}" method="post">
            ${csrf.field()}
            <div class="form-group">
                <label class="control-label col-sm-1"><spring:message code="label.description"/></label>
                <div class="col-sm-4">
                    <c:out value="${event.description}"/>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-1"><spring:message code="label.justification"/></label>
                <div class="col-sm-4">
                    <textarea name="justification" class="form-control" rows="4" required></textarea>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-offset-1 col-sm-4">
                    <button class="btn btn-primary" type="submit">
                        <spring:message code="label.submit"/>
                    </button>
                    <a href="${detailsUrl}" class="btn btn-default"><spring:message code="label.cancel"/><a/>
                </div>
            </div>
        </form:form>
    </div>
</div>
