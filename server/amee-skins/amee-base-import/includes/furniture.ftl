<#function startEndDate date>
    <#return date?string("yyyy-MM-dd'T'HH:mm:ssZ")/>
</#function>

<#function getDateFormatV1>
    <#return "yyyyMMdd"/>
</#function>

<#function getDateFormat version="2.0">
    <#if version == "1.0">
        <#return getDateFormatV1()/>
    <#else>
        <#return "yyyy-MM-dd'T'HH:mm:ssZ"/>
    </#if>
</#function>