<#if !sectionName??>
  <#assign sectionName = "home">
</#if>
<li class="page_item<#if sectionName == "home"> current_page_item</#if>"><a href="/" title="Home">Home</a></li>
<#if authService.hasActions("environment.list")><li class="page_item<#if sectionName == "environments"> current_page_item</#if>"><a href="/environments" title="Sites">Environments</a></li></#if>
<#if authService.hasActions("app.list")><li class="page_item<#if sectionName == "apps"> current_page_item</#if>"><a href="/apps" title="Apps">Apps</a></li></#if>