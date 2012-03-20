<#assign pageTitle = "500 - Internal Server Error">
<#include '/includes/before_content.ftl'>
<h1>500 - Internal Server Error</h1>
<p>500 - Internal Server Error</p>
<#if status??>
    <p>Error: ${status.description}</p>
</#if>
<#include '/includes/after_content.ftl'>