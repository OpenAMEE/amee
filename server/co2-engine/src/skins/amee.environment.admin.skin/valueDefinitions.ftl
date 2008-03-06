<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<script type="text/javascript">
function deleteValueDefinition(valueDefinitionUid) {
  resourceUrl = '/environments/${environment.uid}/valueDefinitions/' + valueDefinitionUid + '?method=delete';
  resourceElem = $('Elem_' + valueDefinitionUid);
  resourceType = 'Value Definition'; 
  var deleteResource = new DeleteResource()
  deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
}
</script>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/valueDefinitions'>Value Definitions</a></p>
<h2>Value Definitions</h2>
<p>
<#assign pagerItemsLabel = 'value definitions'>
<#assign pagerName = 'pagerTop'>
<#include '/includes/pager.ftl'>
<table>
<tr>
  <th>Name</th>
  <th>Value Type</th>
  <th>Actions</th>
</tr>
<#list valueDefinitions as vd>
  <tr id='Elem_${vd.uid}'>
    <td>${vd.name}</td>
    <td>${vd.valueType}</td>
    <td>
        <#if browser.itemValueDefinitionActions.allowView><a href='/environments/${environment.uid}/valueDefinitions/${vd.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a></#if>
        <#if browser.itemValueDefinitionActions.allowDelete><input type="image" onClick="deleteValueDefinition('${vd.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
    </td>
  </tr>
</#list>
</table>
<#assign pagerName = 'pagerBottom'>
<#include '/includes/pager.ftl'>
</p>
<#if browser.valueDefinitionActions.allowCreate>
<p>
  <h2>Create Value Definition</h2>
  <p>
    <form action='/environments/${environment.uid}/valueDefinitions' method='POST' enctype='application/x-www-form-urlencoded'>
    Name: <input name='name' type='text' size='30'/><br/><br/>
    Value Type: <select name='valueType'>
      <#list valueTypes?keys as key>
        <option value='${key}'>${valueTypes[key]}</option>
      </#list>
    </select><br/><br/>
    <input type='submit' value='Create'/>
  </form>
  </p>
<p>
</#if>
<#include '/includes/after_content.ftl'>