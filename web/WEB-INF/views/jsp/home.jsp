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

<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<link rel="stylesheet" href="/resources/demos/style.css">

<!-- kltsa 13/08/2014 : : A select element with auto complete 
                         (Functionality from : http://jqueryui.com/autocomplete/#combobox). 
-->
<style>
.custom-combobox 
{
  position: relative;
  display: inline-block;
  text-align:left;
}
.custom-combobox-toggle 
{
  position: absolute;
  top: 0;
  bottom: 0;
  margin-left: 0px;
  padding: 0;
  /* support: IE7 */
  *height: 2.7em;
  *top: 0.1em;
  text-align:left;
}
.custom-combobox-input 
{
  top : 10
  margin: 10;
  text-align:left;
  padding: 0.5em;
  width: 600px;
  height: 16.8px;
  float:left;
}
</style>
				
		
<script language="javascript">

	(function($) {
		$
				.widget(
						"custom.combobox",
						{
							_create : function() {
								this.wrapper = $("<span>").addClass(
										"custom-combobox").insertAfter(
										this.element);
								this.element.hide();
								this._createAutocomplete();
								this._createShowAllButton();
							},
							_createAutocomplete : function() {
								var selected = this.element
										.children(":selected"), value = selected
										.val() ? selected.text() : "";
								this.input = $("<input>")
										.appendTo(this.wrapper)
										.val(value)
										.attr("title", "")
										.addClass(
												"custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left")
										.autocomplete({
											delay : 0,
											minLength : 0,
											source : $.proxy(this, "_source")
										}).tooltip({
											tooltipClass : "ui-state-highlight"
										});
								this._on(this.input, {
									autocompleteselect : function(event, ui) {
										ui.item.option.selected = true;
										this._trigger("select", event, {
											item : ui.item.option
										});
									},
									autocompletechange : "_removeIfInvalid"
								});
							},
							_createShowAllButton : function() {
								var input = this.input, wasOpen = false;
								$("<a>")
										.attr("tabIndex", -1)
										.attr("title", "Show All Items")
										.tooltip()
										.appendTo(this.wrapper)
										.button(
												{
													icons : {
														primary : "ui-icon-triangle-1-s"
													},
													text : false
												})
										.removeClass("ui-corner-all")
										.addClass(
												"custom-combobox-toggle ui-corner-right")
										.mousedown(
												function() {
													wasOpen = input
															.autocomplete(
																	"widget")
															.is(":visible");
												}).click(function() {
											input.focus();
											// Close if already visible
											if (wasOpen) {
												return;
											}
											// Pass empty string as value to search for, displaying all results
											input.autocomplete("search", "");
										});
							},
							_source : function(request, response) {
								var matcher = new RegExp($.ui.autocomplete
										.escapeRegex(request.term), "i");
								response(this.element
										.children("option")
										.map(
												function() {
													var text = $(this).text();
													if (this.value
															&& (!request.term || matcher
																	.test(text)))
														return {
															label : text,
															value : text,
															option : this
														};
												}));
							},
							_removeIfInvalid : function(event, ui) {
								// Selected an item, nothing to do
								if (ui.item) {
									return;
								}
								// Search for a match (case-insensitive)
								var value = this.input.val(), valueLowerCase = value
										.toLowerCase(), valid = false;
								this.element
										.children("option")
										.each(
												function() {
													if ($(this).text()
															.toLowerCase() === valueLowerCase) {
														this.selected = valid = true;
														return false;
													}

												});
								// Found a match, nothing to do
								if (valid) {
									return;
								}

								// Remove invalid value
								this.input.val(value).attr(
										"title",
										"\"" + value
												+ "\"  didn't match any item.")
										.tooltip("open");

								/* kltsa 15/08/2014 : if option does not exists, add it.*/
								select = document
										.getElementById('openid_identifier');
								var opt = document.createElement('option');
								opt.value = value;
								opt.innerHTML = value;
								select.appendChild(opt);
								select.value = value;
								this.selected = valid = true;
								return false;

								//this.element.val("");
								this._delay(function() {
									this.input.tooltip("close").attr("title",
											"");
								}, 5500);
								this.input.autocomplete("instance").term = "";
							},
							_destroy : function() {
								this.wrapper.remove();
								this.element.show();
							}
						});
	})(jQuery);
	$(function() {
		$("#openid_identifier").combobox();
		$("#toggle").click(function() {
			$("#openid_identifier").toggle();
		});
	});
</script>

<p/>&nbsp;<p/>
<form method="post" action="<c:url value='${target_url}'/>">
<script language="javascript">
function sanitize() 
{
  $("#Go_button").focus();	
  openidElement = document.getElementById("openid_identifier");
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
<td align="left"  WIDTH="650">

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>

<fmt:setBundle var="bundle" basename="esgf"/>
<fmt:message var="path" key="orp.provider.list" bundle="${bundle}"/>
<c:import url="file:${path}" var="doc_xml"/>

<x:parse xml="${doc_xml}" var="parsed_doc"/>
<select name="openid_identifier" id="openid_identifier">
<x:forEach select="$parsed_doc/OPS/OP" var="item">
 <option value=<x:out select="$item/URL"/>><x:out select="$item/NAME"/></option>
</x:forEach>
</select>

</td>
<td  align="center">
<input type="submit" value="GO" style="height:30px; width:50px" id="Go_button" onclick="javascript:sanitize()"/>
</td>												
</tr>
<tr>
<td align="center" colspan="4">
<input type="checkbox" name="rememberOpenid" checked="checked"/> <span class="highlight">Remember my OpenID</span> on this computer
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