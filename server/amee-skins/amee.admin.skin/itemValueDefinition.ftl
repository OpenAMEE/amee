<#assign sectionName = 'admin'>

<#include '/includes/before_content.ftl'>

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

<h1>Administration</h1>

<p><a href='/admin'>Admin</a> /
   <a href='/admin/itemDefinitions'>Item Definitions</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions'>Item Value Definitions</a> /
   <a href='/admin/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${itemValueDefinition.uid}'>${itemValueDefinition.name}</a></p>

<h2>Item Value Definition Details</h2>

<p>Name: ${itemValueDefinition.name}<br/>
   Value Definition: <a href='/admin/valueDefinitions/${itemValueDefinition.valueDefinition.uid}'>${itemValueDefinition.valueDefinition.name}</a><br/>
   Value Type: ${itemValueDefinition.valueDefinition.valueType}<br/>
   Created: ${itemValueDefinition.created?datetime}<br/>
   Modified: ${itemValueDefinition.modified?datetime}<br/>
</p>

<#if canModify()>

    <h2>Update Item Value Definition</h2>
    <p>
        <form id="update" action='/admin/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions/${itemValueDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
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
                    <td>Unit:</td>
                    <td colspan="2"><input name='unit' <#if itemValueDefinition.hasUnit()??>value='${itemValueDefinition.unit}'</#if> type='text' size='10'/></td>
                 </tr>
                <tr>
                    <td>PerUnit:</td>
                    <td colspan="2"><input name='perUnit' <#if itemValueDefinition.hasPerUnit()??>value='${itemValueDefinition.perUnit}'</#if> type='text' size='10'/></td>
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
            </table>
            <input type='submit' value='Update'/>
        </form>
    </p>
</#if>

<#include '/includes/after_content.ftl'>