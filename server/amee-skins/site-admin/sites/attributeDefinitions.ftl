<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>

<script type="text/javascript">
function deleteAttributeDefinition(attributeGroupDefinitionUid, attributeDefinitionUid) {
  resourceUrl = '/environments/${environment.uid}/attributeGroupDefinitions/' + attributeGroupDefinitionUid + '/attributeDefinitions/' + attributeDefinitionUid + '?method=delete';
  resourceElem = $('Elem_' + attributeDefinitionUid);
  resourceType = 'Attribute Definition';
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions'>Attribute Group Definitions</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}'>${attributeGroupDefinition.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}/attributeDefinitions'>Attribute Definitions</a></p>

<h2>Attribute Definitions</h2>
<#if browser.attributeDefinitionActions.allowList>
<p>

<#assign pagerItemsLabel = 'attribute definitions'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>

<table>
<tr>
  <th>Name</th>
  <th></th>
</tr>
<#list attributeDefinitions as ad>
  <tr id='Elem_${ad.uid}'>
    <td>${ad.key}</td>
    <td>
        <#if browser.attributeDefinitionActions.allowView><a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}/attributeDefinitions/${ad.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.attributeDefinitionActions.allowDelete><input type="image" onClick="deleteAttributeDefinition('${attributeGroupDefinition.uid}', '${ad.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>

<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>

</p>
</#if>
<#if browser.attributeDefinitionActions.allowCreate>
<h2>Create Attribute Definition</h2>

<p>
<form action='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}/attributeDefinitions' method='POST' enctype='application/x-www-form-urlencoded'>
Key: <input name='key' type='text' size='30'/><br/><br/>
<input type='submit' value="Create"/><br/>
</form>
</p>
</#if>
<#include '/includes/after_content.ftl'>