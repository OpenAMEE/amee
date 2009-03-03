<#if !supportDetails??>
    <#assign supportDetails = true>
</#if>
<#if !supportEmail??>
    <#assign supportEmail = "mailto:support@jellymold.com">
</#if>
<#if !supportText??>
    <#assign supportText = "Email Jelly Mold support team">
</#if>
<#if !footerCopy??>
    <#assign footerCopy = "&copy; 2007 Jelly Mold Solutions Ltd.">
</#if>
<!-- start footer -->
<div id="footer" class="noLHN clearfix">
    <div id="footerLinks">
		<ul class="clearfix">
			<#if supportDetails><li><a class="lastFooter" href="mailto:${supportEmail}">${supportText}</a></li></#if>
		</ul>
	</div>
    <div id="footerCopy">${footerCopy}</div>
</div>
<!-- end footer -->