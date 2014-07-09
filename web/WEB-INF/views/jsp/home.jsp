
<%@ include file="/WEB-INF/views/jsp/common/include.jsp" %>

<!-- set page scope variables -->
<c:set var="saml_cookie" value="<%= esg.orp.Parameters.OPENID_SAML_COOKIE %>"/>
<c:set var="identity_cookie" value="<%= esg.orp.Parameters.OPENID_IDENTITY_COOKIE %>"/>
<c:set var="openid" value="<%= esg.orp.Parameters.OPENID_IDENTITY %>"/>
<c:set var="target_url" value="<%= esg.orp.Parameters.OPENID_URL %>"/>
<c:set var="last_exception_key" value="<%= org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION %>"/>


<authz:authentication property="principal" var="principal"/>

<tiles:insertDefinition name="center-layout">


	<tiles:putAttribute type="string" name="title" value="OpenID Login Page" />
	
	<!-- automatic redirect if "openid" parameter is detected -->
	<tiles:putAttribute name="script">
	
		<c:if test="${param[openid] != null}">
			<c:url value="${target_url}" var="myurl">
				<c:param name="openid_identifier" value="${param[openid]}"/>
			</c:url>
			<script type="text/javascript">
				window.location= "${myurl}";
			</script>		
		</c:if>
		
	</tiles:putAttribute>

	<!-- body -->
	<tiles:putAttribute name="body">
		
		<h1>Data Access Login</h1>
		
						
		<c:choose>
		
			<c:when test="${principal != null}">
			
				<!-- User is authenticated -->
				<table align="center">
					<tr>
						<td align="center">
							<div class="panel">
								
								<table>
									<caption>Status: logged-in</caption>
									<tr>
										<td>
											<img src='<c:url value="/themes/openid.png"/>' width="80" hspace="10px"/>
										</td>
										<td>
											<form name="logoutForm" action='<c:url value="/j_spring_security_logout"/>' >					
												<table border="0" cellpadding="10px" cellspacing="10px" align="center">
													<tr>
														<td align="center">Thank you, you are now logged in.</td>
													</tr>
													<tr>
														<td align="center">Your Openid:&nbsp;<b><c:out value="${principal.username}"/></b></td>
													</tr>
												</table>
											</form>
										</td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
				</table>

			</c:when>
			
			<c:otherwise>
			
				<c:set value="${cookie[identity_cookie]}" var="openidCookie"/>
				
				<c:if test="${param['failed'] == 'true'}">
					<p>&nbsp;</p>
					<div class="errorbox">
						<p>&nbsp;</p>
						<b>Login Failed</b>						
						<p class="error">ERROR: <c:out value="${sessionScope[last_exception_key].message}"/>
						<p>&nbsp;</p>
					</div>
				</c:if>
				
				<c:if test="${sessionScope['redirect']!=null}">
					<p/>&nbsp;<p/>The following URL requires authentication: 
					<br/><b><c:out value="${sessionScope['redirect']}"/></b>
				</c:if>
			
				<p>&nbsp;</p>Please select your openid provider or type it and then select it and you will be redirected to the login page at that site
				
				<!-- user is NOT authenticated -->
				<p/>&nbsp;<p/>
				<form method="post" action="<c:url value='${target_url}'/>">
					<script language="javascript">
                    function sanitize()
                    {
                      var oNewOption = null;

                      openidElement_t = document.getElementById("idp_identifier_text");
					  if(openidElement_t.value.length >1)
                      {
                        openidElement = document.getElementById("openid_identifier");
  						if (oNewOption == null) 
  						{
    					  oNewOption = document.createElement("OPTION");
    					  openidElement.options.add(oNewOption);
  						}
                        oNewOption.text = openidElement_t.value;
  						oNewOption.value = openidElement_t.value;

                        openidElement = document.getElementById("openid_identifier");
                        openidElement.value = openidElement_t.value; 
                        openidElement_t.value = "";
                      }
                      else
                      { 
                        openidElement = document.getElementById("openid_identifier");
                      }
               
					  openid = openidElement.value;
					  openid = openid.replace("http:","https:")
							               .replace(/^\s\s*/, '').replace(/\s\s*$/, '');
					  openidElement.value = openid;
					}
					</script>				
					<table align="center">
						<tr>
							<td>
								<div class="panel">
									<table border="0" align="center">
										<caption>Status: not logged-in</caption>
										<tr>
											<td align="right" valign="middle">
												<img src='<c:url value="/themes/openid.png"/>' width="80" />
											</td>
											<td>
												<table>
													<tr>
														<td align="right" class="required">OpenID</td>
														<td align="left">
														   <!-- kltsa 27/05/2014, issue 23061: Enable text field and button used for
														                                       sending text openid.
                                                            -->
                                                             
															<script language="javascript">
												            function en_openid_textfield() 
															{
															  document.getElementById("idp_identifier_text").value ="";	
															  document.getElementById("idp_identifier_text").style.visibility = "visible";                                                             
															}
															
															</script>
														   
														   	<br><br>												   
														    <SELECT name="openid_identifier" id="openid_identifier"  STYLE="width: 500px"> 
                                                             <OPTION value="https://localhost:8443/esgf-idp/openid/testUser1234">https://localhost:8443/esgf-idp/openid/testUser1234</OPTION>
                                                             <OPTION value="https://ceda.ac.uk/openid/">https://ceda.ac.uk/openid/</OPTION> 
                                                             <OPTION value="">nothing</OPTION> 
                                                            </SELECT>												
														    <br>

															<button type="button"  style="height:20px; width:40px" onClick="en_openid_textfield()">></button>
		        											<input type="text" name="idp_identifier_text" size="55" value="" id="idp_identifier_text" style="visibility:hidden" onkeyup="AddListItem(this)"/>						
														    	                                                            													
														</td>
														<td><input type="submit" value="GO" onclick="javascript:sanitize()"/></td>													
													</tr>
													<tr>
														<td align="center" colspan="4">
															<input type="checkbox" name="rememberOpenid" checked="checked" /> <span class="highlight">Remember my OpenID</span> on this computer
														</td>
													</tr>													
												</table>
											</td>
										</tr>
									</table>
								</div>
							</td>
						</tr>
					</table>
				</form>		
				<br>
				
				
				<c:if test="${sessionScope['redirect']!=null}">
					<p/>&nbsp;<p/>After logging in, you will be redirected to: 
					<br/>
					<b><c:out value="${sessionScope['redirect']}"/></b>
					<p/>&nbsp;<p/>
				</c:if>
					
											
			</c:otherwise>
		</c:choose>
			
	</tiles:putAttribute>

</tiles:insertDefinition>