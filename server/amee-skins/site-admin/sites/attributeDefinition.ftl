<#assign sectionName = "environments">
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions'>Attribute Group Definitions</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}'>${attributeGroupDefinition.name}</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}/attributeDefinitions'>Attribute Definitions</a> /
   <a href='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}/attributeDefinitions/${attributeDefinition.uid}'>${attributeDefinition.key}</a></p>
<h2>Details</h2>
<#if browser.attributeDefinitionActions.allowView>
<p>
Name: ${attributeDefinition.key}<br/>
Created: ${attributeDefinition.created?string.short}<br/>
Modified: ${attributeDefinition.modified?string.short}<br/>
</p>
</#if>
<#if browser.attributeDefinitionActions.allowModify>
  <h2>Update Attribute Definition</h2>
  <p>
  <form action='/environments/${environment.uid}/attributeGroupDefinitions/${attributeGroupDefinition.uid}/attributeDefinitions/${attributeDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
  Key: <input name='key' value='${attributeDefinition.key}' type='text' size='30'/> (the distinct attribute variable name)<br/>
  Label: <input name='label' value='${attributeDefinition.label}' type='text' size='30'/> (shown next to form fields)<br/>
  Explanation: <textarea name='explanation' rows='5' cols='60'>${attributeDefinition.explanation?html}</textarea><br/>
  Default Value: <input name='value' value='${attributeDefinition.value}' type='text' size='30'/> (replaces empty attribute values)<br/>
  Options: <textarea name='options' rows='5' cols='60'>${attributeDefinition.options?html}</textarea> (comma separated name=value list)<br/>
  Style: <input name='style' value='${attributeDefinition.style}' type='text' size='30'/> (inserted into style attribute of input control)<br/>
  Mandatory:
    Yes <input type="radio" name="mandatory" value="true"<#if attributeDefinition.mandatory> checked</#if>/>
    No <input type="radio" name="mandatory" value="false"<#if !attributeDefinition.mandatory> checked</#if>/> (for client side validation)<br/>
  Validation: <input name='validation' value='${attributeDefinition.validation}' type='text' size='30'/> (for client side validation)<br/>
    Value Type: <select id="valueType" name="valueType">
        <option value="INHERITED"<#if !attributeDefinition.valueType??> selected="selected"</#if>>Use default (${attributeDefinition.attributeGroupDefinition.valueType})</option>
        <option value="UNSPECIFIED"<#if attributeDefinition.valueType?? && attributeDefinition.valueType == 'UNSPECIFIED'> selected="selected"</#if>>Unspecified</option>
        <option value="TEXT"<#if attributeDefinition.valueType?? && attributeDefinition.valueType == 'TEXT'> selected="selected"</#if>>Text</option>
        <option value="DATE"<#if attributeDefinition.valueType?? && attributeDefinition.valueType == 'DATE'> selected="selected"</#if>>Date</option>
        <option value="BOOLEAN"<#if attributeDefinition.valueType?? && attributeDefinition.valueType == 'BOOLEAN'> selected="selected"</#if>>Boolean</option>
        <option value="INTEGER"<#if attributeDefinition.valueType?? && attributeDefinition.valueType == 'INTEGER'> selected="selected"</#if>>Integer</option>
        <option value="DECIMAL"<#if attributeDefinition.valueType?? && attributeDefinition.valueType == 'DECIMAL'> selected="selected"</#if>>Decimal</option>
    </select><br/>
    Widget Type: <select id="widgetType" name="widgetType">
        <option value="INHERITED"<#if !attributeDefinition.widgetType??> selected="selected"</#if>>Use Default (${attributeDefinition.attributeGroupDefinition.widgetType})</option>
        <option value="INPUT"<#if attributeDefinition.widgetType?? && attributeDefinition.widgetType == 'INPUT'> selected="selected"</#if>>Input</option>
        <option value="TEXTAREA"<#if attributeDefinition.widgetType?? && attributeDefinition.widgetType == 'TEXTAREA'> selected="selected"</#if>>Text Area</option>
        <option value="SELECT"<#if attributeDefinition.widgetType?? && attributeDefinition.widgetType == 'SELECT'> selected="selected"</#if>>Select</option>
        <option value="RADIO"<#if attributeDefinition.widgetType?? && attributeDefinition.widgetType == 'RADIO'> selected="selected"</#if>>Radio</option>
        <option value="CHECK"<#if attributeDefinition.widgetType?? && attributeDefinition.widgetType == 'CHECK'> selected="selected"</#if>>Check</option>
    </select><br/><br/>
  <input type='submit' value="Update"/><br/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>