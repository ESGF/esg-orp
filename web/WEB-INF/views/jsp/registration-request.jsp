<%@ include file="/WEB-INF/views/jsp/common/include.jsp" %>

<tiles:insertDefinition name="center-layout">

	<tiles:putAttribute type="string" name="title" value="Registration Request Page" />
	
	<tiles:putAttribute name="script">		
		
		<script type="text/javascript">
		
		// function to show setup and license agreement dialog
		function showLicense(licenseUri) {
			
			var xmlDataSource = new YAHOO.util.XHRDataSource(licenseUri);
			xmlDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_XML;
			xmlDataSource.useXPath = true;
			xmlDataSource.responseSchema = {
					resultNode: "license",
					fields : [
					          { key: "title", locator: "title" },
					          { key: "body", locator: "body" },			        
					         ]
			};
			xmlDataSource.maxCacheEntries = 4;
			
			// the success handler sets up the dialog title and body, and displays it
			var mySuccessHandler = function(request, response) {
				
				// set title and body of license dialog
				licenseDialog.setHeader(response.results[0].title);
				body = response.results[0].body;
				// replace double new lines charecter in text with HTML break
				body = body.replace(/(\n\n)/gm,"<p>&nbsp;</p>");
				body = "<div style='text-align:left'>" + body + "</div>";
			    licenseDialog.setBody(body);
				
				// show license dialog
				licenseDialog.show();

			};
			
			var myFailureHandler = function(request, response) {
				showErrorDialog("Error", "Sorry, an error occurred while displaying the license agreement.<br/>Please contact technical support.");
			};
			
			var callbackObj = { 
					success : mySuccessHandler, 
					failure : myFailureHandler
			}; 
			xmlDataSource.sendRequest(null, callbackObj); 
			
			
		
		}
		
		var licenseDialog;
		var myForm;
		
		function init() {
			
			// initialize the license dialog
			initLicenseDialog();
			
		}	
		
		// function to initialize the license dialog
		function initLicenseDialog() {
		
			licenseDialog = new YAHOO.widget.SimpleDialog("dlg", { 
			    width: "60em",
			    effect:{ effect: YAHOO.widget.ContainerEffect.FADE, duration: 0.25 }, 
			    fixedcenter: true,
			    modal: true,
			    visible: false,
			    draggable: false
			});
			 
			licenseDialog.setHeader("License Agreement");
			licenseDialog.setBody("License Text");
			licenseDialog.cfg.setProperty("icon", YAHOO.widget.SimpleDialog.ICON_WARN);
			
			// YES handler: hide the dialog, submit the form
			var handleYes = function(event, widget, obj) {
			    this.hide();
			    myForm.submit();
			};
			
			// NO handler: hide the dialog, don't submit the form
			var handleNo = function() {
			    this.hide();
			};
			
			var myButtons = [
			    { text: "I Agree.", handler: handleYes },
			    { text:"No, Thanks.", handler: handleNo, isDefault:true}
			];
			 
			licenseDialog.cfg.queueProperty("buttons", myButtons);
			licenseDialog.render(document.body);			
			
		}
		
		// function to show an error dialog
		function showErrorDialog(header, body) {
		
			errorDialog = new YAHOO.widget.SimpleDialog("err", { 
			    width: "40em", 
			    effect:{ effect: YAHOO.widget.ContainerEffect.FADE, duration: 0.25 }, 
			    fixedcenter: true,
			    modal: true,
			    visible: false,
			    draggable: false
			});
			 
			errorDialog.setHeader(header);
			errorDialog.setBody(body);
			errorDialog.cfg.setProperty("icon", YAHOO.widget.SimpleDialog.ICON_WARN);
			
			// DISMISS handler: hide the dialog
			var handleDismiss = function(event, widget, obj) {
			    this.hide();
			};
						
			var myButtons = [
			    { text: "Dismiss", handler: handleDismiss, isDefault:true }
			];
			 
			errorDialog.cfg.queueProperty("buttons", myButtons);
			errorDialog.render(document.body);			
			errorDialog.show();
			
		}
		
		// function to submit the form to register a user
		function register(event, formObj) {
			
			// save reference to form to be submitted
			myForm = formObj;
			
			// retrieve access group
			var group = formObj.group.value;
			
			// FIXME
			if (group=='CMIP5 Research') {								
				// retrieve specified license
				showLicense("licenses/ipccResearchLicense.xml");
				//showLicense("http://jpl-esg.jpl.nasa.gov/climate/ipccResearchLicense.xml");
			} else if (group=='CMIP5 Commercial') {
				// retrieve specified license
				showLicense("licenses/ipccCommercialLicense.xml");
			} else {
				// submit the form
				formObj.submit();
			}
		}
		
		YAHOO.util.Event.onDOMReady(init);
		
		</script>
	</tiles:putAttribute>
	
		
	<tiles:putAttribute name="body">
		
		<tiles:putAttribute type="string" name="pageTitle" value="Registration Request Page" />
		<h1>Group Registration Request</h1>
		<authz:authentication property="principal" var="principal"/>
				
		<p>&nbsp;</p>
		The URL you are trying to access:
		<br/><b><c:out value="${param['resource']}"/></b> 
		<br/>is restricted. 
		<p>&nbsp;</p>
		
		<!-- switch on number of attributes found -->
		<c:choose>
		
			<c:when test="${fn:length(policyAttributes) == 0}">
				Unfortunately, this URL is not associated with any known data access policy.
				Please contact support to have this configuration issue fixed.			
			</c:when>
					
			<c:otherwise>
				To obtain access to these data, please register with one of the following groups:
				
				<table align="center">
					<tr>
						<td>
							<div class="panel">
								<table border="0" align="center" cellpadding="4" cellspacing="="4"">
									<caption>Status: not registered</caption>
									<c:set var="count" value="0"/>
									<c:forEach var="entry" items="${policyAttributes}">
										<c:choose>
											<c:when test="${entry.key.type == 'ANY'}">
												<tr>
													<td colspan="2">
														<b>Resource is Freely Available</b>
													</td>
												</tr>
											</c:when>
											<c:when test="${entry.key.value != 'default'}">
												<c:forEach var="url" items="${entry.value}">
													<c:set var="count" value="${count+1}"/>
													<tr>
														<td align="right">
															Group: <b><c:out value="${entry.key.type}"/></b>
															&nbsp;
															Role: <b><c:out value="${entry.key.value}"/></b>
														</td>
														<td>
															<form method="post" id="form_${count}">
																<input type="hidden" name="group" value="${entry.key.type}"/>
																<input type="hidden" name="role" value="${entry.key.value}"/>
																<input type="hidden" name="user" value="${principal.username}"/>
																<input type="hidden" name="url" value="${url}"/>
																<input type="hidden" name="resource" value="${resource}"/>
																<input type="button" value="Register" id="button_${count}" />
															</form>
														</td>
													</tr>
													<!-- initialize YUI buttons -->
													<script type="text/javascript">
														var button_<c:out value="${count}"/> = new YAHOO.widget.Button("button_${count}");
														YAHOO.util.Event.addListener("button_${count}", "click", register, YAHOO.util.Dom.get("form_${count}") );
													</script>
												</c:forEach>
											</c:when>
											<c:otherwise>
												<!-- don't register for role='default' -->
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</table>
							</div>
						</td>
					</tr>
				</table>
		
			</c:otherwise>
		
		</c:choose>
		
		<p/>
		Thank you for your interest in accessing these data.
		
		
		<!-- display openid if available -->
		&nbsp;<p/>
		<authz:authentication property="principal" var="principal"/>
		<c:if test="${principal!=null}">
			&nbsp;<p/>Your openid: <b><c:out value="${principal.username}"/></b>
		</c:if>		
	
	</tiles:putAttribute>

</tiles:insertDefinition>
