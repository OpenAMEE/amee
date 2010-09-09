<#if !sectionName??>
  <#assign sectionName = "home">
</#if>
<li class="page_item<#if sectionName == "home"> current_page_item</#if>"><a href="/" title="Home">Home</a></li>
<li class="page_item<#if sectionName == "admin"> current_page_item</#if>"><a href="/admin" title="Admin">Admin</a></li>