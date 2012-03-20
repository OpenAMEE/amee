<#include '/dataCommon.ftl'>
<#include '/includes/before_content.ftl'>

<h1>Data Category Drill Down</h1>

<#include '/${activeUser.getAPIVersion()}/dataTrail.ftl'>

<#if selections?? && selections?size gt 0>
    <h2>Selections</h2>
    <p>
        <table>
            <tr>
                <th>Name</th>
                <th>Value</th>
            </tr>
            <#assign choiceUrl = '${basePath}/drill?'>
            <#list selections as selection>
                <#assign choiceUrl = choiceUrl + '${selection.name?url}=${selection.value?url}&'>
                <tr>
                    <td>${selection.name}</td>
                    <td>${selection.value}</td>
                </tr>
            </#list>
        </table>
    </p>
<#else>
    <#assign choiceUrl = '${basePath}/drill?'>
</#if>

<#if choices??>
<h2>Choices for ${choices.name}</h2>
<p>
    <table>
        <tr>
            <th>Name</th>
            <th>Value</th>
            <th>Actions</th>
        </tr>
        <#list choices.choices as choice >
            <tr>
                <td>${choice.name}</td>
                <td>${choice.value}</td>
                <#if choices.name == 'uid'>
                    <td><a href='${basePath}/${choice.value?url}'>View</a></td>
                <#else>
                    <td><a href='${choiceUrl}${choices.name?url}=${choice.value?url}'>Choose</a></td>
                </#if>
            </tr>
        </#list>
    </table>
</p>
</#if>

<#include '/includes/after_content.ftl'>