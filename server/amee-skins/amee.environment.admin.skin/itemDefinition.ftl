<#assign sectionName = 'environments'>

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
        locale_value.hide();
        form.insert(locale_value);
    }
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions'>Item Definitions</a> / 
   <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}'>${itemDefinition.name}</a></p>

<h2>Item Definition Details</h2>
<p>Name: ${itemDefinition.name}<br/>
   Created: ${itemDefinition.created?string.short}<br/>
   Modified: ${itemDefinition.modified?string.short}<br/>
</p>

<h2>Manage</h2>
<p>
  <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/algorithms'>Algorithms</a><br/>
  <a href='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}/itemValueDefinitions'>Item Value Definitions</a><br/>
</p>

<#if canModify()>
    <h2>Update Item Definition</h2>
    <form id="update" action='/environments/${environment.uid}/itemDefinitions/${itemDefinition.uid}?method=put' method='POST' enctype='application/x-www-form-urlencoded'>
        <table>
            <tr>
                <td>Name:</td>
                <td colspan="2"><input name='name' value='${itemDefinition.name}' type='text' size='30'/></td>
            </tr>
            <tr>
                <td>Drill Down:</td>
                <td colspan="2"><input name='drillDown' value='${itemDefinition.drillDown}' type='text' size='30'/></td>
            </tr>
        </table><br/>
        <input type='submit' value='Update'/>
    </form>
</#if>

<#include '/includes/after_content.ftl'>