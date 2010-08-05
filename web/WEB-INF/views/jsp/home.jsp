<%@ include file="/WEB-INF/views/jsp/common/include.jsp" %>

<!-- set page scope variables -->
<c:set var="saml_cookie" value="<%= esg.orp.Parameters.OPENID_SAML_COOKIE %>"/>
<c:set var="identity_cookie" value="<%= esg.orp.Parameters.OPENID_IDENTITY_COOKIE %>"/>
<c:set var="openid" value="<%= esg.orp.Parameters.OPENID_IDENTITY %>"/>
<c:set var="target_url" value="<%= esg.orp.Parameters.OPENID_URL %>"/>
<c:set var="last_exception_key" value="<%= org.springframework.security.web.authentication.AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY %>"/>


<authz:authentication property="principal" var="principal"/>

<tiles:insertDefinition name="center-layout">

	<tiles:putAttribute type="string" name="title" value="OpenID Login Page" />
	
	<!-- automatic redirect if "openid" parameter is detected -->
	<tiles:putAttribute name="script">
	
		<c:if test="${param[openid] != null}">
			<c:url value="${target_url}" var="myurl">
				<c:param name="openid_identity" value="${param[openid]}"/>
			</c:url>
			<script type="text/javascript">
				window.location= "${myurl}";
			</script>		
		</c:if>
	
		<script type="text/javascript">
			function init() {

				var openidButton = new YAHOO.widget.Button("openid-button");
				var cookieButton = new YAHOO.widget.Button("cookie-button");
			}
			YAHOO.util.Event.onDOMReady(init);
		</script>
		<style>
			.yui-fixed-panel#openid-panel { width: 50em; text-align: left; margin:0 auto; align: center; background-color: #FFFFFF; }
			.yui-fixed-panel#openid-panel .bd  { background-color: #FFFFFF; }
			.yui-fixed-panel#openid-panel td { padding: 0.3em; }			
			.yui-fixed-panel#cookie-panel { width: 50em; text-align: left; margin:0 auto; align: center; }
			.yui-fixed-panel#cookie-panel td { padding: 0.3em; }
		</style>
		
	</tiles:putAttribute>

	<!-- body -->
	<tiles:putAttribute name="body">
		
		<tiles:putAttribute type="string" name="pageTitle" value="Data Access Login" />
		<div style="height: 50px;">&nbsp;</div>
			
		<c:choose>
		
			<c:when test="${principal != null}">
			
				<!-- user is authenticated -->
				<div align="center">
					<p/>
					<b>User is logged in</b><br/>OpenID: <c:out value="${principal.username}"/>
					<p/>&nbsp;
				</div>
			
				<c:choose>
				
					<c:when test="${cookie[saml_cookie] != null}">
						
						<!-- authentication cookie detected -->
						<c:set value="${cookie[saml_cookie]}" var="mycookie"/>
						<div class="yui-fixed-panel" id="cookie-panel">
						    <div class="hd">Authentication Cookie</div>
							<div class="bd">
								<table border="0" align="center" cellpadding="4" cellspacing="="4"">
								    <tr>
									  <td align="right"><b>Name :</b></td>
									  <td align="left"><c:out value="${mycookie.name}"/></td>
									</tr>
									<tr>
									  <td align="right"><b>Domain :</b></td>
									  <td align="left"><c:out value="${mycookie.domain}"/></td>
									</tr>
									<tr>
									  <td align="right"><b>Path :</b></td>
									  <td align="left"><c:out value="${mycookie.path}"/></td>
									</tr>
									<tr>
									  <td align="right"><b>Secure :</b></td>
									  <td align="left"><c:out value="${mycookie.secure}"/></td>
									</tr>
									<tr>
									  <td align="right"><b>Value :</b></td>
									  <td align="left">
									  	<c:out value="${fn:substring(mycookie.value,0,50)}"/>...
									  </td>
									</tr>
									
								</table>
							</div>
						</div>
				
					</c:when>
					<c:otherwise>
					
						<!-- authentication cookie not detected, resubmit to send cookie to server -->
						<a href="<c:url value='' />" id="cookie-button">Show Cookie</a>
					
					</c:otherwise>
				
				</c:choose>

			</c:when>
			
			<c:otherwise>
			
				<c:set value="${cookie[identity_cookie]}" var="openidCookie"/>
				
				<c:if test="${param['failed'] == 'true'}">
					<div class="error">Login Failed</div>
					<p>&nbsp;</p>
					<p class="error">ERROR: <c:out value="${sessionScope[last_exception_key].message}"/>
					<p>&nbsp;</p>
				</c:if>
			
				<!-- user is NOT authenticated -->
				<form method="post" action="<c:url value='${target_url}'/>">
					<div class="yui-fixed-panel" id="openid-panel">
					    <div class="hd">OpenID Login</div>
						<div class="bd">
							<table border="0" align="center">
							    <tr>
								  <td align="center" colspan="4">
								  	Please enter your OpenID and you will be redirected to the login page at that site: 
								  </td>
								</tr>
								<tr>
									<td align="right" valign="absmiddle"><img src='<c:url value="/themes/openid_small.gif"/>'/></td>
									<td align="right" class="required">OpenID</td>
									<td align="left">
										<input type="text" name="openid_identifier" size="50" value="${openidCookie.value}"/>
									</td>
									<td><input type="submit" value="GO" id="openid-button"/></td>
								</tr>
								<tr>
									<td align="center" colspan="4">
										<input type="checkbox" name="rememberOpenid" checked="checked" /> <span class="highlight">Remember my OpenID</span> on this computer
									</td>
								</tr>
							</table>
						</div>
					</div>
				</form>			
											
			</c:otherwise>
		</c:choose>
			
	</tiles:putAttribute>

</tiles:insertDefinition>