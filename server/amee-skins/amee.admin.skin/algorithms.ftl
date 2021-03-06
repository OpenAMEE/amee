<#assign sectionName = 'admin'>

<#include '/includes/before_content.ftl'>

<script type="text/javascript">
    function deleteAlgorithm(algorithmUid) {
        resourceUrl = '/admin/itemDefinitions/${itemDefinition.uid}/algorithms/' + algorithmUid + '?method=delete';
        resourceElem = $('Elem_' + algorithmUid);
        resourceType = 'Algorithm';
        var deleteResource = new DeleteResource()
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }
</script>

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> /
   <a href='/admin/itemDefinitions'>Item Definitions</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}/algorithms'>Algorithms</a></p>

<h2>Algorithms</h2>
<p>
    <table>
        <tr>
            <th>Name</th>
            <th>Actions</th>
        </tr>
        <#list algorithms as al>
            <#if canViewEntity(al)>
                <tr id="Elem_${al.uid}">
                    <td>${al.name}</td>
                    <td>
                        <a href='/admin/itemDefinitions/${itemDefinition.uid}/algorithms/${al.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                        <#if canDeleteEntity(al)><input type="image" onClick="deleteAlgorithm('${al.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                    </td>
                </tr>
            </#if>
        </#list>
    </table>
</p>

<#if canCreate()>
    <h2>Create Algorithm</h2>
    <p>
        <form action='/admin/itemDefinitions/${itemDefinition.uid}/algorithms' method='POST' enctype='application/x-www-form-urlencoded'>
            Name: <input name='name' type='text' size='30'/><br/>
            Content: <textarea name='content' rows='15' cols='60'></textarea><br/><br/>
            <input type='submit' value='Create'/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>