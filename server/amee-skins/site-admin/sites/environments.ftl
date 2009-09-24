<#assign sectionName = "environments">

<#include '/includes/before_content.ftl'>

<script type="text/javascript">
    function deleteEnvironment(environmentUid) {
        resourceUrl = '/environments/' + environmentUid + '?method=delete';
        resourceElem = $('Elem_' + environmentUid);
        resourceType = 'Environment';
        var deleteResource = new DeleteResource()
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a></p>

<p>

    <#assign pagerItemsLabel = 'environments'>
    <#assign pagerName = 'pagerTop'>
    <#include '/includes/pager.ftl'>

    <table>
        <tr>
            <th>Name</th>
            <th>&nbsp;</th>
        </tr>
        <#list environments as e>
            <#if canViewEntity(e)>
                <tr id='Elem_${e.uid}'>
                    <td>${e.name}</td>
                    <td>
                        <a href='/environments/${e.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                        <#-- if canDeleteEntity(e)><input type="image" onClick="deleteEnvironment('${e.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if-->
                    </td>
                </tr>
            </#if>
        </#list>
    </table>

    <#assign pagerName = 'pagerBottom'>
    <#include '/includes/pager.ftl'>

</p>

<#if canCreate()>
    <h2>Create Environment</h2>
    <p>
        <form action='/environments' method='POST' enctype='application/x-www-form-urlencoded'>
            Name: <input name='name' type='text' size='30'/><br/>
            Path: <input name='path' type='text' size='30'/><br/><br/>
            <input type='submit' value="Create Environment"/><br/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>