<#if !supportDetails??>
    <#assign supportDetails = true>
</#if>
<#if !supportEmail??>
    <#assign supportEmail = "mailto:help@amee.com">
</#if>
<#if !supportText??>
    <#assign supportText = "Email AMEE Support Team">
</#if>
<#if !footerCopy??>
    <#assign footerCopy = "&copy; 2009 AMEE Ltd.">
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