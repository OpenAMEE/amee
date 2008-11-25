<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions'>Attribute Group Definitions</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}'>${attributeGroupDefinition.name}</a></p>
<h2>Environment Details</h2>
<#if browser.attributeGroupDefinitionActions.allowView>
<p>
Name: ${attributeGroupDefinition.name}<br/>
Created: ${attributeGroupDefinition.created?string.short}<br/>
Modified: ${attributeGroupDefinition.modified?string.short}<br/>
</p>
</#if>
<h2>Manage</h2>
<p>
<a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}/attributeDefinitions'>Attribute Definitions</a><br/>
</p>
<#if browser.attributeGroupDefinitionActions.allowModify>
  <h2>Update Attribute Group Definition</h2>
  <p>
  <form action='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Name: <input name='name' value='${attributeGroupDefinition.name}' type='text' size='30'/><br/>
  keys: <textarea name='keys' rows='5' cols='60'>${attributeGroupDefinition.keys?html}</textarea> (comma separated key=label pairs)<br/>
    Default Value Type: <select id="valueType" name="valueType">
        <option value="UNSPECIFIED"<#if attributeGroupDefinition.valueType == 'UNSPECIFIED'> selected="selected"</#if>>Unspecified</option>
        <option value="TEXT"<#if attributeGroupDefinition.valueType == 'TEXT'> selected="selected"</#if>>Text</option>
        <option value="DATE"<#if attributeGroupDefinition.valueType == 'DATE'> selected="selected"</#if>>Date</option>
        <option value="BOOLEAN"<#if attributeGroupDefinition.valueType == 'BOOLEAN'> selected="selected"</#if>>Boolean</option>
        <option value="INTEGER"<#if attributeGroupDefinition.valueType == 'INTEGER'> selected="selected"</#if>>Integer</option>
        <option value="DECIMAL"<#if attributeGroupDefinition.valueType == 'DECIMAL'> selected="selected"</#if>>Decimal</option>
    </select><br/>
    Default Widget Type: <select id="widgetType" name="widgetType">
        <option value="INPUT"<#if attributeGroupDefinition.widgetType == 'INPUT'> selected="selected"</#if>>Input</option>
        <option value="TEXTAREA"<#if attributeGroupDefinition.widgetType == 'TEXTAREA'> selected="selected"</#if>>Text Area</option>
        <option value="SELECT"<#if attributeGroupDefinition.widgetType == 'SELECT'> selected="selected"</#if>>Select</option>
        <option value="RADIO"<#if attributeGroupDefinition.widgetType == 'RADIO'> selected="selected"</#if>>Radio</option>
        <option value="CHECK"<#if attributeGroupDefinition.widgetType == 'CHECK'> selected="selected"</#if>>Check</option>
    </select><br/><br/>
  <input type='submit' value="Update"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>