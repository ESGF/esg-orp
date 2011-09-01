<%@ include file="/WEB-INF/views/jsp/common/include.jsp" %>

<tiles:insertDefinition name="center-layout">

	<tiles:putAttribute type="string" name="title" value="Registration Response Page" />
	
	<tiles:putAttribute name="script">
		<style>
			.yui-fixed-panel#registration-panel { width: 50em; text-align: center; margin:0 auto; align: center; }
			.yui-fixed-panel#registration-panel td { padding: 0.3em; }
		</style>		
	</tiles:putAttribute>
	
		
	<tiles:putAttribute name="body">
		
		<tiles:putAttribute type="string" name="pageTitle" value="Registration Response Page" />
		<authz:authentication property="principal" var="principal"/>
		
		<div class="yui-fixed-panel" id="registration-panel">
			<div class="hd">Group Registration Result: "${param['result']}" </div>
			<div class="bd">
			
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
		
			</div>
		</div>
			
		&nbsp;<p/>
		Your openid: <b><c:out value="${principal.username}"/></b>		
	
	</tiles:putAttribute>

</tiles:insertDefinition>
