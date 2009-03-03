<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteAttributeGroup(environmentUid, attributeGroupUid) {
  resourceUrl = '/environments/' + environmentUid + '/attributeGroups/' + attributeGroupUid + '?method=delete';
  resourceElem = $('Elem_' + attributeGroupUid);
  resourceType = 'Attribute Group';
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroups'>Attribute Groups</a></p>


<#if browser.attributeGroupActions.allowList>
<h2>Attribute Groups</h2>
<p>

<#assign pagerItemsLabel = 'attribute groups'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th>Actions</th>
</tr>
<#list attributeGroups as ag>
  <tr id='Elem_${ag.uid}'>
    <td>${ag.name}</td>
    <td>
        <#if browser.attributeGroupActions.allowView><a href='/environments/${environment.uid}/attributeGroups/${ag.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.attributeGroupActions.allowDelete><input type="image" onClick="deleteAttributeGroup('${environment.uid}', '${ag.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>

<#if browser.attributeGroupActions.allowCreate>
<h2>Create Attribute Group</h2>

<p>
<form action='/environments/${environment.uid}/attributeGroups' method='POST' enctype='application/x-www-form-urlencoded'>
Attribute Group Definition: <select name="attributeGroupDefinitionUid">
    <#list attributeGroupDefinitions as agd>
        <option value="${agd.uid}">${agd.name}</option>
    </#list>
    </select><br/>
Name: <input name='name' type='text' size='30'/><br/>
<br/><input type='submit' value="Create Attribute Group"/><br/>
</form>
</p>
</#if>
<#include '/includes/after_content.ftl'>