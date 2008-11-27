<#include 'profileCommon.ftl'>
<#include '/includes/before_content.ftl'>

<h1>Profile Item Value</h1>

<#include 'profileTrail.ftl'>

<h2>Profile Item Value Details</h2>
<p><#if profileItemValue.value != ''>Value: ${profileItemValue.value}<br/></#if>
    Full Path: ${browser.fullPath}<br/>
    Data Item Label: ${profileItemValue.item.dataItem.label}<br/>
    Item Value Definition: ${profileItemValue.itemValueDefinition.name}<br/>
    Value Definition: ${profileItemValue.itemValueDefinition.valueDefinition.name}<br/>
    Value Type: ${profileItemValue.itemValueDefinition.valueDefinition.valueType}<br/>
    Environment: ${profileItemValue.item.environment.name}<br/>
    UID: ${profileItemValue.uid}<br/>
    Created: ${profileItemValue.created?datetime}<br/>
    Modified: ${profileItemValue.modified?datetime}<br/>
</p>

<#if browser.profileItemValueActions.allowModify>
    <h2>Update Profile Item Value</h2>
    <p>
    <form action='${basePath}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
        Value:
        <#if profileItemValue.itemValueDefinition.choicesAvailable>
            <select name='value'>
                <#list profileItemValue.itemValueDefinition.choiceList as choice>
                    <option value='${choice.value}'<#if profileItemValue.value == choice.value>
                            selected</#if>>${choice.name}</option>
                </#list>
            </select>
        <#else>
            <input name='value' value='${profileItemValue.value}' type='text' size="30"/>
        </#if><br/><br/>
        <input type='submit' value='Update'/>
    </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>