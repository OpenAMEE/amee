<#assign sectionName = "environments">
<#assign pagerItemsLabel = 'users'>
<#assign pagerUrl = path + '?search=' + search>

<#include '/includes/before_content.ftl'>

<script type="text/javascript">
    function deleteUser(environmentUid, userUid) {
        resourceUrl = '/environments/' + environmentUid + '/users/' + userUid + '?method=delete';
        resourceElem = $('Elem_' + userUid);
        resourceType = 'User';
        var deleteResource = new DeleteResource()
        deleteResource.deleteResource(resourceUrl, resourceElem, resourceType);
    }
</script>

<h1>Environment Administration</h1>

<p><a href='/environments'>Environments</a> /
   <a href='/environments/${environment.uid}'>${environment.name}</a> /
   <a href='/environments/${environment.uid}/users'>Users</a></p>

<h2>Users</h2>

<p>

    <form action='/environments/${environment.uid}/users' method='GET'>
        Search: <input name='search' value='${search}' type='text' size='20'/>&nbsp;<input type='submit' value="Go!"/><br/>
    </form>

    <#assign pagerName = 'pagerTop'>
    <#include '/includes/pager.ftl'>

    <table>
    <tr>
        <th>Username</th>
        <th>Name</th>
        <th>API Version</th>
        <th>Status</th>
        <th>Actions</th>
    </tr>
    <#list users as u>
        <#if canViewEntity(u)>
            <tr id='Elem_${u.uid}'>
                <td>${u.username}</td>
                <td>${u.name}</td>
                <td>${u.APIVersion}</td>
                <td>${u.status}</td>
                <td>
                    <a href='/environments/${environment.uid}/users/${u.uid}'><img src="/images/icons/page_edit.png" title="Edit" alt="Edit" border="0"/></a>
                    <#if canDeleteEntity(u)><input type="image" onClick="deleteUser('${environment.uid}', '${u.uid}'); return false;" src="/images/icons/page_delete.png" title="Delete" alt="Delete" border="0"/></#if>
                </td>
            </tr>
        </#if>
    </#list>
    </table>

    <#assign pagerName = 'pagerBottom'>
    <#include '/includes/pager.ftl'>

</p>

<#if canCreate()>

    <h2>Create User</h2>

    <p>

        <form action='/environments/${environment.uid}/users' method='POST' enctype='application/x-www-form-urlencoded'>
            Groups: clone from <select name="cloneUserUid">
                <option value="">(Select User)</option>
                <#list users as u>
                    <option value="${u.uid}">${u.username}</option>
                </#list>
            </select> OR enter groups <input name='groups' type='text' size='10'/> (comma separated case-sensitive names).<br/>
            Name: <input name='name' type='text' size='60'/><br/>
            Username: <input name='username' type='text' size='30'/><br/>
            Password: <input name='password' type='password' size='30'/><br/>
            Email: <input name='email' type='text' size='60'/><br/>
            API Version: <select name='apiVersion'> <br/>
                <#list apiVersions as apiVersion>
                    <option value='${apiVersion.version}'>${apiVersion.version}</option>
                </#list>
            </select><br/>
            Locale: <select name='locale'> <br/>
                <option value='en_GB'>en_GB</option>
                <#list availableLocales as locale>
                    <option value='${locale}'>${locale}</option>
                </#list>
            </select>
            <br/><br/>

            <input type='submit' value="Create User"/><br/>

        </form>

    </p>

</#if>

<#include '/includes/after_content.ftl'>