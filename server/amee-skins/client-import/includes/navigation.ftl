<#if !sectionName??>
  <#assign sectionName = "home">
</#if>
<li class="page_item<#if sectionName == "home"> current_page_item</#if>"><a href="/" title="Home">Home</a></li>
<li class="page_item<#if sectionName == "profiles"> current_page_item</#if>"><a href="/profiles" title="Profiles">Profiles</a></li>
<li class="page_item<#if sectionName == "data"> current_page_item</#if>"><a href="/data" title="Data">Data</a></li>
