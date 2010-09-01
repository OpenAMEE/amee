<#include '/dataCommon.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<script type='text/javascript'>

function localeonchange() {
    var form = $('update');
    if ($('localeValue')) {
        $('localeValue').remove();
    }
    var locale = form['localeName_part'].value;
    var value = form['localeValue_part'].value;
    var locale_value = new Element('input', { id: "localeValue", name: "value_" + locale , value: value});
    locale_value.hide()
    form.insert(locale_value);
}

</script>

<h1>Data Item Value</h1>

<#include 'dataTrail.ftl'>

<h2>Data Item Value Details</h2>
<p>Value: ${itemValue.value}<br/>
    Full Path: ${basePath}<br/>
    Item Value Definition: ${itemValue.itemValueDefinition.name}<br/>
    Value Definition: ${itemValue.itemValueDefinition.valueDefinition.name}<br/>
    Value Type: ${itemValue.itemValueDefinition.valueDefinition.valueType}<br/>
    Item: ${itemValue.item.label}<br/>
    UID: ${itemValue.uid}<br/>
    Created: ${itemValue.created?string.short}<br/>
    Modified: ${itemValue.modified?string.short}<br/>
</p>

<#if canModify()>
    <h2>Update Data Item Value</h2>
    <p>
        <form id="update" action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
            <table>
                <tr>
                   <td>Value:</td>
                   <td colspan="2">
                       <#if itemValue.itemValueDefinition.choicesAvailable>
                           <select name='value'>
                               <#list itemValue.itemValueDefinition.choiceList as choice>
                                   <option value='${choice.value}'<#if itemValue.value == choice.value> selected</#if>>${choice.name}</option>
                               </#list>
                           </select>
                       <#else>
                           <input name='value' value='${itemValue.value}' type='text' size="30"/>
                           <#if itemValue.hasUnit()>
                               Unit: <input name='unit' value='${itemValue.unit}' type='text' size="10"/>
                           </#if>
                           <#if itemValue.hasPerUnit()>
                               Per Unit: <input name='perUnit' value='${itemValue.perUnit}' type='text' size="10"/>
                           </#if>
                       </#if>
                   </td>
                </tr>
            </table><br/>

            <input type='submit' value='Update'/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>