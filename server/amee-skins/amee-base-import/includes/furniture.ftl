<#function startEndDate date>
    <#return date?string("yyyy-MM-dd'T'HH:mm:ssZ")/>
</#function>

<#function getDateFormatV1>
    <#return "yyyyMMdd"/>
</#function>

<#function getDateFormat>
    <#return "yyyy-MM-dd'T'HH:mm:ssZ"/>
</#function>

<#assign loader = '<img src="/images/ajax-loader.gif"/>'>