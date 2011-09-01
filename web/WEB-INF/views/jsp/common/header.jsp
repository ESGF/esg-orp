<%@ include file="/WEB-INF/views/jsp/common/include.jsp" %>

<style type="text/css">

#header {
    background: #FFFFFF;
    color: #ffc;
    overflow: hidden;
    height: 80px;
    margin: 0px;
    padding: 0px;
}
#logo_esgf {
    float: left;
    background: #FFFFFF;
    margin-top: 0px;
    padding-bottom: 0px;
}
#logo_institution {
    float: right;
    background: #FFFFFF;
    margin-top: 0px;
    padding-bottom: 0px;
}
</style>


<div id="header">
    <div id="logo_esgf">
         <img src='<c:url value="images/esgf.png"/>' style="height:76px; padding:2px" />
    </div>
    <div id="logo_institution">
    	<c:set var="ilogo"><spring:message code="esgf.homepage.institutionLogo" /></c:set>
    	<c:if test="${ilogo != ''}">
        	<img src='<c:url value="${ilogo}"/>' style="height:76px; padding:2px" />
        </c:if>
    </div>
</div>