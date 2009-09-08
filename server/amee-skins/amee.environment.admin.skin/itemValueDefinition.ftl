<script type="text/javascript">
    function localeonchange() {
        var form = $('update');
        if ($('localeName')) {
            $('localeName').remove();
        }
        var locale = form['localeName_part'].value;
        var value = form['localeValue_part'].value;
        var locale_value = new Element('input', { id: "localeName", name: "name_" + locale , value: value});
        locale_value.hide()
        form.insert(locale_value);
    }
</script>
<#assign sectionName = 'environments'>
<#include '/includes/before_content.ftl'>
<h1>Environment Administration</h1>
<p><a href='/environments'>Environments</a> / 
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions'>Item Definitions</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions'>Item Value Definitions</a> /
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${itemValueDefinition.uid}'>${itemValueDefinition.name}</a></p>
<h2>Item Value Definition Details</h2>
<p>Name: ${itemValueDefinition.name}<br/>
   Value Definition: <#if browser.valueDefinitionActions.allowView><a href='/environments/${environment.uid}/valueDefinitions/${itemValueDefinition.valueDefinition.uid}'>${itemValueDefinition.valueDefinition.name}</a><#else>${itemValueDefinition.valueDefinition.name}</#if><br/>
   Value Type: ${itemValueDefinition.valueDefinition.valueType}<br/>
   Created: ${itemValueDefinition.created?datetime}<br/>
   Modified: ${itemValueDefinition.modified?datetime}<br/>
</p>
<#if browser.itemValueDefinitionActions.allowModify>
  <h2>Update Item Value Definition</h2>
  <p>

  <form id="update" action='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${itemValueDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>


    <table>
        <tr>
           <td>Name:</td>
           <td colspan="2"><input name='name' value='${itemValueDefinition.name}' type='text' size='30'/></td>
        </tr>
        <tr>
           <td>Path:</td>
           <td colspan="2"><input name='path' value='${itemValueDefinition.path}' type='text' size='30'/></td>
        </tr>
        <tr>
           <td>Default value:</td>
           <td colspan="2"><input name='value' <#if itemValueDefinition.value??>value='${itemValueDefinition.value}'</#if> type='text' size='30'/></td>
        </tr>
        <tr>
           <td>Choices:</td>
           <td colspan="2"><input name='choices' <#if itemValueDefinition.choices??>value='${itemValueDefinition.choices}'</#if> type='text' size='30'/></td>
        </tr>
        <tr>
           <td>Get value from admin?:</td>
           <td colspan="2">
            <select name='fromData'>
                <option value='false'<#if !itemValueDefinition.fromData> selected</#if>>No</option>
                <option value='true'<#if itemValueDefinition.fromData> selected</#if>>Yes</option>
            </select>
           </td>
        </tr>
        <tr>
           <td>Get value from user?:</td>
           <td colspan="2">
            <select name='fromProfile'>
                <option value='false'<#if !itemValueDefinition.fromProfile> selected</#if>>No</option>
                <option value='true'<#if itemValueDefinition.fromProfile> selected</#if>>Yes</option>
            </select>
           </td>
        </tr>
        <tr>
            <td>Allowed roles:</td>
            <td colspan="2"><input name='allowedRoles' <#if itemValueDefinition.allowedRoles??>value='${itemValueDefinition.allowedRoles}'</#if> type='text' size='30'/></td>
         </tr>
        <tr>
            <td>Unit:</td>
            <td colspan="2"><input name='unit' <#if itemValueDefinition.unit??>value='${itemValueDefinition.unit}'</#if> type='text' size='10'/></td>
         </tr>
        <tr>
            <td>PerUnit:</td>
            <td colspan="2"><input name='perUnit' <#if itemValueDefinition.perUnit??>value='${itemValueDefinition.perUnit}'</#if> type='text' size='10'/></td>
         </tr>
        <tr>
           <td>API Version:</td>
           <td colspan="2">
               <#list apiVersions as v>
                 ${v.version}<input type="checkbox" name="apiversion-${v.version}" value="true" <#if itemValueDefinition.isValidInAPIVersion(v)>checked</#if>>
               </#list>                                             
           </td>
        </tr>
        <tr>
           <td>Alias To:</td>
           <td colspan="2">
            <select name='aliasedTo'>
                <#if !itemValueDefinition.aliasedTo??>
                    <option value='' selected>None</option>
                </#if>
                <#list itemDefinition.itemValueDefinitions as ivd>
                    <#if itemValueDefinition.uid != ivd.uid>
                        <option value='${ivd.uid}' <#if itemValueDefinition.aliasedTo?? && itemValueDefinition.aliasedTo.uid == ivd.uid>selected</#if>>${ivd.name}</option>
                    </#if>
                </#list>
            </select>
           </td>
        </tr>
        <tr>
            <td>Treat as timeseries:</td>
            <td>
                <select name='forceTimeSeries'>
                    <option value='false'<#if !itemValueDefinition.forceTimeSeries> selected</#if>>No</option>
                    <option value='true'<#if itemValueDefinition.forceTimeSeries> selected</#if>>Yes</option>
                </select>
            </td>
        </tr>
        <#if itemValueDefinition.localeNames?size != 0>
            <#list itemValueDefinition.localeNames?keys as locale>
            <tr>
                <td>Name: [${locale}]</td>
                <td><input name='name_${locale}' value='${itemValueDefinition.localeNames[locale].name}' type='text' size='30'/></td>
                <td>Remove: <input type="checkbox" name="remove_name_${locale}"/> </td>
            </tr>
            </#list>
        </#if>
        <tr>
            <td>New Locale Name:</td>
            <td>
                <select name='localeName_part' onchange='javascript:localeonchange();'> <br/>
                <#list availableLocales as locale>
                    <option value='${locale}'>${locale}</option>
                </#list>
                </select>
                <input name='localeValue_part' type='text' size='30' onchange='javascript:localeonchange();'/><br/>
            </td>
        </tr>
    </table>
    <input type='submit' value='Update'/>
  </form>
  </p>
</#if>
<#include '/includes/after_content.ftl'>