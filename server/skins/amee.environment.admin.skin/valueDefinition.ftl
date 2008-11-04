<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/valueDefinitions'>Value Definitions</a> / 
   <a href='/environments/${environment.uid}/valueDefinitions/${valueDefinition.uid}'>${valueDefinition.name}</a></p>
<h2>Value Definition Details</h2>
<p>Name: ${valueDefinition.name}<br/>
   Value Type: ${valueDefinition.valueType}<br/>
   Created: ${valueDefinition.created?string.short}<br/>
   Modified: ${valueDefinition.modified?string.short}<br/>
</p>
<#if browser.valueDefinitionActions.allowModify>
  <h2>Update Value Definition</h2>
  <p>
  <form action='/environments/${environment.uid}/valueDefinitions/${valueDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
    Name: <input name='name' value='${valueDefinition.name}' type='text' size='30'/><br/>
    Description: <input name='description' value='${valueDefinition.description}' type='text' size='50'/><br/>
    Value Type: <select name='valueType'>
      <#list valueTypes?keys as key>
        <option value='${key}'<#if valueDefinition.valueType == key> selected</#if>>${valueTypes[key]}</option>
      </#list>
    </select><br/><br/>
    <input type='submit' value='Update'/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>