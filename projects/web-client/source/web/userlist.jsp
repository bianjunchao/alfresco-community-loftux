<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/custom.tld" prefix="awc" %>

<%@ page isELIgnored="false" %>

<f:view>
   <%-- load a bundle of properties I18N strings here --%>
   <f:loadBundle basename="messages" var="msg"/>
   
   <h:form id="userListForm">
   
      <h2>Users</h2>
      
      <%-- use JSTL as simple example way to list the users --%>
      <%-- TODO: find out how to get this working - currently it can't find the JSF bean
                 in the session scope. Using useBean tag creates a new copy --%>
      <%--<jsp:useBean id="UserListBean" scope="session" class="jsftest.UserListBean" />--%>
      <%--
      <ol>
         <c:forEach items="${sessionScope.UserListBean.users}" var="u">
            <li>Username: ${u.username}, Name: ${u.name}, Roles: ${u.roles}</li>
         </c:forEach>
      </ol>
            
      <p>
      --%>
      
      <%-- data grid test --%>
      before datagrid table
      <awc:data-grid cellspacing="2" cellpadding="1" styleClass="mycss" style="border:2px">
      inside datagrid table
      </awc:data-grid>
      after datagrid table
      
      <%-- rich list test --%>
      <awc:rich-list viewModes="list,details,icon" filtering="true" sorting="true" pageSize="5" styleClass="mycss" style="border:2px" value="#{UserListBean.users}" var="u">
         <awc:column label="Name" primary="true">
            <h:outputText value="#{u.name}"/>
         </awc:column>
         <awc:column label="Join Date">
            <h:commandButton value="#{u.joinDate}"/>
         </awc:column>
      </awc:rich-list>
      
      <%-- example of using a JSF DataTable to list the users --%>
      <%-- iterates around the List of User objects in the UserListBean --%>
      <h:dataTable id="userlist" value="#{UserListBean.usersModel}" var="u">
        <h:column>
          <f:facet name="header">
            <%-- NOTE: You cannot insert plain HTML text here, due to the way that some JSF
                       components are architected, the plain HTML would get displayed before
                       the body of the datatable tag is output. This is also true of the
                       other container tags including 'panel'.
                       The datatable is considerably inferior to our portal data tags
                       or even the freely available 'displaytag' tag library --%>
            <%-- You can also use the nasty 'f:verbatim' tag to wrap any non JSF elements --%>
            <h:outputText value="#{msg.username}"/>
          </f:facet>
          <h:outputText value="#{u.username}"/>
        </h:column>
        <h:column>
          <f:facet name="header">
            <h:outputText value="#{msg.name}"/>
          </f:facet>
          <h:outputText value="#{u.name}"/>
        </h:column>
        <h:column>
          <f:facet name="header">
            <h:outputText value="#{msg.joindate}"/>
          </f:facet>
          <h:outputText value="#{u.dateJoined}">
            <%-- example of a DateTime converter --%>
            <%-- can be used to convert both input and output text --%>
            <f:convertDateTime dateStyle="short" />
          </h:outputText>
        </h:column>
        <h:column>
          <f:facet name="header">
            <h:outputText value="#{msg.roles}"/>
          </f:facet>
          <h:outputText value="#{u.roles}"/>
        </h:column>
        <h:column>
          <f:facet name="actions">
            <h:outputText value="#{msg.actions}"/>
          </f:facet>
          <%-- inline command link - has an action listener which will decode which
               item in the grid it was clicked using the param tag below
               Then the action listener will delegate to the action view --%>
          <h:commandLink id="edit" value="Edit" action="edituser" actionListener="#{UserListBean.editUser}"/>
          <f:param id="userId" name="id" value="#{u.username}" />
        </h:column>
      </h:dataTable>
      
      <p>
      
      <h:commandButton id="add-user" value="Add" action="adduser" actionListener="#{UserListBean.addUser}"/>
      
   </h:form>
</f:view>
