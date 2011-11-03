<%@ include file="/WEB-INF/views/jsp/common/include.jsp" %>

<tiles:insertDefinition name="center-layout">

	<tiles:putAttribute type="string" name="title" value="Registration Response Page" />
		
	<tiles:putAttribute name="body">
		
		<tiles:putAttribute type="string" name="pageTitle" value="Registration Response Page" />
		<h1>Group Registration Response</h1>
		<authz:authentication property="principal" var="principal"/>
		
		<p>&nbsp;</p>
		<table align="center">
			<tr>
				<td>
					<div class="panel">
			
						<table align="center">
							<caption>Registration result: <c:out value="${param['result']}"/></caption>
							<tr>
								<td>											
									<c:choose>
										
										<c:when test="${param['result']=='SUCCESS'}">
											Congratulations!
											<br/>Your request to register in group <b>${param['group']}</b> has been approved.
											<br/>Your membership is effective immediately.
											<br/>&nbsp;
											<br/>Click on the button below to continue your original request:
											<br/>&nbsp;
											<br/>
											<form method="get" action="${param['resource']}">
												<input type='submit' value="Download Data" id="goButton" />
											</form>							
											<script type="text/javascript">var button = new YAHOO.widget.Button("goButton");</script>
										</c:when>
										
										<c:when test="${param['result']=='PENDING'}">
											Your request to register in group <b>${param['group']}</b> is waiting for approval.
											<br/>You will be notified by email as soon as possible of the decision outcome.
											<br/>Thank you for your patience.
										</c:when>
										
										<c:when test="${param['result']=='EXISTING'}">
											You are already registered in group <b>${param['group']}</b> with role <b>${param['role']}</b>.
											<br/>&nbsp;
											<br/>Click on the button below to continue your original request:
											<br/>&nbsp;
											<br/>
											<form method="get" action="${param['resource']}">
												<input type='submit' value="Download Data" id="goButton" />
											</form>							
											<script type="text/javascript">var button = new YAHOO.widget.Button("goButton");</script>
										</c:when>
										
										
										<c:when test="${param['result']=='DENIED'}">
											Your request to register in group <b>${param['group']}</b> has been denied.
											<br/>Please contact support for any questions.
											<br/>Thank you for your understanding.
										</c:when>
										
										<c:when test="${param['result']=='ERROR'}">
											Your request to register in group <b>${param['group']}</b> resulted in error.
											<br/>Please contact support for any questions.
											<br/>Thank you for your understanding.
										</c:when>
														
										<c:otherwise>
											The result of your registration request in group <b>${param['group']}</b> is unknown.
											<br/>Please contact support for any questions.
										</c:otherwise>
														
									</c:choose>
								</td>
							</tr>
						</table>
		
					</div>
				</td>
			</tr>
		</table>
			
		&nbsp;<p/>
		Your openid: <b><c:out value="${principal.username}"/></b>		
	
	</tiles:putAttribute>

</tiles:insertDefinition>
